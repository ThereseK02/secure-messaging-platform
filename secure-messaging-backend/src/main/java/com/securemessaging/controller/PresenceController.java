package com.securemessaging.controller;

import com.securemessaging.entity.GroupMemberEntity;
import com.securemessaging.repository.GroupMemberEntityRepository;
import com.securemessaging.service.UserPresenceService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/presence")
public class PresenceController {

    private final UserPresenceService userPresenceService;
    private final GroupMemberEntityRepository groupMemberRepository;

    public PresenceController(
            UserPresenceService userPresenceService,
            GroupMemberEntityRepository groupMemberRepository) {

        this.userPresenceService = userPresenceService;
        this.groupMemberRepository = groupMemberRepository;
    }

    @PostMapping("/heartbeat")
    public ResponseEntity<?> heartbeat() {

        String currentUsername =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getName();

        userPresenceService.recordHeartbeat(currentUsername);

        return ResponseEntity.ok(
                Map.of(
                        "status", "Presence heartbeat recorded",
                        "username", currentUsername,
                        "online", true
                )
        );
    }

    @DeleteMapping("/offline")
    public ResponseEntity<?> markOffline() {

        String currentUsername =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getName();

        userPresenceService.markOffline(currentUsername);

        return ResponseEntity.ok(
                Map.of(
                        "status", "User marked offline",
                        "username", currentUsername,
                        "online", false
                )
        );
    }

    @GetMapping("/groups/{groupId}")
    public ResponseEntity<?> getOnlineGroupMembers(
            @PathVariable("groupId") Long groupId) {

        String currentUsername =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getName();

        if (
                groupMemberRepository
                        .findByGroupIdAndUsername(
                                groupId,
                                currentUsername
                        )
                        .isEmpty()
        ) {
            return ResponseEntity.status(403).body(
                    Map.of(
                            "error",
                            "You are not a member of this group"
                    )
            );
        }

        List<GroupMemberEntity> groupMembers =
                groupMemberRepository.findByGroupId(groupId);

        Set<String> onlineUsernames =
                groupMembers
                        .stream()
                        .map(GroupMemberEntity::getUsername)
                        .filter(userPresenceService::isOnline)
                        .collect(java.util.stream.Collectors.toSet());

        return ResponseEntity.ok(
                Map.of(
                        "groupId", groupId,
                        "onlineUsernames", onlineUsernames
                )
        );
    }
}
