
package com.securemessaging.controller;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import java.time.LocalDateTime;

import com.securemessaging.entity.AttachmentEntity;
import com.securemessaging.entity.GroupEntity;
import com.securemessaging.entity.GroupMemberEntity;
import com.securemessaging.entity.GroupMessageEntity;
import com.securemessaging.entity.GroupMessageReadEntity;
import com.securemessaging.repository.AttachmentRepository;
import com.securemessaging.repository.GroupAttachmentKeyRepository;
import com.securemessaging.repository.GroupEntityRepository;
import com.securemessaging.repository.GroupMemberEntityRepository;
import com.securemessaging.repository.GroupMessageEntityRepository;
import com.securemessaging.repository.GroupMessageReadRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.securemessaging.entity.EncryptedMessageEntity;
import com.securemessaging.mapper.EncryptedMessageMapper;
import com.securemessaging.core.SecureMessagingSystem.EncryptedMessage;
import com.securemessaging.core.SecureMessagingSystem.DecryptedMessageView;
import com.securemessaging.core.SecureMessagingSystem;
import com.securemessaging.service.DatabaseUserService;
import com.securemessaging.service.DatabaseMessagingService;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"", "/api"})
@CrossOrigin(origins = "*")
public class MessagingController {

    private final DatabaseMessagingService databaseMessagingService;
    private final DatabaseUserService databaseUserService;
    private final GroupEntityRepository groupRepository;
    private final GroupMemberEntityRepository groupMemberRepository;
    private final GroupMessageEntityRepository groupMessageRepository;
    private final GroupMessageReadRepository groupMessageReadRepository;
    private final AttachmentRepository attachmentRepository;
    private final GroupAttachmentKeyRepository groupAttachmentKeyRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public MessagingController(DatabaseMessagingService databaseMessagingService,
                               DatabaseUserService databaseUserService,
                               GroupEntityRepository groupRepository,
                               GroupMemberEntityRepository groupMemberRepository,
                               GroupMessageEntityRepository groupMessageRepository,
                               GroupMessageReadRepository groupMessageReadRepository,
                               AttachmentRepository attachmentRepository,
                               GroupAttachmentKeyRepository groupAttachmentKeyRepository,
                               SimpMessagingTemplate messagingTemplate) {
        this.databaseMessagingService = databaseMessagingService;
        this.databaseUserService = databaseUserService;
        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.groupMessageRepository = groupMessageRepository;
        this.groupMessageReadRepository = groupMessageReadRepository;
        this.attachmentRepository = attachmentRepository;
        this.groupAttachmentKeyRepository = groupAttachmentKeyRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @GetMapping("/messages")
    public ResponseEntity<?> getMessages() {
        return ResponseEntity.ok(
                Map.of("status", "Messages endpoint working")
        );
    }

    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        return ResponseEntity.ok(
                Map.of(
                        "server", "Online",
                        "encryption", "Active",
                        "signatures", "Enabled",
                        "storage", "In Memory"
                )
        );
    }

    @GetMapping("/messages/encrypted")
    public ResponseEntity<?> encryptedRecords() {
        return ResponseEntity.ok(
                Map.of(
                        "repository", "Encrypted repository accessible",
                        "status", "Records loaded successfully"
                )
        );
    }

    @PostMapping("/messages/inbox")
    public ResponseEntity<?> inbox() {
        String receiver = org.springframework.security.core.context.SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        return ResponseEntity.ok(
                databaseMessagingService.findInbox(receiver)
        );
    }

    @GetMapping("/users")
    public ResponseEntity<?> users() {
        return ResponseEntity.ok(
                Map.of(
                        "users", new String[]{"Igor", "John", "Simon"}
                )
        );
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> request) {
        return ResponseEntity.ok(
                Map.of(
                        "status", "User registered successfully",
                        "username", request.get("username")
                )
        );
    }

