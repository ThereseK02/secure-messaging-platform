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

    public void openVoting(LocalDateTime votingDeadline) {
        if (governanceMode != GroupDecisionGovernanceMode.MEMBER_VOTE) {
            throw new IllegalStateException(
                    "Only member-vote decisions can open voting"
            );
        }

        if (status != GroupDecisionStatus.PROPOSED) {
            throw new IllegalStateException(
                    "Only a proposed decision can open voting"
            );
        }

        if (votingDeadline == null) {
            throw new IllegalArgumentException(
                    "Voting deadline is required"
            );
        }

        if (!votingDeadline.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException(
                    "Voting deadline must be in the future"
            );
        }

        this.status = GroupDecisionStatus.VOTING_OPEN;
        this.votingDeadline = votingDeadline;
        this.tieBreakDeadline = null;
    }

    public boolean isVotingOpenAt(LocalDateTime currentTime) {
        if (currentTime == null) {
            throw new IllegalArgumentException(
                    "Current time is required"
            );
        }

        return governanceMode == GroupDecisionGovernanceMode.MEMBER_VOTE
                && status == GroupDecisionStatus.VOTING_OPEN
                && votingDeadline != null
                && currentTime.isBefore(votingDeadline);
    }

    public void resolveMemberVote(
            GroupDecisionStatus outcomeStatus,
            LocalDateTime resolvedAt) {

        if (governanceMode != GroupDecisionGovernanceMode.MEMBER_VOTE) {
            throw new IllegalStateException(
                    "Only member-vote decisions can be resolved by voting"
            );
        }

        if (status != GroupDecisionStatus.VOTING_OPEN) {
            throw new IllegalStateException(
                    "Only an open vote can be resolved"
            );
        }

        if (resolvedAt == null) {
            throw new IllegalArgumentException(
                    "Vote resolution time is required"
            );
        }

        if (
                votingDeadline == null ||
                        resolvedAt.isBefore(votingDeadline)
        ) {
            throw new IllegalStateException(
                    "Voting cannot be resolved before the deadline"
            );
        }

        if (
                outcomeStatus != GroupDecisionStatus.APPROVED &&
                        outcomeStatus != GroupDecisionStatus.REJECTED &&
                        outcomeStatus !=
                                GroupDecisionStatus.WAITING_FOR_TIE_BREAK &&
                        outcomeStatus !=
                                GroupDecisionStatus.EXPIRED_WITHOUT_QUORUM
        ) {
            throw new IllegalArgumentException(
                    "Unsupported member-vote outcome"
            );
        }

        this.status = outcomeStatus;

        if (
                outcomeStatus ==
                        GroupDecisionStatus.WAITING_FOR_TIE_BREAK
        ) {
            this.tieBreakDeadline =
                    resolvedAt.plusDays(1);
        } else {
            this.tieBreakDeadline = null;
        }
    }

    public void resolveTieBreak(
            GroupDecisionStatus finalStatus,
            LocalDateTime resolvedAt) {

        if (
                governanceMode !=
                        GroupDecisionGovernanceMode.MEMBER_VOTE
        ) {
            throw new IllegalStateException(
                    "Only member-vote decisions can use a tie-break"
            );
        }

        if (
                status !=
                        GroupDecisionStatus.WAITING_FOR_TIE_BREAK
        ) {
            throw new IllegalStateException(
                    "Decision is not waiting for a tie-break"
            );
        }

        if (resolvedAt == null) {
            throw new IllegalArgumentException(
                    "Tie-break resolution time is required"
            );
        }

        if (tieBreakDeadline == null) {
            throw new IllegalStateException(
                    "Tie-break deadline is not available"
            );
        }

        if (resolvedAt.isAfter(tieBreakDeadline)) {
            throw new IllegalStateException(
                    "Tie-break deadline has passed"
            );
        }

        if (
                finalStatus != GroupDecisionStatus.APPROVED &&
                        finalStatus != GroupDecisionStatus.REJECTED
        ) {
            throw new IllegalArgumentException(
                    "Tie-break outcome must be APPROVED or REJECTED"
            );
        }

        this.status = finalStatus;
        this.tieBreakDeadline = null;
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
