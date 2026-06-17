package com.securemessaging.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "attachments")
public class AttachmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sender;
    private String receiver;

    private String originalFilename;
    private String contentType;
    private Long fileSize;

    @Column(nullable = false, columnDefinition = "BYTEA")
    private byte[] encryptedFileBytes;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String ivBase64;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String encryptedKeyForReceiverBase64;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String encryptedKeyForSenderBase64;

    private LocalDateTime timestamp;

    public AttachmentEntity() {
    }

    public AttachmentEntity(String sender,
                            String receiver,
                            String originalFilename,
                            String contentType,
                            Long fileSize,
                            byte[] encryptedFileBytes,
                            String ivBase64,
                            String encryptedKeyForReceiverBase64,
                            String encryptedKeyForSenderBase64,
                            LocalDateTime timestamp) {
        this.sender = sender;
        this.receiver = receiver;
        this.originalFilename = originalFilename;
        this.contentType = contentType;
        this.fileSize = fileSize;
        this.encryptedFileBytes = encryptedFileBytes;
        this.ivBase64 = ivBase64;
        this.encryptedKeyForReceiverBase64 = encryptedKeyForReceiverBase64;
        this.encryptedKeyForSenderBase64 = encryptedKeyForSenderBase64;
        this.timestamp = timestamp;
    }

    public Long getId() { return id; }
    public String getSender() { return sender; }
    public String getReceiver() { return receiver; }
    public String getOriginalFilename() { return originalFilename; }
    public String getContentType() { return contentType; }
    public Long getFileSize() { return fileSize; }
    public byte[] getEncryptedFileBytes() { return encryptedFileBytes; }
    public String getIvBase64() { return ivBase64; }
    public String getEncryptedKeyForReceiverBase64() { return encryptedKeyForReceiverBase64; }
    public String getEncryptedKeyForSenderBase64() { return encryptedKeyForSenderBase64; }
    public LocalDateTime getTimestamp() { return timestamp; }
}