package com.securemessaging.controller;

import com.securemessaging.entity.AttachmentEntity;
import com.securemessaging.service.AttachmentService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/attachments", "/api/attachments"})
@CrossOrigin(origins = "*")
public class AttachmentController {

    private final AttachmentService attachmentService;

    public AttachmentController(AttachmentService attachmentService) {
        this.attachmentService = attachmentService;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadAttachment(@RequestParam("receiver") String receiver,
                                              @RequestParam("file") MultipartFile file) throws Exception {
        String sender = org.springframework.security.core.context.SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "File is empty")
            );
        }

        AttachmentEntity savedAttachment =
                attachmentService.saveEncryptedAttachment(sender, receiver, file);

        return ResponseEntity.ok(
                Map.of(
                        "status", "Encrypted attachment uploaded",
                        "attachmentId", savedAttachment.getId(),
                        "filename", savedAttachment.getOriginalFilename(),
                        "sender", savedAttachment.getSender(),
                        "receiver", savedAttachment.getReceiver()
                )
        );
    }

    @GetMapping("/inbox")
    public ResponseEntity<?> inboxAttachments() {
        String receiver = org.springframework.security.core.context.SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        List<Map<String, Object>> attachments = attachmentService.findInbox(receiver)
                .stream()
                .map(attachment -> {
                    Map<String, Object> item = new java.util.LinkedHashMap<>();
                    item.put("id", attachment.getId());
                    item.put("sender", attachment.getSender());
                    item.put("receiver", attachment.getReceiver());
                    item.put("filename", attachment.getOriginalFilename());
                    item.put("contentType", attachment.getContentType() != null
                            ? attachment.getContentType()
                            : "application/octet-stream");
                    item.put("fileSize", attachment.getFileSize());
                    item.put("timestamp", attachment.getTimestamp());
                    return item;
                })
                .toList();

        return ResponseEntity.ok(attachments);
    }
    @GetMapping("/{attachmentId}/download")
    public ResponseEntity<byte[]> downloadAttachment(@PathVariable Long attachmentId) throws Exception {
        String currentUsername = org.springframework.security.core.context.SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        AttachmentEntity attachment = attachmentService.findById(attachmentId);
        byte[] decryptedBytes =
                attachmentService.decryptAttachmentForUser(attachmentId, currentUsername);

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
