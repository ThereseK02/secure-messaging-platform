package com.securemessaging.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "group_decision_acknowledgments",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_group_decision_acknowledgment_member",
                        columnNames = {
                                "decision_id",
                                "username"
                        }
                )
        },
        indexes = {
                @Index(
                        name = "idx_group_decision_ack_decision",
                        columnList = "decision_id"
                ),
                @Index(
                        name = "idx_group_decision_ack_group",
                        columnList = "group_id"
                ),
                @Index(
                        name = "idx_group_decision_ack_username",
                        columnList = "username"
                )
        }
)
public class GroupDecisionAcknowledgmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "decision_id", nullable = false)
    private Long decisionId;

    @Column(name = "group_id", nullable = false)
    private Long groupId;

    @Column(nullable = false)
    private String username;

    @Column(name = "acknowledged_at", nullable = false)
    private LocalDateTime acknowledgedAt;

    public GroupDecisionAcknowledgmentEntity() {
    }

    public GroupDecisionAcknowledgmentEntity(
            Long decisionId,
            Long groupId,
            String username,
            LocalDateTime acknowledgedAt) {

        this.decisionId = decisionId;
        this.groupId = groupId;
        this.username = username;
        this.acknowledgedAt = acknowledgedAt;
    }

    public Long getId() {
        return id;
    }

    public Long getDecisionId() {
        return decisionId;
    }

    public Long getGroupId() {
        return groupId;
    }

    public String getUsername() {
        return username;
    }

    public LocalDateTime getAcknowledgedAt() {
        return acknowledgedAt;
    }
}