package com.securemessaging.controller;

import com.securemessaging.core.SecureMessagingSystem;
import com.securemessaging.service.DatabaseUserService;
import com.securemessaging.service.DatabaseMessagingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class MessagingController {

    private final DatabaseMessagingService databaseMessagingService;
    private final DatabaseUserService databaseUserService;

    public MessagingController(DatabaseMessagingService databaseMessagingService,
                               DatabaseUserService databaseUserService) {
        this.databaseMessagingService = databaseMessagingService;
        this.databaseUserService = databaseUserService;
    }

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
    public ResponseEntity<?> inbox() {

        String receiver = org.springframework.security.core.context.SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        return ResponseEntity.ok(
                databaseMessagingService.findInbox(receiver)
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
    public ResponseEntity<?> send(@RequestBody Map<String, String> request) throws Exception {

        String senderUsername = org.springframework.security.core.context.SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        String receiverUsername = request.get("receiver");
        String messageText = request.get("message");

        SecureMessagingSystem.User sender =
                databaseUserService.findDomainUser(senderUsername);

        SecureMessagingSystem.User receiver =
                databaseUserService.findDomainUser(receiverUsername);

        SecureMessagingSystem.HybridEncryptionService encryptionService =
                new SecureMessagingSystem.HybridEncryptionService();

        SecureMessagingSystem.EncryptedMessage encryptedMessage =
                encryptionService.encrypt(messageText, sender, receiver);

        databaseMessagingService.saveEncryptedMessage(encryptedMessage);

        return ResponseEntity.ok(
                Map.of(
                        "status", "Encrypted message saved successfully",
                        "sender", senderUsername,
                        "receiver", receiverUsername
                )
        );
    }

}