package com.securemessaging.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "group_decision_acknowledgment_requirements",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_group_decision_ack_requirement_member",
                        columnNames = {
                                "decision_id",
                                "username"
                        }
                )
        },
        indexes = {
                @Index(
                        name = "idx_group_decision_ack_req_decision",
                        columnList = "decision_id"
                ),
                @Index(
                        name = "idx_group_decision_ack_req_group",
                        columnList = "group_id"
                ),
                @Index(
                        name = "idx_group_decision_ack_req_username",
                        columnList = "username"
                )
        }
)
public class GroupDecisionAcknowledgmentRequirementEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "decision_id", nullable = false)
    private Long decisionId;

    @Column(name = "group_id", nullable = false)
    private Long groupId;

    @Column(nullable = false)
    private String username;

    @Column(name = "required_at", nullable = false)
    private LocalDateTime requiredAt;

    public GroupDecisionAcknowledgmentRequirementEntity() {
    }

    public GroupDecisionAcknowledgmentRequirementEntity(
            Long decisionId,
            Long groupId,
            String username,
            LocalDateTime requiredAt) {

        this.decisionId = decisionId;
        this.groupId = groupId;
        this.username = username;
        this.requiredAt = requiredAt;
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

    public LocalDateTime getRequiredAt() {
        return requiredAt;
    }
}