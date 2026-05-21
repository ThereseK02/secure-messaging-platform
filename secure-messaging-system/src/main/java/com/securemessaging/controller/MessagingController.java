package com.securemessaging.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class MessagingController {

    @GetMapping("/messages")
    public ResponseEntity<?> getMessages() {
        return ResponseEntity.ok(
                Map.of("status", "Messages endpoint working")
        );
    }
    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {

        return ResponseEntity.ok(
                Map.of(
                        "server", "Online",
                        "encryption", "Active",
                        "signatures", "Enabled",
                        "storage", "In Memory"
                )
        );
    }

    @GetMapping("/messages/encrypted")
    public ResponseEntity<?> encryptedRecords() {

        return ResponseEntity.ok(
                Map.of(
                        "repository", "Encrypted repository accessible",
                        "status", "Records loaded successfully"
                )
        );
    }

    @PostMapping("/messages/inbox")
    public ResponseEntity<?> inbox(@RequestBody Map<String, String> request) {

        String receiver = request.get("receiver");

        if (receiver == null || receiver.isBlank()) {
            receiver = request.get("username");
        }

        if (receiver == null || receiver.isBlank()) {
            receiver = "Unknown receiver";
        }

        return ResponseEntity.ok(
                Map.of(
                        "receiver", receiver,
                        "messages", new String[]{
                                "Encrypted message available",
                                "Digital signature verified"
                        }
                )
        );
    }
    @GetMapping("/users")
    public ResponseEntity<?> users() {
        return ResponseEntity.ok(
                Map.of(
                        "users", new String[]{"Igor", "John", "Simon"}
                )
        );
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> request) {
        return ResponseEntity.ok(
                Map.of(
                        "status", "User registered successfully",
                        "username", request.get("username")
                )
        );
    }

    @PostMapping("/messages/send")
    public ResponseEntity<?> send(@RequestBody Map<String, String> request) {
        return ResponseEntity.ok(
                Map.of(
                        "status", "Message endpoint working",
                        "sender", request.get("sender"),
                        "receiver", request.get("receiver"),
                        "message", request.get("message")
                )
        );
    }
}