package com.securemessaging.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class BlockedUserId implements Serializable {

    @Column(name = "blocker_username")
    private String blockerUsername;

    @Column(name = "blocked_username")
    private String blockedUsername;

    public BlockedUserId() {
    }

    public BlockedUserId(String blockerUsername, String blockedUsername) {
        this.blockerUsername = blockerUsername;
        this.blockedUsername = blockedUsername;
    }

    public String getBlockerUsername() {
        return blockerUsername;
    }

    public void setBlockerUsername(String blockerUsername) {
        this.blockerUsername = blockerUsername;
    }

    public String getBlockedUsername() {
        return blockedUsername;
    }

    public void setBlockedUsername(String blockedUsername) {
        this.blockedUsername = blockedUsername;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BlockedUserId)) return false;
        BlockedUserId that = (BlockedUserId) o;
        return Objects.equals(blockerUsername, that.blockerUsername)
                && Objects.equals(blockedUsername, that.blockedUsername);
    }

    @Override
    public int hashCode() {
        return Objects.hash(blockerUsername, blockedUsername);
    }
}