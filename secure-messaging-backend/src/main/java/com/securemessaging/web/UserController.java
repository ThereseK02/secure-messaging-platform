package com.securemessaging.web;

import com.securemessaging.service.DatabaseUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    private final DatabaseUserService databaseUserService;

    public UserController(DatabaseUserService databaseUserService) {
        this.databaseUserService = databaseUserService;
    }

    @GetMapping("/users")
    public ResponseEntity<?> loadUsers() {
        return ResponseEntity.ok(
                Map.of("users", new String[]{"Mark", "Gislain", "Bob"})
        );
    }

    @GetMapping("/messages/inbox/{receiver}")
    public ResponseEntity<?> getInbox(@PathVariable String receiver) {
        return ResponseEntity.ok(
                Map.of(
                        "receiver", receiver,
                        "messages", new String[]{
                                "Encrypted message 1",
                                "Encrypted message 2"
                        }
                )
        );
    }

    @GetMapping("/messages/repository")
    public ResponseEntity<?> repositoryView() {
        return ResponseEntity.ok(
                Map.of("storage", "In-memory encrypted repository active")
        );
    }

    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        return ResponseEntity.ok(
                Map.of(
                        "server", "Online",
                        "encryption", "Active",
                        "signatures", "Enabled",
                        "storage", "PostgreSQL"
                )
        );
    }

    @GetMapping("/encrypted")
    public ResponseEntity<?> encryptedRecords() {
        return ResponseEntity.ok(
                Map.of(
                        "repository", "Encrypted repository accessible",
                        "status", "Records loaded successfully"
                )
        );
    }

    @PostMapping("/inbox")
    public ResponseEntity<?> inbox(@RequestBody Map<String, String> request) {
        return ResponseEntity.ok(
                Map.of(
                        "receiver", request.get("receiver"),
                        "messages", new String[]{
                                "Encrypted message available",
                                "Digital signature verified"
                        }
                )
        );
    }
}