package com.securemessaging.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "blocked_users")
public class BlockedUser {

    @EmbeddedId
    private BlockedUserId id;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public BlockedUser() {
    }

    public BlockedUser(BlockedUserId id) {
        this.id = id;
        this.createdAt = LocalDateTime.now();
    }

    public BlockedUserId getId() {
        return id;
    }

    public void setId(BlockedUserId id) {
        this.id = id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}