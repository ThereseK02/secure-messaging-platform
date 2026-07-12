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

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private GroupRole role;

    public GroupMemberEntity() {
    }

    public GroupMemberEntity(Long groupId, String username) {
        this(groupId, username, GroupRole.MEMBER);
    }

    public GroupMemberEntity(
            Long groupId,
            String username,
            GroupRole role) {

        this.groupId = groupId;
        this.username = username;
        this.role = role;
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

    public GroupRole getRole() {
        return role;
    }

    public void setRole(GroupRole role) {
        this.role = role;
    }
}
