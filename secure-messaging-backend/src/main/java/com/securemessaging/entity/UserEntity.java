package com.securemessaging.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")

public class UserEntity {
    @Id
    private String username;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String publicKeyBase64;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String privateKeyBase64;

    private LocalDateTime createdAt;

    public UserEntity() {
    }

    public UserEntity(String username, String passwordHash, String publicKeyBase64, String privateKeyBase64, LocalDateTime createdAt) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.publicKeyBase64 = publicKeyBase64;
        this.privateKeyBase64 = privateKeyBase64;
        this.createdAt = createdAt;
    }

    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public String getPublicKeyBase64() { return publicKeyBase64; }
    public String getPrivateKeyBase64() { return privateKeyBase64; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}