    @PostMapping("/messages/send")
    public ResponseEntity<?> send(@RequestBody Map<String, String> request) throws Exception {
        String senderUsername = org.springframework.security.core.context.SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        String receiverUsername = request.get("receiver");
        String messageText = request.get("message");

        SecureMessagingSystem.User sender =
                databaseUserService.findDomainUser(senderUsername);

        SecureMessagingSystem.User receiver =
                databaseUserService.findDomainUser(receiverUsername);

        SecureMessagingSystem.HybridEncryptionService encryptionService =
                new SecureMessagingSystem.HybridEncryptionService();

        SecureMessagingSystem.EncryptedMessage encryptedMessage =
                encryptionService.encrypt(messageText, sender, receiver);

        EncryptedMessageEntity savedMessage =
                databaseMessagingService.saveEncryptedMessage(encryptedMessage);

        return ResponseEntity.ok(
                Map.of(
                        "status", "Encrypted message saved successfully",
                        "messageId", savedMessage.getId(),
                        "sender", senderUsername,
                        "receiver", receiverUsername
                )
        );
    }


	
    @GetMapping("/api")
    public ResponseEntity<?> home() {
        return ResponseEntity.ok(
                Map.of(
                        "application", "Secure Messaging System",
                        "status", "Running",
                        "deployment", "Render",
                        "apiHealth", "/api/health",
                        "register", "/api/users/register",
                        "login", "/users/login"
                )
        );
    }

    @PostMapping("/messages/inbox/decrypted")
    public ResponseEntity<?> decryptedInbox() throws Exception {
        String receiverUsername = org.springframework.security.core.context.SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        List<EncryptedMessageEntity> encryptedEntities =
                databaseMessagingService.findInbox(receiverUsername);
        List<Map<String, Object>> decryptedMessages =
                new ArrayList<>();

        SecureMessagingSystem.HybridEncryptionService encryptionService =
                new SecureMessagingSystem.HybridEncryptionService();

        SecureMessagingSystem.User receiver =
                databaseUserService.findDomainUser(receiverUsername);

        for (EncryptedMessageEntity entity : encryptedEntities) {
            EncryptedMessage encryptedMessage =
                    EncryptedMessageMapper.toDomain(entity);

            SecureMessagingSystem.User sender =
                    databaseUserService.findDomainUser(
                            encryptedMessage.getSender()
                    );

            String decryptedText =
                    encryptionService.decrypt(
                            encryptedMessage,
                            receiver,
                            sender
                    );

            decryptedMessages.add(
                    Map.of(
                            "id", entity.getId(),
                            "sender", encryptedMessage.getSender(),
                            "receiver", encryptedMessage.getReceiver(),
                            "message", decryptedText,
                            "timestamp", entity.getTimestamp(),
                            "readByReceiver", entity.isReadByReceiver()
                    )
            );
        }

        decryptedMessages.sort(
                (a, b) -> ((LocalDateTime) b.get("timestamp"))
                        .compareTo((LocalDateTime) a.get("timestamp"))
        );

        return ResponseEntity.ok(decryptedMessages);
    }

