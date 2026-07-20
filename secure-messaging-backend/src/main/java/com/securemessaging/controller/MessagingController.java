package com.securemessaging.controller;

import com.securemessaging.dto.CreateGroupDecisionRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import java.time.LocalDateTime;

import com.securemessaging.entity.AttachmentEntity;
import com.securemessaging.entity.EmailGroupInvitationEntity;
import com.securemessaging.entity.EmailGroupInvitationStatus;
import com.securemessaging.entity.GroupEntity;
import com.securemessaging.entity.GroupDecisionEntity;
import com.securemessaging.entity.GroupDecisionGovernanceMode;
import com.securemessaging.entity.GroupInvitationEntity;
import com.securemessaging.entity.GroupInvitationStatus;
import com.securemessaging.entity.GroupMemberEntity;
import com.securemessaging.entity.GroupRole;
import com.securemessaging.entity.GroupMessageEntity;
import com.securemessaging.entity.GroupMessageReadEntity;
import com.securemessaging.repository.AttachmentRepository;
import com.securemessaging.repository.EmailGroupInvitationRepository;
import com.securemessaging.repository.GroupAttachmentKeyRepository;
import com.securemessaging.repository.GroupEntityRepository;
import com.securemessaging.repository.GroupInvitationRepository;
import com.securemessaging.repository.GroupMemberEntityRepository;
import com.securemessaging.repository.GroupMessageEntityRepository;
import com.securemessaging.repository.GroupMessageReadRepository;
import com.securemessaging.repository.UserEntityRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.securemessaging.entity.EncryptedMessageEntity;
import com.securemessaging.mapper.EncryptedMessageMapper;
import com.securemessaging.core.SecureMessagingSystem.EncryptedMessage;
import com.securemessaging.core.SecureMessagingSystem.DecryptedMessageView;
import com.securemessaging.core.SecureMessagingSystem;
import com.securemessaging.service.DatabaseUserService;
import com.securemessaging.service.DatabaseMessagingService;
import com.securemessaging.service.InvitationTokenService;
import com.securemessaging.service.GroupDecisionService;
import org.springframework.http.HttpStatus;
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
    private final GroupInvitationRepository groupInvitationRepository;
    private final EmailGroupInvitationRepository emailGroupInvitationRepository;
    private final GroupMemberEntityRepository groupMemberRepository;
    private final GroupMessageEntityRepository groupMessageRepository;
    private final GroupMessageReadRepository groupMessageReadRepository;
    private final AttachmentRepository attachmentRepository;
    private final GroupAttachmentKeyRepository groupAttachmentKeyRepository;
    private final UserEntityRepository userEntityRepository;
    private final InvitationTokenService invitationTokenService;
    private final GroupDecisionService groupDecisionService;
    private final SimpMessagingTemplate messagingTemplate;

    public MessagingController(DatabaseMessagingService databaseMessagingService,
                               DatabaseUserService databaseUserService,
                               GroupEntityRepository groupRepository,
                               GroupInvitationRepository groupInvitationRepository,
                               EmailGroupInvitationRepository emailGroupInvitationRepository,
                               GroupMemberEntityRepository groupMemberRepository,
                               GroupMessageEntityRepository groupMessageRepository,
                               GroupMessageReadRepository groupMessageReadRepository,
                               AttachmentRepository attachmentRepository,
                               GroupAttachmentKeyRepository groupAttachmentKeyRepository,
                               UserEntityRepository userEntityRepository,
                               InvitationTokenService invitationTokenService,
                               GroupDecisionService groupDecisionService,
                               SimpMessagingTemplate messagingTemplate) {
        this.databaseMessagingService = databaseMessagingService;
        this.databaseUserService = databaseUserService;
        this.groupRepository = groupRepository;
        this.groupInvitationRepository = groupInvitationRepository;
        this.emailGroupInvitationRepository =
                emailGroupInvitationRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.groupMessageRepository = groupMessageRepository;
        this.groupMessageReadRepository = groupMessageReadRepository;
        this.attachmentRepository = attachmentRepository;
        this.groupAttachmentKeyRepository = groupAttachmentKeyRepository;
        this.userEntityRepository = userEntityRepository;
        this.invitationTokenService = invitationTokenService;
        this.groupDecisionService = groupDecisionService;
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
            new GroupMemberEntity(
                    savedGroup.getId(),
                    currentUsername,
                    GroupRole.OWNER
            )
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
    List<Map<String, Object>> groups = new ArrayList<>();

    for (GroupMemberEntity membership : memberships) {
        groupRepository.findById(membership.getGroupId())
                .ifPresent(group -> {
                    long unreadCount =
                            groupMessageReadRepository.countUnreadByGroupIdAndUsername(
                                    group.getId(),
                                    currentUsername
                            );

                    Map<String, Object> groupView = new java.util.HashMap<>();

                    groupView.put("id", group.getId());
                    groupView.put("groupName", group.getGroupName());
                    groupView.put("createdBy", group.getCreatedBy());
                    groupView.put("unreadCount", unreadCount);

                    groups.add(groupView);
                });
    }

    return ResponseEntity.ok(groups);
}

    @GetMapping("/groups/{groupId}/members")
    public ResponseEntity<?> groupMembers(
            @PathVariable("groupId") Long groupId) {

        List<GroupMemberEntity> members =
                groupMemberRepository.findByGroupId(groupId);

        List<Map<String, String>> memberDetails = members.stream()
                .map(member -> Map.of(
                        "username", member.getUsername(),
                        "role", member.getRole().name()
                ))
                .toList();

        return ResponseEntity.ok(memberDetails);
    }

    @PostMapping("/groups/{groupId}/join")
    public ResponseEntity<?> joinGroup(@PathVariable("groupId") Long groupId) {
        if (groupRepository.findById(groupId).isEmpty()) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Group not found")
            );
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                Map.of(
                        "error",
                        "Direct group joining is disabled. You must accept a group invitation."
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

    @PutMapping("/groups/{groupId}/members/{username}/promote")
    public ResponseEntity<?> promoteGroupMember(
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
                    Map.of("error", "Only the group owner can promote members")
            );
        }

        if (group.getCreatedBy().equals(username)) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "The group owner already has the highest role")
            );
        }

        GroupMemberEntity member =
                groupMemberRepository
                        .findByGroupIdAndUsername(groupId, username)
                        .orElse(null);

        if (member == null) {
            return ResponseEntity.status(404).body(
                    Map.of("error", "Group member not found")
            );
        }

        if (member.getRole() == GroupRole.ADMIN) {
            return ResponseEntity.ok(
                    Map.of(
                            "status", username + " is already a group admin",
                            "groupId", groupId,
                            "username", username,
                            "role", GroupRole.ADMIN.name()
                    )
            );
        }

        member.setRole(GroupRole.ADMIN);
        groupMemberRepository.save(member);

        messagingTemplate.convertAndSend(
                "/topic/groups/" + groupId,
                Map.of(
                        "type", "GROUP_MEMBER_ROLE_CHANGED",
                        "groupId", groupId,
                        "username", username,
                        "role", GroupRole.ADMIN.name()
                )
        );

        return ResponseEntity.ok(
                Map.of(
                        "status", username + " was promoted to group admin",
                        "groupId", groupId,
                        "username", username,
                        "role", GroupRole.ADMIN.name()
                )
        );
    }

    @PutMapping("/groups/{groupId}/members/{username}/demote")
    public ResponseEntity<?> demoteGroupAdmin(
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
                    Map.of("error", "Only the group owner can demote admins")
            );
        }

        if (group.getCreatedBy().equals(username)) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "The group owner cannot be demoted")
            );
        }

        GroupMemberEntity member =
                groupMemberRepository
                        .findByGroupIdAndUsername(groupId, username)
                        .orElse(null);

        if (member == null) {
            return ResponseEntity.status(404).body(
                    Map.of("error", "Group member not found")
            );
        }

        if (member.getRole() == GroupRole.MEMBER) {
            return ResponseEntity.ok(
                    Map.of(
                            "status", username + " is already a regular member",
                            "groupId", groupId,
                            "username", username,
                            "role", GroupRole.MEMBER.name()
                    )
            );
        }

        member.setRole(GroupRole.MEMBER);
        groupMemberRepository.save(member);

        messagingTemplate.convertAndSend(
                "/topic/groups/" + groupId,
                Map.of(
                        "type", "GROUP_MEMBER_ROLE_CHANGED",
                        "groupId", groupId,
                        "username", username,
                        "role", GroupRole.MEMBER.name()
                )
        );

        return ResponseEntity.ok(
                Map.of(
                        "status", username + " was demoted to regular member",
                        "groupId", groupId,
                        "username", username,
                        "role", GroupRole.MEMBER.name()
                )
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

        GroupMemberEntity currentMember =
                groupMemberRepository
                        .findByGroupIdAndUsername(groupId, currentUsername)
                        .orElse(null);

        if (currentMember == null) {
            return ResponseEntity.status(403).body(
                    Map.of("error", "You are not a member of this group")
            );
        }

        boolean isOwner = currentMember.getRole() == GroupRole.OWNER;
        boolean isAdmin = currentMember.getRole() == GroupRole.ADMIN;

        if (!isOwner && !isAdmin) {
            return ResponseEntity.status(403).body(
                    Map.of(
                            "error",
                            "Only the group owner or an admin can remove members"
                    )
            );
        }

        GroupMemberEntity targetMember =
                groupMemberRepository
                        .findByGroupIdAndUsername(groupId, username)
                        .orElse(null);

        if (targetMember == null) {
            return ResponseEntity.status(404).body(
                    Map.of("error", "Group member not found")
            );
        }

        if (targetMember.getRole() == GroupRole.OWNER) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "The group owner cannot be removed")
            );
        }

        if (isAdmin && targetMember.getRole() == GroupRole.ADMIN) {
            return ResponseEntity.status(403).body(
                    Map.of("error", "An admin cannot remove another admin")
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

    @PostMapping("/groups/{groupId}/invitations")
    public ResponseEntity<?> inviteRegisteredUserToGroup(
            @PathVariable("groupId") Long groupId,
            @RequestBody Map<String, String> request) {

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

        GroupMemberEntity currentMember =
                groupMemberRepository
                        .findByGroupIdAndUsername(groupId, currentUsername)
                        .orElse(null);

        if (currentMember == null) {
            return ResponseEntity.status(403).body(
                    Map.of("error", "You are not a member of this group")
            );
        }

        if (
                currentMember.getRole() != GroupRole.OWNER &&
                        currentMember.getRole() != GroupRole.ADMIN
        ) {
            return ResponseEntity.status(403).body(
                    Map.of(
                            "error",
                            "Only the group owner or an admin can invite users"
                    )
            );
        }

        String invitedUsername = request.get("username");

        if (invitedUsername == null || invitedUsername.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Username is required")
            );
        }

        invitedUsername = invitedUsername.trim();

        if (invitedUsername.equals(currentUsername)) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "You cannot invite yourself")
            );
        }

        if (userEntityRepository.findByUsername(invitedUsername).isEmpty()) {
            return ResponseEntity.status(404).body(
                    Map.of("error", "Registered user not found")
            );
        }

        if (
                groupMemberRepository
                        .findByGroupIdAndUsername(groupId, invitedUsername)
                        .isPresent()
        ) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "User is already a group member")
            );
        }

        GroupInvitationEntity existingInvitation =
                groupInvitationRepository
                        .findByGroupIdAndInvitedUsername(
                                groupId,
                                invitedUsername
                        )
                        .orElse(null);

        if (
                existingInvitation != null &&
                        existingInvitation.getStatus() == GroupInvitationStatus.PENDING
        ) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "A pending invitation already exists")
            );
        }

        GroupInvitationEntity invitation;

        if (existingInvitation == null) {
            invitation = new GroupInvitationEntity(
                    groupId,
                    invitedUsername,
                    currentUsername,
                    GroupInvitationStatus.PENDING,
                    LocalDateTime.now()
            );
        } else {
            invitation = existingInvitation;
            invitation.setInvitedBy(currentUsername);
            invitation.setStatus(GroupInvitationStatus.PENDING);
            invitation.setCreatedAt(LocalDateTime.now());
            invitation.setRespondedAt(null);
        }

        GroupInvitationEntity savedInvitation =
                groupInvitationRepository.save(invitation);

        return ResponseEntity.ok(
                Map.of(
                        "status", "Group invitation sent",
                        "invitationId", savedInvitation.getId(),
                        "groupId", savedInvitation.getGroupId(),
                        "groupName", group.getGroupName(),
                        "invitedUsername", savedInvitation.getInvitedUsername(),
                        "invitedBy", savedInvitation.getInvitedBy(),
                        "invitationStatus", savedInvitation.getStatus().name()
                )
        );
    }

    @PostMapping("/groups/{groupId}/email-invitations")
    public ResponseEntity<?> inviteUnregisteredUserByEmail(
            @PathVariable("groupId") Long groupId,
            @RequestBody Map<String, String> request) {

        String currentUsername =
                org.springframework.security.core.context
                        .SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getName();

        GroupEntity group =
                groupRepository.findById(groupId).orElse(null);

        if (group == null) {
            return ResponseEntity.status(404).body(
                    Map.of("error", "Group not found")
            );
        }

        GroupMemberEntity currentMember =
                groupMemberRepository
                        .findByGroupIdAndUsername(
                                groupId,
                                currentUsername
                        )
                        .orElse(null);

        if (currentMember == null) {
            return ResponseEntity.status(403).body(
                    Map.of(
                            "error",
                            "You are not a member of this group"
                    )
            );
        }

        if (
                currentMember.getRole() != GroupRole.OWNER &&
                        currentMember.getRole() != GroupRole.ADMIN
        ) {
            return ResponseEntity.status(403).body(
                    Map.of(
                            "error",
                            "Only the group owner or an admin can invite users"
                    )
            );
        }

        String invitedEmail = request.get("email");

        if (invitedEmail == null || invitedEmail.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Email is required")
            );
        }

        invitedEmail =
                invitedEmail
                        .trim()
                        .toLowerCase(Locale.ROOT);

        if (!invitedEmail.matches(
                "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
        )) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Invalid email address")
            );
        }

        if (
                userEntityRepository
                        .findByEmailIgnoreCase(invitedEmail)
                        .isPresent()
        ) {
            return ResponseEntity.badRequest().body(
                    Map.of(
                            "error",
                            "This email belongs to a registered user. Invite the user by username."
                    )
            );
        }

        EmailGroupInvitationEntity existingInvitation =
                emailGroupInvitationRepository
                        .findByGroupIdAndInvitedEmailIgnoreCase(
                                groupId,
                                invitedEmail
                        )
                        .orElse(null);

        LocalDateTime now = LocalDateTime.now();

        if (
                existingInvitation != null &&
                        existingInvitation.getStatus() ==
                                EmailGroupInvitationStatus.PENDING &&
                        existingInvitation.getExpiresAt().isAfter(now)
        ) {
            return ResponseEntity.badRequest().body(
                    Map.of(
                            "error",
                            "A pending email invitation already exists"
                    )
            );
        }

        String rawToken =
                invitationTokenService.generateToken();

        String tokenHash =
                invitationTokenService.hashToken(rawToken);

        LocalDateTime expiresAt = now.plusDays(7);

        EmailGroupInvitationEntity invitation;

        if (existingInvitation == null) {
            invitation = new EmailGroupInvitationEntity(
                    groupId,
                    invitedEmail,
                    currentUsername,
                    tokenHash,
                    EmailGroupInvitationStatus.PENDING,
                    now,
                    expiresAt
            );
        } else {
            invitation = existingInvitation;
            invitation.setInvitedBy(currentUsername);
            invitation.setTokenHash(tokenHash);
            invitation.setStatus(
                    EmailGroupInvitationStatus.PENDING
            );
            invitation.setCreatedAt(now);
            invitation.setExpiresAt(expiresAt);
            invitation.setUsedAt(null);
            invitation.setRegisteredUsername(null);
        }

        EmailGroupInvitationEntity savedInvitation =
                emailGroupInvitationRepository.save(invitation);

        String registrationLink =
                "https://brain-secure-messaging.com/register" +
                        "?invitationToken=" + rawToken;

        return ResponseEntity.ok(
                Map.of(
                        "status",
                        "Email group invitation created",
                        "invitationId",
                        savedInvitation.getId(),
                        "groupId",
                        savedInvitation.getGroupId(),
                        "groupName",
                        group.getGroupName(),
                        "invitedEmail",
                        savedInvitation.getInvitedEmail(),
                        "expiresAt",
                        savedInvitation.getExpiresAt(),
                        "registrationLink",
                        registrationLink
                )
        );
    }

    @GetMapping("/groups/invitations/pending")
    public ResponseEntity<?> pendingGroupInvitations() {

        String currentUsername = org.springframework.security.core.context.SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        List<GroupInvitationEntity> invitations =
                groupInvitationRepository
                        .findByInvitedUsernameAndStatusOrderByCreatedAtDesc(
                                currentUsername,
                                GroupInvitationStatus.PENDING
                        );

        List<Map<String, Object>> invitationDetails =
                new ArrayList<>();

        for (GroupInvitationEntity invitation : invitations) {

            GroupEntity group =
                    groupRepository
                            .findById(invitation.getGroupId())
                            .orElse(null);

            if (group == null) {
                continue;
            }

            invitationDetails.add(
                    Map.of(
                            "invitationId", invitation.getId(),
                            "groupId", invitation.getGroupId(),
                            "groupName", group.getGroupName(),
                            "invitedBy", invitation.getInvitedBy(),
                            "invitationStatus", invitation.getStatus().name(),
                            "createdAt", invitation.getCreatedAt()
                    )
            );
        }

        return ResponseEntity.ok(invitationDetails);
    }

    @Transactional
    @PostMapping("/groups/invitations/{invitationId}/accept")
    public ResponseEntity<?> acceptGroupInvitation(
            @PathVariable("invitationId") Long invitationId) {

        String currentUsername = org.springframework.security.core.context.SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        GroupInvitationEntity invitation =
                groupInvitationRepository
                        .findById(invitationId)
                        .orElse(null);

        if (invitation == null) {
            return ResponseEntity.status(404).body(
                    Map.of("error", "Group invitation not found")
            );
        }

        if (!invitation.getInvitedUsername().equals(currentUsername)) {
            return ResponseEntity.status(403).body(
                    Map.of("error", "This invitation does not belong to you")
            );
        }

        if (invitation.getStatus() != GroupInvitationStatus.PENDING) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "This invitation is no longer pending")
            );
        }

        GroupEntity group =
                groupRepository
                        .findById(invitation.getGroupId())
                        .orElse(null);

        if (group == null) {
            return ResponseEntity.status(404).body(
                    Map.of("error", "Group not found")
            );
        }

        boolean alreadyMember =
                groupMemberRepository
                        .findByGroupIdAndUsername(
                                invitation.getGroupId(),
                                currentUsername
                        )
                        .isPresent();

        if (!alreadyMember) {
            groupMemberRepository.save(
                    new GroupMemberEntity(
                            invitation.getGroupId(),
                            currentUsername,
                            GroupRole.MEMBER
                    )
            );
        }

        invitation.setStatus(GroupInvitationStatus.ACCEPTED);
        invitation.setRespondedAt(LocalDateTime.now());

        groupInvitationRepository.save(invitation);

        return ResponseEntity.ok(
                Map.of(
                        "status",
                        alreadyMember
                                ? "Invitation accepted; already a group member"
                                : "Group invitation accepted",
                        "invitationId", invitation.getId(),
                        "groupId", invitation.getGroupId(),
                        "groupName", group.getGroupName(),
                        "username", currentUsername,
                        "role", GroupRole.MEMBER.name(),
                        "invitationStatus", invitation.getStatus().name()
                )
        );
    }

    @PostMapping("/groups/invitations/{invitationId}/decline")
    public ResponseEntity<?> declineGroupInvitation(
            @PathVariable("invitationId") Long invitationId) {

        String currentUsername = org.springframework.security.core.context.SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        GroupInvitationEntity invitation =
                groupInvitationRepository
                        .findById(invitationId)
                        .orElse(null);

        if (invitation == null) {
            return ResponseEntity.status(404).body(
                    Map.of("error", "Group invitation not found")
            );
        }

        if (!invitation.getInvitedUsername().equals(currentUsername)) {
            return ResponseEntity.status(403).body(
                    Map.of("error", "This invitation does not belong to you")
            );
        }

        if (invitation.getStatus() != GroupInvitationStatus.PENDING) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "This invitation is no longer pending")
            );
        }

        GroupEntity group =
                groupRepository
                        .findById(invitation.getGroupId())
                        .orElse(null);

        if (group == null) {
            return ResponseEntity.status(404).body(
                    Map.of("error", "Group not found")
            );
        }

        invitation.setStatus(GroupInvitationStatus.DECLINED);
        invitation.setRespondedAt(LocalDateTime.now());

        groupInvitationRepository.save(invitation);

        return ResponseEntity.ok(
                Map.of(
                        "status", "Group invitation declined",
                        "invitationId", invitation.getId(),
                        "groupId", invitation.getGroupId(),
                        "groupName", group.getGroupName(),
                        "username", currentUsername,
                        "invitationStatus", invitation.getStatus().name()
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
        groupInvitationRepository.deleteByGroupId(groupId);
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
    private void markGroupMessageAsRead(
            Long groupId,
            Long groupMessageId,
            String username) {

        groupMessageReadRepository.insertReadIfAbsent(
                groupMessageId,
                groupId,
                username,
                LocalDateTime.now()
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

    @GetMapping("/groups/{groupId}/decisions")
    public ResponseEntity<?> getGroupDecisions(
            @PathVariable("groupId") Long groupId) {

        String currentUsername =
                org.springframework.security.core.context
                        .SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getName();

        try {
            List<Map<String, Object>> decisions =
                    groupDecisionService
                            .getGroupDecisions(
                                    groupId,
                                    currentUsername
                            )
                            .stream()
                            .map(decision -> {
                                Map<String, Object> item =
                                        new java.util.LinkedHashMap<>();

                                item.put(
                                        "decisionId",
                                        decision.getId()
                                );
                                item.put(
                                        "groupId",
                                        decision.getGroupId()
                                );
                                item.put(
                                        "sourceMessageId",
                                        decision.getSourceMessageId()
                                );
                                item.put(
                                        "sourceSender",
                                        decision.getSourceSender()
                                );
                                item.put(
                                        "decisionText",
                                        decision.getDecisionTextSnapshot()
                                );
                                item.put(
                                        "createdBy",
                                        decision.getCreatedBy()
                                );
                                item.put(
                                        "governanceMode",
                                        decision.getGovernanceMode()
                                );
                                item.put(
                                        "status",
                                        decision.getStatus()
                                );
                                item.put(
                                        "category",
                                        decision.getCategory()
                                );
                                item.put(
                                        "threshold",
                                        decision.getThreshold()
                                );
                                item.put(
                                        "votingDeadline",
                                        decision.getVotingDeadline()
                                );
                                item.put(
                                        "tieBreakDeadline",
                                        decision.getTieBreakDeadline()
                                );
                                item.put(
                                        "createdAt",
                                        decision.getCreatedAt()
                                );

                                return item;
                            })
                            .toList();

            return ResponseEntity.ok(decisions);

        } catch (RuntimeException exception) {

            String errorMessage =
                    exception.getMessage() == null
                            ? "Unable to load group decisions"
                            : exception.getMessage();

            if (
                    errorMessage.equals(
                            "You are not a member of this group"
                    )
            ) {
                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body(
                                Map.of(
                                        "error",
                                        errorMessage
                                )
                        );
            }

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "error",
                                    errorMessage
                            )
                    );
        }
    }


    @PostMapping("/groups/{groupId}/messages/{messageId}/decision")
    public ResponseEntity<?> createGroupDecision(
            @PathVariable("groupId") Long groupId,
            @PathVariable("messageId") Long messageId,
            @RequestBody(required = false)
            CreateGroupDecisionRequest request) {

        String currentUsername =
                org.springframework.security.core.context
                        .SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getName();

        try {
            GroupDecisionGovernanceMode governanceMode =
                    request == null ||
                            request.governanceMode() == null
                            ? GroupDecisionGovernanceMode.OWNER_REVIEW
                            : request.governanceMode();

            GroupDecisionEntity decision =
                    groupDecisionService.createDecision(
                            groupId,
                            messageId,
                            currentUsername,
                            governanceMode
                    );

            return ResponseEntity.ok(
                    Map.of(
                            "status", "Group decision created",
                            "decisionId", decision.getId(),
                            "groupId", decision.getGroupId(),
                            "sourceMessageId",
                            decision.getSourceMessageId(),
                            "sourceSender",
                            decision.getSourceSender(),
                            "decisionText",
                            decision.getDecisionTextSnapshot(),
                            "createdBy",
                            decision.getCreatedBy(),
                            "governanceMode",
                            decision.getGovernanceMode(),
                            "createdAt",
                            decision.getCreatedAt()
                    )
            );

        } catch (RuntimeException exception) {

            String errorMessage =
                    exception.getMessage() == null
                            ? "Unable to create group decision"
                            : exception.getMessage();

            if (
                    errorMessage.equals(
                            "You are not a member of this group"
                    ) ||
                            errorMessage.equals(
                                    "Only the group owner can select owner-led governance"
                            )
            ) {
                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body(
                                Map.of(
                                        "error",
                                        errorMessage
                                )
                        );
            }

            if (errorMessage.equals("Group message not found")) {
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(
                                Map.of(
                                        "error",
                                        errorMessage
                                )
                        );
            }

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "error",
                                    errorMessage
                            )
                    );
        }
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
            LocalDateTime readAt = LocalDateTime.now();

            for (GroupMessageEntity message : groupMessages) {
                groupMessageReadRepository.insertReadIfAbsent(
                        message.getId(),
                        groupId,
                        currentUsername,
                        readAt
                );
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

    @PostMapping("/groups/{groupId}/typing")
    public ResponseEntity<?> updateGroupTypingStatus(
            @PathVariable("groupId") Long groupId,
            @RequestBody Map<String, Boolean> request) {

        String currentUsername =
                org.springframework.security.core.context.SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getName();

        if (
                groupMemberRepository
                        .findByGroupIdAndUsername(
                                groupId,
                                currentUsername
                        )
                        .isEmpty()
        ) {
            return ResponseEntity.status(403).body(
                    Map.of(
                            "error",
                            "You are not a member of this group"
                    )
            );
        }

        boolean typing =
                Boolean.TRUE.equals(request.get("typing"));

        messagingTemplate.convertAndSend(
                "/topic/groups/" + groupId,
                Map.of(
                        "type", "GROUP_TYPING_STATUS",
                        "groupId", groupId,
                        "username", currentUsername,
                        "typing", typing
                )
        );

        return ResponseEntity.ok(
                Map.of(
                        "status",
                        "Group typing status updated",
                        "groupId", groupId,
                        "username", currentUsername,
                        "typing", typing
                )
        );
    }
}
