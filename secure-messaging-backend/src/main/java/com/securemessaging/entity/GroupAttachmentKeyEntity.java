package com.securemessaging.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "group_attachment_keys")
public class GroupAttachmentKeyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "attachment_id", nullable = false)
    private Long attachmentId;

    @Column(nullable = false)
    private String username;

    @Column(name = "encrypted_key_base64", nullable = false, columnDefinition = "TEXT")
    private String encryptedKeyBase64;

    public GroupAttachmentKeyEntity() {
    }

    public GroupAttachmentKeyEntity(Long attachmentId,
                                    String username,
                                    String encryptedKeyBase64) {
        this.attachmentId = attachmentId;
        this.username = username;
        this.encryptedKeyBase64 = encryptedKeyBase64;
    }

    public Long getId() {
        return id;
    }

    public Long getAttachmentId() {
        return attachmentId;
    }

    public String getUsername() {
        return username;
    }

    public String getEncryptedKeyBase64() {
        return encryptedKeyBase64;
    }
}