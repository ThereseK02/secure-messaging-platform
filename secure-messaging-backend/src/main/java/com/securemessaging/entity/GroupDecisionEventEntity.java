package com.securemessaging.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "group_decision_events",
        indexes = {
                @Index(
                        name = "idx_group_decision_event_decision",
                        columnList = "decision_id"
                ),
                @Index(
                        name = "idx_group_decision_event_group",
                        columnList = "group_id"
                )
        }
)
public class GroupDecisionEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "decision_id", nullable = false)
    private Long decisionId;

    @Column(name = "group_id", nullable = false)
    private Long groupId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 40)
    private GroupDecisionEventType eventType;

    @Column(name = "actor_username", nullable = false)
    private String actorUsername;

    @Column(name = "event_at", nullable = false)
    private LocalDateTime eventAt;

    @Column(name = "event_details", columnDefinition = "TEXT")
    private String eventDetails;

    public GroupDecisionEventEntity() {
    }

    public GroupDecisionEventEntity(
            Long decisionId,
            Long groupId,
            GroupDecisionEventType eventType,
            String actorUsername,
            LocalDateTime eventAt,
            String eventDetails) {

        this.decisionId = decisionId;
        this.groupId = groupId;
        this.eventType = eventType;
        this.actorUsername = actorUsername;
        this.eventAt = eventAt;
        this.eventDetails = eventDetails;
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

    public GroupDecisionEventType getEventType() {
        return eventType;
    }

    public String getActorUsername() {
        return actorUsername;
    }

    public LocalDateTime getEventAt() {
        return eventAt;
    }

    public String getEventDetails() {
        return eventDetails;
    }
}