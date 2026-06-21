package com.securemessaging.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "encrypted_messages")

public class EncryptedMessageEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sender;
    private String receiver;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String encryptedPayloadBase64;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String encryptedSessionKeyBase64;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String ivBase64;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String digitalSignatureBase64;

    private LocalDateTime timestamp;
    private boolean readByReceiver = false;

    public EncryptedMessageEntity() {
    }

    public EncryptedMessageEntity(String sender, String receiver, String encryptedPayloadBase64,
                                  String encryptedSessionKeyBase64, String ivBase64,
                                  String digitalSignatureBase64, LocalDateTime timestamp) {
        this.sender = sender;
        this.receiver = receiver;
        this.encryptedPayloadBase64 = encryptedPayloadBase64;
        this.encryptedSessionKeyBase64 = encryptedSessionKeyBase64;
        this.ivBase64 = ivBase64;
        this.digitalSignatureBase64 = digitalSignatureBase64;
        this.timestamp = timestamp;
    }

    public Long getId() { return id; }
    public String getSender() { return sender; }
    public String getReceiver() { return receiver; }
    public String getEncryptedPayloadBase64() { return encryptedPayloadBase64; }
    public String getEncryptedSessionKeyBase64() { return encryptedSessionKeyBase64; }
    public String getIvBase64() { return ivBase64; }
    public String getDigitalSignatureBase64() { return digitalSignatureBase64; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public boolean isReadByReceiver() {
        return readByReceiver;
    }
    public void setReadByReceiver(boolean readByReceiver) {
        this.readByReceiver = readByReceiver;
    }
}
