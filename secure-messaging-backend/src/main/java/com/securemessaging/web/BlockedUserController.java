package com.securemessaging.web;

import com.securemessaging.service.BlockedUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/blocked")
@CrossOrigin(origins = "*")
public class BlockedUserController {

    private final BlockedUserService blockedUserService;

    public BlockedUserController(
            BlockedUserService blockedUserService) {

        this.blockedUserService = blockedUserService;
    }

    @PostMapping("/{username}")
    public ResponseEntity<?> blockUser(
            @PathVariable String username) {

        String currentUsername = getCurrentUsername();

        blockedUserService.blockUser(
                currentUsername,
                username);

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "User blocked successfully"
                )
        );
    }

    @DeleteMapping("/{username}")
    public ResponseEntity<?> unblockUser(
            @PathVariable String username) {

        String currentUsername = getCurrentUsername();

        blockedUserService.unblockUser(
                currentUsername,
                username);

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "User unblocked successfully"
                )
        );
    }

    @GetMapping
    public ResponseEntity<?> getBlockedUsers() {

        String currentUsername = getCurrentUsername();

        return ResponseEntity.ok(
                Map.of(
                        "blockedUsers",
                        blockedUserService.getBlockedUsers(
                                currentUsername)
                )
        );
    }

    @GetMapping("/check/{username}")
    public ResponseEntity<?> checkBlocked(
            @PathVariable String username) {

        String currentUsername = getCurrentUsername();

        return ResponseEntity.ok(
                Map.of(
                        "blocked",
                        blockedUserService.isBlocked(
                                currentUsername,
                                username)
                )
        );
    }

    private String getCurrentUsername() {

        return SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();
    }
}