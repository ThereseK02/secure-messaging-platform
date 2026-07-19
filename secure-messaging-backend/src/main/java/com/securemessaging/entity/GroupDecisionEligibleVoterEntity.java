package com.securemessaging.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "group_decision_eligible_voters",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_decision_eligible_voter",
                        columnNames = {
                                "decision_id",
                                "username"
                        }
                )
        }
)
public class GroupDecisionEligibleVoterEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "decision_id", nullable = false)
    private Long decisionId;

    @Column(name = "group_id", nullable = false)
    private Long groupId;

    @Column(nullable = false)
    private String username;

    @Enumerated(EnumType.STRING)
    @Column(name = "role_at_snapshot", nullable = false, length = 20)
    private GroupRole roleAtSnapshot;

    @Column(name = "snapshotted_at", nullable = false)
    private LocalDateTime snapshottedAt;

    public GroupDecisionEligibleVoterEntity() {
    }

    public GroupDecisionEligibleVoterEntity(
            Long decisionId,
            Long groupId,
            String username,
            GroupRole roleAtSnapshot,
            LocalDateTime snapshottedAt) {

        this.decisionId = decisionId;
        this.groupId = groupId;
        this.username = username;
        this.roleAtSnapshot = roleAtSnapshot;
        this.snapshottedAt = snapshottedAt;
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

    public GroupRole getRoleAtSnapshot() {
        return roleAtSnapshot;
    }

    public LocalDateTime getSnapshottedAt() {
        return snapshottedAt;
    }
}