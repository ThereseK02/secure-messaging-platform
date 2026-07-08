package com.securemessaging.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "group_message_reads",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_group_message_read_user",
                        columnNames = {"group_message_id", "username"}
                )
        }
)
public class GroupMessageReadEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "group_message_id", nullable = false)
    private Long groupMessageId;

    @Column(name = "group_id", nullable = false)
    private Long groupId;

    @Column(nullable = false)
    private String username;

    @Column(name = "read_at", nullable = false)
    private LocalDateTime readAt;

    public GroupMessageReadEntity() {
    }

    public GroupMessageReadEntity(Long groupMessageId,
                                  Long groupId,
                                  String username,
                                  LocalDateTime readAt) {
        this.groupMessageId = groupMessageId;
        this.groupId = groupId;
        this.username = username;
        this.readAt = readAt;
    }

    public Long getId() {
        return id;
    }

    public Long getGroupMessageId() {
        return groupMessageId;
    }

    public Long getGroupId() {
        return groupId;
    }

    public String getUsername() {
        return username;
    }

    public LocalDateTime getReadAt() {
        return readAt;
    }
}
