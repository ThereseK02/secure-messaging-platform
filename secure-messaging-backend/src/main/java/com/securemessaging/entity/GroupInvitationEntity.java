package com.securemessaging.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "group_invitations",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_group_invitation_group_username",
                        columnNames = {"group_id", "invited_username"}
                )
        }
)
public class GroupInvitationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "group_id", nullable = false)
    private Long groupId;

    @Column(name = "invited_username", nullable = false)
    private String invitedUsername;

    @Column(name = "invited_by", nullable = false)
    private String invitedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private GroupInvitationStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "responded_at")
    private LocalDateTime respondedAt;

    public GroupInvitationEntity() {
    }

    public GroupInvitationEntity(
            Long groupId,
            String invitedUsername,
            String invitedBy,
            GroupInvitationStatus status,
            LocalDateTime createdAt) {

        this.groupId = groupId;
        this.invitedUsername = invitedUsername;
        this.invitedBy = invitedBy;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public Long getGroupId() {
        return groupId;
    }

    public String getInvitedUsername() {
        return invitedUsername;
    }

    public String getInvitedBy() {
        return invitedBy;
    }

    public GroupInvitationStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getRespondedAt() {
        return respondedAt;
    }

    public void setStatus(GroupInvitationStatus status) {
        this.status = status;
    }

    public void setRespondedAt(LocalDateTime respondedAt) {
        this.respondedAt = respondedAt;
    }
}
