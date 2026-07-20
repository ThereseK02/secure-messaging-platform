package com.securemessaging.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "group_decision_votes",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_group_decision_vote_member",
                        columnNames = {
                                "decision_id",
                                "voter_username"
                        }
                )
        },
        indexes = {
                @Index(
                        name = "idx_group_decision_vote_decision",
                        columnList = "decision_id"
                ),
                @Index(
                        name = "idx_group_decision_vote_group",
                        columnList = "group_id"
                ),
                @Index(
                        name = "idx_group_decision_vote_voter",
                        columnList = "voter_username"
                )
        }
)
public class GroupDecisionVoteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "group_id", nullable = false)
    private Long groupId;

    @Column(name = "decision_id", nullable = false)
    private Long decisionId;

    @Column(name = "voter_username", nullable = false)
    private String voterUsername;

    @Enumerated(EnumType.STRING)
    @Column(name = "vote_choice", nullable = false, length = 20)
    private GroupDecisionVoteChoice voteChoice;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public GroupDecisionVoteEntity() {
    }

    public GroupDecisionVoteEntity(
            Long groupId,
            Long decisionId,
            String voterUsername,
            GroupDecisionVoteChoice voteChoice,
            LocalDateTime createdAt) {

        this.groupId = groupId;
        this.decisionId = decisionId;
        this.voterUsername = voterUsername;
        this.voteChoice = voteChoice;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public Long getGroupId() {
        return groupId;
    }

    public Long getDecisionId() {
        return decisionId;
    }

    public String getVoterUsername() {
        return voterUsername;
    }

    public GroupDecisionVoteChoice getVoteChoice() {
        return voteChoice;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void changeVote(
            GroupDecisionVoteChoice voteChoice,
            LocalDateTime updatedAt) {

        if (voteChoice == null) {
            throw new IllegalArgumentException(
                    "Vote choice is required"
            );
        }

        if (updatedAt == null) {
            throw new IllegalArgumentException(
                    "Vote update time is required"
            );
        }

        this.voteChoice = voteChoice;
        this.updatedAt = updatedAt;
    }
}
