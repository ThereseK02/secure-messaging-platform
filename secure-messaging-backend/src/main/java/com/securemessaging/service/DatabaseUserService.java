package com.securemessaging.service;

import com.securemessaging.core.SecureMessagingSystem;
import com.securemessaging.core.SecureMessagingSystem.User;
import com.securemessaging.entity.UserEntity;
import com.securemessaging.mapper.UserMapper;
import com.securemessaging.repository.UserEntityRepository;
import org.springframework.stereotype.Service;

@Service
public class DatabaseUserService {

    private final UserEntityRepository repository;

    public DatabaseUserService(UserEntityRepository repository) {
        this.repository = repository;
    }

    public void saveUser(User user) {

        UserEntity entity =
                UserMapper.toEntity(user);

        repository.save(entity);
    }
    public void register(
            String username,
            String email,
            String password) throws Exception {

        String normalizedUsername =
                username == null ? "" : username.trim();

        String normalizedEmail =
                email == null ? "" : email.trim().toLowerCase();

        if (normalizedUsername.isBlank()) {
            throw new RuntimeException("Username is required");
        }

        if (normalizedEmail.isBlank()) {
            throw new RuntimeException("Email is required");
        }

        if (!normalizedEmail.matches(
                "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
        )) {
            throw new RuntimeException("Invalid email address");
        }

        if (repository.existsById(normalizedUsername)) {
            throw new RuntimeException("Username already exists");
        }

        if (repository.findByEmailIgnoreCase(normalizedEmail).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        SecureMessagingSystem.PasswordHasher hasher =
                new SecureMessagingSystem.PasswordHasher();

        String passwordHash = hasher.hash(password);

        java.security.KeyPairGenerator keyGen =
                java.security.KeyPairGenerator.getInstance("RSA");

        keyGen.initialize(2048);

        java.security.KeyPair keyPair = keyGen.generateKeyPair();

        SecureMessagingSystem.User user =
                new SecureMessagingSystem.User(
                        normalizedUsername,
                        passwordHash,
                        (java.security.interfaces.RSAPublicKey) keyPair.getPublic(),
                        (java.security.interfaces.RSAPrivateKey) keyPair.getPrivate()
                );

        UserEntity entity = UserMapper.toEntity(user);
        entity.setEmail(normalizedEmail);

        repository.save(entity);
    }

    public boolean existsByUsername(String username) {
        return repository.existsById(username);
    }

    public boolean validateLogin(String username, String password) throws Exception {

        var user = repository.findById(username);

        if (user.isEmpty()) {
            return false;
        }

        SecureMessagingSystem.PasswordHasher hasher =
                new SecureMessagingSystem.PasswordHasher();

        return hasher.verify(password, user.get().getPasswordHash());
    }

    public SecureMessagingSystem.User findDomainUser(String username) {

        var user = repository.findById(username);

        if (user.isEmpty()) {
            throw new RuntimeException("User not found: " + username);
        }

        return UserMapper.toDomain(user.get());
    }
}