    @PutMapping("/messages/{messageId}/read")
    public ResponseEntity<?> markMessageAsRead(@PathVariable("messageId") Long messageId) {
        String currentUsername = org.springframework.security.core.context.SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        return databaseMessagingService.findById(messageId)
                .map(message -> {
                    if (!currentUsername.equals(message.getReceiver())) {
                        return ResponseEntity.status(403).body(
                                Map.of("error", "You can only mark your own received messages as read")
                        );
                    }

                    message.setReadByReceiver(true);
                    databaseMessagingService.save(message);

                    return ResponseEntity.ok(
                            Map.of(
                                    "status", "Message marked as read",
                                    "messageId", message.getId(),
                                    "readByReceiver", message.isReadByReceiver()
                            )
                    );
                })
                .orElseGet(() -> ResponseEntity.status(404).body(
                        Map.of("error", "Message not found")
                ));
    }

@PostMapping("/groups/create")
public ResponseEntity<?> createGroup(@RequestBody Map<String, String> request) {
    String currentUsername = org.springframework.security.core.context.SecurityContextHolder
            .getContext()
            .getAuthentication()
            .getName();

    String groupName = request.get("groupName");

    GroupEntity group = new GroupEntity(
            groupName,
            currentUsername,
            LocalDateTime.now()
    );

    GroupEntity savedGroup = groupRepository.save(group);

    groupMemberRepository.save(
            new GroupMemberEntity(savedGroup.getId(), currentUsername)
    );

    return ResponseEntity.ok(
            Map.of(
                    "status", "Group created successfully",
                    "groupId", savedGroup.getId(),
                    "groupName", savedGroup.getGroupName()
            )
    );
}

@GetMapping("/groups/my-groups")
public ResponseEntity<?> myGroups() {
    String currentUsername = org.springframework.security.core.context.SecurityContextHolder
            .getContext()
            .getAuthentication()
            .getName();

    List<GroupMemberEntity> memberships =
            groupMemberRepository.findByUsername(currentUsername);

    List<GroupEntity> groups = new ArrayList<>();

    for (GroupMemberEntity membership : memberships) {
        groupRepository.findById(membership.getGroupId())
                .ifPresent(groups::add);
    }

    return ResponseEntity.ok(groups);
}
@GetMapping("/groups/{groupId}/members")
public ResponseEntity<?> groupMembers(
        @PathVariable("groupId") Long groupId) {

    List<GroupMemberEntity> members =
            groupMemberRepository.findByGroupId(groupId);

    List<String> usernames = members.stream()
            .map(GroupMemberEntity::getUsername)
            .toList();

    return ResponseEntity.ok(usernames);
}

@PostMapping("/groups/{groupId}/join")
public ResponseEntity<?> joinGroup(@PathVariable("groupId") Long groupId) {
    String currentUsername = org.springframework.security.core.context.SecurityContextHolder
            .getContext()
            .getAuthentication()
            .getName();

    if (groupRepository.findById(groupId).isEmpty()) {
        return ResponseEntity.badRequest().body(
                Map.of("error", "Group not found")
        );
    }

    if (groupMemberRepository.findByGroupIdAndUsername(groupId, currentUsername).isPresent()) {
        return ResponseEntity.ok(
                Map.of("status", "Already a group member")
        );
    }

    groupMemberRepository.save(
            new GroupMemberEntity(groupId, currentUsername)
    );

    return ResponseEntity.ok(
            Map.of(
                    "status", "Joined group successfully",
                    "groupId", groupId
            )
    );
}

@DeleteMapping("/groups/{groupId}/leave")
public ResponseEntity<?> leaveGroup(@PathVariable("groupId") Long groupId) {
    String currentUsername = org.springframework.security.core.context.SecurityContextHolder
            .getContext()
            .getAuthentication()
            .getName();

    if (groupMemberRepository.findByGroupIdAndUsername(groupId, currentUsername).isEmpty()) {
        return ResponseEntity.badRequest().body(
                Map.of("error", "You are not a member of this group")
        );
    }

    groupMemberRepository.deleteByGroupIdAndUsername(groupId, currentUsername);

    return ResponseEntity.ok(
            Map.of("status", "Left group successfully")
    );
}

