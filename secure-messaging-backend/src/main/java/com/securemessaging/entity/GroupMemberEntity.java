package com.securemessaging.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "group_members")

public class GroupMemberEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long groupId;

    private String username;

    public GroupMemberEntity() {
    }

    public GroupMemberEntity(Long groupId, String username) {
        this.groupId = groupId;
        this.username = username;
    }

    public Long getId() {
        return id;
    }

    public Long getGroupId() {
        return groupId;
    }

    public String getUsername() {
        return username;
    }
}
