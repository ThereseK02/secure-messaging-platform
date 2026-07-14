package com.securemessaging.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "email_group_invitations",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_email_group_invitation_group_email",
                        columnNames = {"group_id", "invited_email"}
                ),
                @UniqueConstraint(
                        name = "uk_email_group_invitation_token_hash",
                        columnNames = {"token_hash"}
                )
        }
)
public class EmailGroupInvitationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "group_id", nullable = false)
    private Long groupId;

    @Column(name = "invited_email", nullable = false)
    private String invitedEmail;

    @Column(name = "invited_by", nullable = false)
    private String invitedBy;

    @Column(name = "token_hash", nullable = false, length = 64)
    private String tokenHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EmailGroupInvitationStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    @Column(name = "registered_username")
    private String registeredUsername;

    public EmailGroupInvitationEntity() {
    }

    public EmailGroupInvitationEntity(
            Long groupId,
            String invitedEmail,
            String invitedBy,
            String tokenHash,
            EmailGroupInvitationStatus status,
            LocalDateTime createdAt,
            LocalDateTime expiresAt) {

        this.groupId = groupId;
        this.invitedEmail = invitedEmail;
        this.invitedBy = invitedBy;
        this.tokenHash = tokenHash;
        this.status = status;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }

    public Long getId() {
        return id;
    }

    public Long getGroupId() {
        return groupId;
    }

    public String getInvitedEmail() {
        return invitedEmail;
    }

    public String getInvitedBy() {
        return invitedBy;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public EmailGroupInvitationStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public LocalDateTime getUsedAt() {
        return usedAt;
    }

    public String getRegisteredUsername() {
        return registeredUsername;
    }

    public void setInvitedBy(String invitedBy) {
        this.invitedBy = invitedBy;
    }

    public void setTokenHash(String tokenHash) {
        this.tokenHash = tokenHash;
    }

    public void setStatus(EmailGroupInvitationStatus status) {
        this.status = status;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public void setUsedAt(LocalDateTime usedAt) {
        this.usedAt = usedAt;
    }

    public void setRegisteredUsername(String registeredUsername) {
        this.registeredUsername = registeredUsername;
    }
}