    @DeleteMapping("/groups/{groupId}/members/{username}")
    public ResponseEntity<?> removeGroupMember(
            @PathVariable("groupId") Long groupId,
            @PathVariable("username") String username) {

        String currentUsername = org.springframework.security.core.context.SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        GroupEntity group = groupRepository.findById(groupId).orElse(null);

        if (group == null) {
            return ResponseEntity.status(404).body(
                    Map.of("error", "Group not found")
            );
        }

        if (!group.getCreatedBy().equals(currentUsername)) {
            return ResponseEntity.status(403).body(
                    Map.of("error", "Only the group admin can remove members")
            );
        }

        if (group.getCreatedBy().equals(username)) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "The group admin cannot be removed")
            );
        }

        if (groupMemberRepository.findByGroupIdAndUsername(groupId, username).isEmpty()) {
            return ResponseEntity.status(404).body(
                    Map.of("error", "Group member not found")
            );
        }

        groupMemberRepository.deleteByGroupIdAndUsername(groupId, username);

        messagingTemplate.convertAndSend(
                "/topic/groups/" + groupId,
                Map.of(
                        "type", "GROUP_MEMBER_REMOVED",
                        "groupId", groupId,
                        "username", username
                )
        );

        return ResponseEntity.ok(
                Map.of(
                        "status", username + " was removed from the group",
                        "groupId", groupId,
                        "username", username
                )
        );
    }

    @Transactional
    @DeleteMapping("/groups/{groupId}")
    public ResponseEntity<?> deleteGroup(
            @PathVariable("groupId") Long groupId) {

        String currentUsername = org.springframework.security.core.context.SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        GroupEntity group = groupRepository.findById(groupId).orElse(null);

        if (group == null) {
            return ResponseEntity.status(404).body(
                    Map.of("error", "Group not found")
            );
        }

        if (!group.getCreatedBy().equals(currentUsername)) {
            return ResponseEntity.status(403).body(
                    Map.of("error", "Only the group admin can delete this group")
            );
        }

        String deletedGroupName = group.getGroupName();

        List<AttachmentEntity> groupAttachments =
                attachmentRepository.findByGroupIdOrderByTimestampDesc(groupId);

        List<Long> attachmentIds = groupAttachments.stream()
                .map(AttachmentEntity::getId)
                .toList();

        if (!attachmentIds.isEmpty()) {
            groupAttachmentKeyRepository.deleteByAttachmentIdIn(attachmentIds);
        }

        attachmentRepository.deleteByGroupId(groupId);
        groupMessageReadRepository.deleteByGroupId(groupId);
        groupMessageRepository.deleteByGroupId(groupId);
        groupMemberRepository.deleteByGroupId(groupId);
        groupRepository.delete(group);

        messagingTemplate.convertAndSend(
                "/topic/groups/" + groupId,
                Map.of(
                        "type", "GROUP_DELETED",
                        "groupId", groupId,
                        "groupName", deletedGroupName
                )
        );

        return ResponseEntity.ok(
                Map.of(
                        "status", "Group deleted successfully",
                        "groupId", groupId,
                        "groupName", deletedGroupName
                )
        );
    }

private void markGroupMessageAsRead(Long groupId, Long groupMessageId, String username) {
        if (groupMessageReadRepository.existsByGroupMessageIdAndUsername(groupMessageId, username)) {
            return;
        }

        groupMessageReadRepository.save(
                new GroupMessageReadEntity(
                        groupMessageId,
                        groupId,
                        username,
                        LocalDateTime.now()
                )
        );
    }

