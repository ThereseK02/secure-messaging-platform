package com.securemessaging.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "group_messages")

public class GroupMessageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long groupId;

    private String sender;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    private LocalDateTime timestamp;

    public GroupMessageEntity() {
    }

    public GroupMessageEntity(Long groupId,
                              String sender,
                              String message,
                              LocalDateTime timestamp) {
        this.groupId = groupId;
        this.sender = sender;
        this.message = message;
        this.timestamp = timestamp;
    }

    public Long getId() {
        return id;
    }

    public Long getGroupId() {
        return groupId;
    }

    public String getSender() {
        return sender;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}

