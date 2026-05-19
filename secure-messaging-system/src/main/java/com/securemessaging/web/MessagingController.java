package com.securemessaging.web;

import com.securemessaging.core.SecureMessagingSystem.DecryptedMessageView;
import com.securemessaging.core.SecureMessagingSystem.EncryptedMessage;
import com.securemessaging.core.SecureMessagingSystem.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class MessagingController {

    private final DemoState demoState;

    public MessagingController(DemoState demoState) {
        this.demoState = demoState;
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of(
                "status", "running",
                "application", "Secure Messaging System",
                "time", LocalDateTime.now().toString()
        );
    }

    @GetMapping("/users")
    public List<UserView> users() {
        return demoState.getUserRepository().findAll()
                .stream()
                .map(user -> new UserView(user.getUsername(), user.getCreatedAt()))
                .toList();
    }

    @PostMapping("/users/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            User user = demoState.getAuthService().register(request.username(), request.password());
            return ResponseEntity.ok(new UserView(user.getUsername(), user.getCreatedAt()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/messages/send")
    public ResponseEntity<?> sendMessage(@RequestBody SendMessageRequest request) {
        try {
            User sender = demoState.requireUser(request.sender());
            User receiver = demoState.requireUser(request.receiver());
            demoState.getMessagingService().sendMessage(request.message(), sender, receiver);
            return ResponseEntity.ok(Map.of("status", "encrypted message sent"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/messages/inbox")
    public ResponseEntity<?> inbox(@RequestBody InboxRequest request) {
        try {
            User receiver = demoState.requireUser(request.username());
            List<DecryptedMessageView> messages = demoState.getMessagingService().receiveMessages(receiver);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/messages/encrypted")
    public List<EncryptedMessageView> encryptedMessages() {
        return demoState.getMessageRepository().findAll()
                .stream()
                .map(EncryptedMessageView::from)
                .toList();
    }

    public record RegisterRequest(String username, String password) {
    }

    public record SendMessageRequest(String sender, String receiver, String message) {
    }

    public record InboxRequest(String username) {
    }

    public record UserView(String username, LocalDateTime createdAt) {
    }

    public record EncryptedMessageView(
            String sender,
            String receiver,
            LocalDateTime timestamp,
            String encryptedPayloadBase64,
            String encryptedSessionKeyBase64,
            String ivBase64,
            String digitalSignatureBase64
    ) {
        public static EncryptedMessageView from(EncryptedMessage message) {
            return new EncryptedMessageView(
                    message.getSender(),
                    message.getReceiver(),
                    message.getTimestamp(),
                    message.getEncryptedPayloadBase64(),
                    message.getEncryptedSessionKeyBase64(),
                    message.getIvBase64(),
                    message.getDigitalSignatureBase64()
            );
        }
    }
}
