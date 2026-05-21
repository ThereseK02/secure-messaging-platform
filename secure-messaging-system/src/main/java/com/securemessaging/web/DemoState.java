package com.securemessaging.web;
import com.securemessaging.security.JwtUtil;
import com.securemessaging.core.SecureMessagingSystem.AuthService;
import com.securemessaging.core.SecureMessagingSystem.MessageRepository;
import com.securemessaging.core.SecureMessagingSystem.MessagingService;
import com.securemessaging.core.SecureMessagingSystem.User;
import com.securemessaging.core.SecureMessagingSystem.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class DemoState {

    private final JwtUtil jwtUtil = new JwtUtil();
    private final UserRepository userRepository = new UserRepository();
    private final MessageRepository messageRepository = new MessageRepository();

    private final com.securemessaging.service.DatabaseUserService databaseUserService;
    private final com.securemessaging.service.DatabaseMessagingService databaseMessagingService;

    private final AuthService authService;
    private final MessagingService messagingService;

    public DemoState(com.securemessaging.service.DatabaseUserService databaseUserService,
                     com.securemessaging.service.DatabaseMessagingService databaseMessagingService) {

        this.databaseUserService = databaseUserService;
        this.databaseMessagingService = databaseMessagingService;

        this.authService =
                new AuthService(userRepository, databaseUserService);

        this.messagingService =
                new MessagingService(messageRepository, userRepository, databaseMessagingService);
    }
    @PostConstruct
    public void initializeDemoUsers() throws Exception {
        ensureUser("Alice", "password123");
        ensureUser("Bob", "secure456");
    }

    private void ensureUser(String username, String password) throws Exception {
        if (userRepository.findByUsername(username) == null) {
            authService.register(username, password);
        }
    }
    public JwtUtil getJwtUtil() {
        return jwtUtil;
    }
    public UserRepository getUserRepository() {
        return userRepository;
    }

    public MessageRepository getMessageRepository() {
        return messageRepository;
    }

    public AuthService getAuthService() {
        return authService;
    }

    public MessagingService getMessagingService() {
        return messagingService;
    }

    public User requireUser(String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new IllegalArgumentException("User not found: " + username);
        }
        return user;
    }
}
