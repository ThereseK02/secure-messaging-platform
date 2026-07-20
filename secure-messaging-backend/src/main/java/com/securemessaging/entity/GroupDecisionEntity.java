package com.securemessaging.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "group_decisions",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_group_decision_source_message",
                        columnNames = {"source_message_id"}
                )
        }
)
public class GroupDecisionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "group_id", nullable = false)
    private Long groupId;

    @Column(name = "source_message_id", nullable = false)
    private Long sourceMessageId;

    @Column(name = "source_sender", nullable = false)
    private String sourceSender;

    @Column(
            name = "decision_text_snapshot",
            nullable = false,
            columnDefinition = "TEXT"
    )
    private String decisionTextSnapshot;

    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 40)
    private GroupDecisionStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", length = 40)
    private GroupDecisionCategory category;

    @Enumerated(EnumType.STRING)
    @Column(name = "threshold", length = 40)
    private GroupDecisionThreshold threshold;

    @Enumerated(EnumType.STRING)
    @Column(name = "governance_mode", length = 40)
    private GroupDecisionGovernanceMode governanceMode;

    @Column(name = "voting_deadline")
    private LocalDateTime votingDeadline;

    @Column(name = "tie_break_deadline")
    private LocalDateTime tieBreakDeadline;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public GroupDecisionEntity() {
    }

    public GroupDecisionEntity(
            Long groupId,
            Long sourceMessageId,
            String sourceSender,
            String decisionTextSnapshot,
            String createdBy,
            LocalDateTime createdAt) {

        this(
                groupId,
                sourceMessageId,
                sourceSender,
                decisionTextSnapshot,
                createdBy,
                GroupDecisionGovernanceMode.OWNER_REVIEW,
                createdAt
        );
    }

    public GroupDecisionEntity(
            Long groupId,
            Long sourceMessageId,
            String sourceSender,
            String decisionTextSnapshot,
            String createdBy,
            GroupDecisionGovernanceMode governanceMode,
            LocalDateTime createdAt) {

        this.groupId = groupId;
        this.sourceMessageId = sourceMessageId;
        this.sourceSender = sourceSender;
        this.decisionTextSnapshot = decisionTextSnapshot;
        this.createdBy = createdBy;
        this.status = GroupDecisionStatus.PROPOSED;
        this.category = GroupDecisionCategory.ROUTINE_OPERATION;
        this.threshold = GroupDecisionThreshold.SIMPLE_MAJORITY;
        this.governanceMode =
                governanceMode == null
                        ? GroupDecisionGovernanceMode.OWNER_REVIEW
                        : governanceMode;
        this.votingDeadline = null;
        this.tieBreakDeadline = null;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public Long getGroupId() {
        return groupId;
    }

    public Long getSourceMessageId() {
        return sourceMessageId;
    }

    public String getSourceSender() {
        return sourceSender;
    }

    public String getDecisionTextSnapshot() {
        return decisionTextSnapshot;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public GroupDecisionStatus getStatus() {
        return status;
    }

    public GroupDecisionCategory getCategory() {
        return category;
    }

    public GroupDecisionThreshold getThreshold() {
        return threshold;
    }

    public GroupDecisionGovernanceMode getGovernanceMode() {
        return governanceMode;
    }

    public LocalDateTime getVotingDeadline() {
        return votingDeadline;
    }

    public LocalDateTime getTieBreakDeadline() {
        return tieBreakDeadline;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void approve() {
        requireProposedOwnerReviewDecision();
        this.status = GroupDecisionStatus.APPROVED;
    }

    public void reject() {
        requireProposedOwnerReviewDecision();
        this.status = GroupDecisionStatus.REJECTED;
    }

    private void requireProposedOwnerReviewDecision() {
        if (governanceMode != GroupDecisionGovernanceMode.OWNER_REVIEW) {
            throw new IllegalStateException(
                    "Only proposals for owner approval can be approved or rejected"
            );
        }

        if (status != GroupDecisionStatus.PROPOSED) {
            throw new IllegalStateException(
                    "Only a proposed decision can be approved or rejected"
            );
        }
    }
}
