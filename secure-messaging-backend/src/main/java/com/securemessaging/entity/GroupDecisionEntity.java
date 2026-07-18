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

        this.groupId = groupId;
        this.sourceMessageId = sourceMessageId;
        this.sourceSender = sourceSender;
        this.decisionTextSnapshot = decisionTextSnapshot;
        this.createdBy = createdBy;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}