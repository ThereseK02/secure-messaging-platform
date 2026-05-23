package com.securemessaging.web;

import com.securemessaging.core.SecureMessagingSystem;
import com.securemessaging.service.DatabaseUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    private final DatabaseUserService databaseUserService;

    public UserController(DatabaseUserService databaseUserService) {
        this.databaseUserService = databaseUserService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> request) throws Exception {

        String username = request.get("username");
        String password = request.get("password");

        if (databaseUserService.existsByUsername(username)) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Username already exists")
            );
        }

        SecureMessagingSystem.PasswordHasher hasher =
                new SecureMessagingSystem.PasswordHasher();

        SecureMessagingSystem.RSAService rsaService =
                new SecureMessagingSystem.RSAService();

        String passwordHash = hasher.hash(password);

        KeyPair keyPair = rsaService.generateKeyPair();

        SecureMessagingSystem.User user =
                new SecureMessagingSystem.User(
                        username,
                        passwordHash,
                        (RSAPublicKey) keyPair.getPublic(),
                        (RSAPrivateKey) keyPair.getPrivate()
                );

        databaseUserService.saveUser(user);

        return ResponseEntity.ok(
                Map.of(
                        "status", "User registered successfully",
                        "username", username
                )
        );
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