@PostMapping("/groups/{groupId}/send")
public ResponseEntity<?> sendGroupMessage(@PathVariable("groupId") Long groupId,
                                          @RequestBody Map<String, String> request) {
    String currentUsername = org.springframework.security.core.context.SecurityContextHolder
            .getContext()
            .getAuthentication()
            .getName();

    if (groupMemberRepository.findByGroupIdAndUsername(groupId, currentUsername).isEmpty()) {
        return ResponseEntity.status(403).body(
                Map.of("error", "You are not a member of this group")
        );
    }

    String messageText = request.get("message");

    GroupMessageEntity message = new GroupMessageEntity(
            groupId,
            currentUsername,
            messageText,
            LocalDateTime.now()
    );

    GroupMessageEntity savedMessage = groupMessageRepository.save(message);

    markGroupMessageAsRead(groupId, savedMessage.getId(), currentUsername);

    messagingTemplate.convertAndSend(
            "/topic/groups/" + groupId,
            Map.of(
                    "type", "NEW_GROUP_MESSAGE",
                    "groupId", groupId,
                    "messageId", savedMessage.getId()
            )
    );

    return ResponseEntity.ok(
            Map.of(
                    "status", "Group message sent",
                    "groupId", groupId,
                    "messageId", savedMessage.getId(),
                    "sender", currentUsername
            )
    );
}

    @PutMapping("/groups/{groupId}/messages/{messageId}")
    public ResponseEntity<?> editOwnGroupMessage(@PathVariable("groupId") Long groupId,
                                                 @PathVariable("messageId") Long messageId,
                                                 @RequestBody Map<String, String> request) {
        String currentUsername = org.springframework.security.core.context.SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        if (groupMemberRepository.findByGroupIdAndUsername(groupId, currentUsername).isEmpty()) {
            return ResponseEntity.status(403).body(
                    Map.of("error", "You are not a member of this group")
            );
        }

        String updatedMessage = request.get("message");

        if (updatedMessage == null || updatedMessage.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Edited message cannot be empty")
            );
        }

        return groupMessageRepository.findById(messageId)
                .map(message -> {
                    if (!message.getGroupId().equals(groupId)) {
                        return ResponseEntity.badRequest().body(
                                Map.of("error", "Message does not belong to this group")
                        );
                    }

                    if (!message.getSender().equals(currentUsername)) {
                        return ResponseEntity.status(403).body(
                                Map.of("error", "You can edit only your own group messages")
                        );
                    }

                    if (!attachmentRepository.findByGroupMessageId(messageId).isEmpty()) {
                        return ResponseEntity.badRequest().body(
                                Map.of("error", "Messages with attachments cannot be edited yet")
                        );
                    }

                    message.setMessage(updatedMessage.trim());
                    message.setEditedAt(LocalDateTime.now());
                    groupMessageRepository.save(message);

                    messagingTemplate.convertAndSend(
                            "/topic/groups/" + groupId,
                            Map.of(
                                    "type", "GROUP_MESSAGE_EDITED",
                                    "groupId", groupId,
                                    "messageId", messageId
                            )
                    );

                    return ResponseEntity.ok(
                            Map.of(
                                    "status", "Group message edited",
                                    "groupId", groupId,
                                    "messageId", messageId,
                                    "message", message.getMessage(),
                                    "editedAt", message.getEditedAt()
                            )
                    );
                })
                .orElseGet(() -> ResponseEntity.status(404).body(
                        Map.of("error", "Group message not found")
                ));
    }

    @PutMapping("/groups/{groupId}/messages/{messageId}/pin")
    public ResponseEntity<?> toggleGroupMessagePin(@PathVariable("groupId") Long groupId,
                                                   @PathVariable("messageId") Long messageId) {
        String currentUsername = org.springframework.security.core.context.SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        if (groupMemberRepository.findByGroupIdAndUsername(groupId, currentUsername).isEmpty()) {
            return ResponseEntity.status(403).body(
                    Map.of("error", "You are not a member of this group")
            );
        }

        return groupMessageRepository.findById(messageId)
                .map(message -> {
                    if (!message.getGroupId().equals(groupId)) {
                        return ResponseEntity.badRequest().body(
                                Map.of("error", "Message does not belong to this group")
                        );
                    }

                    boolean shouldPin = !message.isPinned();

                    message.setPinned(shouldPin);

                    if (shouldPin) {
                        message.setPinnedBy(currentUsername);
                        message.setPinnedAt(LocalDateTime.now());
                    } else {
                        message.setPinnedBy(null);
                        message.setPinnedAt(null);
                    }

                    groupMessageRepository.save(message);

                    messagingTemplate.convertAndSend(
                            "/topic/groups/" + groupId,
                            Map.of(
                                    "type", shouldPin ? "GROUP_MESSAGE_PINNED" : "GROUP_MESSAGE_UNPINNED",
                                    "groupId", groupId,
                                    "messageId", messageId
                            )
                    );

                    Map<String, Object> response = new java.util.HashMap<>();
                    response.put("status", shouldPin ? "Group message pinned" : "Group message unpinned");
                    response.put("groupId", groupId);
                    response.put("messageId", messageId);
                    response.put("pinned", message.isPinned());
                    response.put("pinnedBy", message.getPinnedBy());
                    response.put("pinnedAt", message.getPinnedAt());

                    return ResponseEntity.ok(response);

                })
                .orElseGet(() -> ResponseEntity.status(404).body(
                        Map.of("error", "Group message not found")
                ));
    }

    @DeleteMapping("/groups/{groupId}/messages/{messageId}")
    public ResponseEntity<?> deleteOwnGroupMessage(@PathVariable("groupId") Long groupId,
                                                   @PathVariable("messageId") Long messageId) {
        String currentUsername = org.springframework.security.core.context.SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        if (groupMemberRepository.findByGroupIdAndUsername(groupId, currentUsername).isEmpty()) {
            return ResponseEntity.status(403).body(
                    Map.of("error", "You are not a member of this group")
            );
        }

        return groupMessageRepository.findById(messageId)
                .map(message -> {
                    if (!message.getGroupId().equals(groupId)) {
                        return ResponseEntity.badRequest().body(
                                Map.of("error", "Message does not belong to this group")
                        );
                    }

                    if (!message.getSender().equals(currentUsername)) {
                        return ResponseEntity.status(403).body(
                                Map.of("error", "You can delete only your own group messages")
                        );
                    }

                    if (!attachmentRepository.findByGroupMessageId(messageId).isEmpty()) {
                        return ResponseEntity.badRequest().body(
                                Map.of("error", "Messages with attachments cannot be deleted yet")
                        );
                    }

                    groupMessageReadRepository.deleteByGroupMessageId(messageId);
                    groupMessageRepository.delete(message);

                    messagingTemplate.convertAndSend(
                            "/topic/groups/" + groupId,
                            Map.of(
                                    "type", "GROUP_MESSAGE_DELETED",
                                    "groupId", groupId,
                                    "messageId", messageId
                            )
                    );

                    return ResponseEntity.ok(
                            Map.of(
                                    "status", "Group message deleted",
                                    "groupId", groupId,
                                    "messageId", messageId
                            )
                    );
                })
                .orElseGet(() -> ResponseEntity.status(404).body(
                        Map.of("error", "Group message not found")
                ));
    }

    @GetMapping("/groups/{groupId}/messages")
    public ResponseEntity<?> getGroupMessages(@PathVariable("groupId") Long groupId) {
        String currentUsername = org.springframework.security.core.context.SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        if (groupMemberRepository.findByGroupIdAndUsername(groupId, currentUsername).isEmpty()) {
            return ResponseEntity.status(403).body(
                    Map.of("error", "You are not a member of this group")
            );
        }

        List<GroupMessageEntity> groupMessages =
                groupMessageRepository.findByGroupIdOrderByTimestampAsc(groupId);

        List<Long> groupMessageIds = groupMessages.stream()
                .map(GroupMessageEntity::getId)
                .toList();

        if (!groupMessageIds.isEmpty()) {
            java.util.Set<Long> alreadyReadMessageIds =
                    groupMessageReadRepository
                            .findByGroupMessageIdInAndUsername(groupMessageIds, currentUsername)
                            .stream()
                            .map(GroupMessageReadEntity::getGroupMessageId)
                            .collect(java.util.stream.Collectors.toSet());

            LocalDateTime readAt = LocalDateTime.now();

            List<GroupMessageReadEntity> newReadRecords = groupMessages.stream()
                    .filter(message -> !alreadyReadMessageIds.contains(message.getId()))
                    .map(message -> new GroupMessageReadEntity(
                            message.getId(),
                            groupId,
                            currentUsername,
                            readAt
                    ))
                    .toList();

            if (!newReadRecords.isEmpty()) {
                groupMessageReadRepository.saveAll(newReadRecords);
            }
        }

        List<GroupMemberEntity> currentMembers =
                groupMemberRepository.findByGroupId(groupId);

        int memberCount = currentMembers.size();

        java.util.Set<String> currentMemberUsernames =
                currentMembers.stream()
                        .map(GroupMemberEntity::getUsername)
                        .collect(java.util.stream.Collectors.toSet());

        Map<Long, Long> seenCountsByMessageId = new java.util.HashMap<>();

        if (!groupMessageIds.isEmpty()) {
            groupMessageReadRepository.findByGroupMessageIdIn(groupMessageIds)
                    .stream()
                    .filter(read ->
                            currentMemberUsernames.contains(read.getUsername())
                    )
                    .forEach(read -> seenCountsByMessageId.merge(
                            read.getGroupMessageId(),
                            1L,
                            Long::sum
                    ));
        }

        List<Map<String, Object>> messageViews = groupMessages.stream()
                .map(message -> {
                    Map<String, Object> messageView = new java.util.HashMap<>();

                    messageView.put("id", message.getId());
                    messageView.put("groupId", message.getGroupId());
                    messageView.put("sender", message.getSender());
                    messageView.put("message", message.getMessage());
                    messageView.put("timestamp", message.getTimestamp());
                    messageView.put("editedAt", message.getEditedAt());
                    messageView.put("pinned", message.isPinned());
                    messageView.put("pinnedBy", message.getPinnedBy());
                    messageView.put("pinnedAt", message.getPinnedAt());
                    messageView.put(
                            "seenCount",
                            seenCountsByMessageId.getOrDefault(message.getId(), 0L)
                    );
                    messageView.put("memberCount", memberCount);

                    return messageView;
                })
                .toList();
        
        return ResponseEntity.ok(messageViews);
    }
}
