package com.securemessaging.controller;

import com.securemessaging.entity.AttachmentEntity;
import com.securemessaging.entity.GroupMessageEntity;
import com.securemessaging.repository.GroupMemberEntityRepository;
import com.securemessaging.repository.GroupMessageEntityRepository;
import com.securemessaging.service.AttachmentService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping({"/groups", "/api/groups"})
@CrossOrigin(origins = "*")
public class GroupAttachmentController {

    private final AttachmentService attachmentService;
    private final GroupMemberEntityRepository groupMemberRepository;
    private final GroupMessageEntityRepository groupMessageRepository;

    public GroupAttachmentController(AttachmentService attachmentService,
                                     GroupMemberEntityRepository groupMemberRepository,
                                     GroupMessageEntityRepository groupMessageRepository) {
        this.attachmentService = attachmentService;
        this.groupMemberRepository = groupMemberRepository;
        this.groupMessageRepository = groupMessageRepository;
    }

    @PostMapping("/{groupId}/attachments/upload")
    public ResponseEntity<?> uploadGroupAttachment(@PathVariable("groupId") Long groupId,
                                                   @RequestParam("groupMessageId") Long groupMessageId,
                                                   @RequestParam("file") MultipartFile file) throws Exception {
        String currentUsername = org.springframework.security.core.context.SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "File is empty")
            );
        }

        if (groupMemberRepository.findByGroupIdAndUsername(groupId, currentUsername).isEmpty()) {
            return ResponseEntity.status(403).body(
                    Map.of("error", "You are not a member of this group")
            );
        }

        GroupMessageEntity groupMessage = groupMessageRepository.findById(groupMessageId)
                .orElse(null);

        if (groupMessage == null) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Group message not found")
            );
        }

        if (!groupMessage.getGroupId().equals(groupId)) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Group message does not belong to this group")
            );
        }

        AttachmentEntity savedAttachment =
                attachmentService.saveEncryptedGroupAttachment(
                        currentUsername,
                        groupId,
                        groupMessageId,
                        file
                );

        return ResponseEntity.ok(
                Map.of(
                        "status", "Encrypted group attachment uploaded",
                        "attachmentId", savedAttachment.getId(),
                        "filename", savedAttachment.getOriginalFilename(),
                        "sender", savedAttachment.getSender(),
                        "groupId", savedAttachment.getGroupId(),
                        "groupMessageId", savedAttachment.getGroupMessageId()
                )
        );
    }

    @GetMapping("/{groupId}/attachments")
    public ResponseEntity<?> getGroupAttachments(@PathVariable("groupId") Long groupId) {
        String currentUsername = org.springframework.security.core.context.SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        if (groupMemberRepository.findByGroupIdAndUsername(groupId, currentUsername).isEmpty()) {
            return ResponseEntity.status(403).body(
                    Map.of("error", "You are not a member of this group")
            );
        }

        return ResponseEntity.ok(
                attachmentService.findGroupAttachments(groupId)
                        .stream()
                        .map(attachment -> {
                            Map<String, Object> item = new java.util.LinkedHashMap<>();
                            item.put("id", attachment.getId());
                            item.put("groupId", attachment.getGroupId());
                            item.put("groupMessageId", attachment.getGroupMessageId());
                            item.put("filename", attachment.getOriginalFilename());
                            item.put("sender", attachment.getSender());
                            item.put("contentType", attachment.getContentType() != null
                                    ? attachment.getContentType()
                                    : "application/octet-stream");
                            item.put("fileSize", attachment.getFileSize());
                            item.put("timestamp", attachment.getTimestamp());
                            return item;
                        })
                        .toList()
        );
    }

    @GetMapping("/{groupId}/attachments/{attachmentId}/download")
    public ResponseEntity<?> downloadGroupAttachment(@PathVariable("groupId") Long groupId,
                                                     @PathVariable("attachmentId") Long attachmentId) throws Exception {
        String currentUsername = org.springframework.security.core.context.SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        if (groupMemberRepository.findByGroupIdAndUsername(groupId, currentUsername).isEmpty()) {
            return ResponseEntity.status(403).body(
                    Map.of("error", "You are not a member of this group")
            );
        }

        AttachmentEntity attachment = attachmentService.findById(attachmentId);

        if (attachment.getGroupId() == null || !attachment.getGroupId().equals(groupId)) {
            return ResponseEntity.status(404).body(
                    Map.of("error", "Group attachment not found")
            );
        }

        if (!currentUsername.equals(attachment.getSender())) {
            return ResponseEntity.status(403).body(
                    Map.of("error", "Group attachment download is currently available to the sender only")
            );
        }

        byte[] decryptedBytes = attachmentService.decryptAttachmentForUser(attachmentId, currentUsername);

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + attachment.getOriginalFilename() + "\""
                )
                .contentType(MediaType.parseMediaType(
                        attachment.getContentType() != null
                                ? attachment.getContentType()
                                : "application/octet-stream"
                ))
                .body(decryptedBytes);
    }

}

