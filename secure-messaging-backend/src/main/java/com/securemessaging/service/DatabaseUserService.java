package com.securemessaging.service;

import com.securemessaging.core.SecureMessagingSystem;
import com.securemessaging.core.SecureMessagingSystem.User;
import com.securemessaging.entity.UserEntity;
import com.securemessaging.mapper.UserMapper;
import com.securemessaging.repository.UserEntityRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Locale;

@Service
public class DatabaseUserService {

    private static final int MIN_PASSWORD_LENGTH = 15;
    private static final int MAX_PASSWORD_BYTES = 72;

    private final UserEntityRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final CommonPasswordService commonPasswordService;
    private final CompromisedPasswordService compromisedPasswordService;

    public DatabaseUserService(
            UserEntityRepository repository,
            PasswordEncoder passwordEncoder,
            CommonPasswordService commonPasswordService,
            CompromisedPasswordService compromisedPasswordService) {

        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.commonPasswordService = commonPasswordService;
        this.compromisedPasswordService =
                compromisedPasswordService;
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
                email == null
                        ? ""
                        : email.trim().toLowerCase(Locale.ROOT);

        if (normalizedUsername.isBlank()) {
            throw new RuntimeException("Username is required");
        }

        if (normalizedEmail.isBlank()) {
            throw new RuntimeException("Email is required");
        }

        if (password == null || password.isBlank()) {
            throw new RuntimeException(
                    "Password is required"
            );
        }

        validateNewPassword(
                normalizedUsername,
                normalizedEmail,
                password
        );

        if (!normalizedEmail.matches(
                "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
        )) {
            throw new RuntimeException(
                    "Invalid email address"
            );
        }

        if (repository.existsById(normalizedUsername)) {
            throw new RuntimeException("Username already exists");
        }

        if (repository.findByEmailIgnoreCase(normalizedEmail).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        String passwordHash =
                passwordEncoder.encode(password);

        java.security.KeyPairGenerator keyGen =
                java.security.KeyPairGenerator.getInstance("RSA");

        keyGen.initialize(2048);

        java.security.KeyPair keyPair =
                keyGen.generateKeyPair();

        SecureMessagingSystem.User user =
                new SecureMessagingSystem.User(
                        normalizedUsername,
                        passwordHash,
                        (java.security.interfaces.RSAPublicKey) keyPair.getPublic(),
                        (java.security.interfaces.RSAPrivateKey) keyPair.getPrivate()
                );

        UserEntity entity =
                UserMapper.toEntity(user);

        entity.setEmail(normalizedEmail);

        repository.save(entity);
    }

    @Transactional
    public void changePassword(
            String username,
            String currentPassword,
            String newPassword) throws Exception {

        String normalizedUsername =
                username == null ? "" : username.trim();

        if (normalizedUsername.isBlank()) {
            throw new RuntimeException(
                    "Authenticated user is required"
            );
        }

        if (
                currentPassword == null ||
                        currentPassword.isBlank()
        ) {
            throw new RuntimeException(
                    "Current password is required"
            );
        }

        UserEntity user =
                repository.findById(normalizedUsername)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "User account was not found"
                                )
                        );

        String storedHash =
                user.getPasswordHash();

        if (
                !passwordMatchesStoredHash(
                        currentPassword,
                        storedHash
                )
        ) {
            throw new RuntimeException(
                    "Current password is incorrect"
            );
        }

        if (
                newPassword != null &&
                        passwordMatchesStoredHash(
                                newPassword,
                                storedHash
                        )
        ) {
            throw new RuntimeException(
                    "New password must be different from the current password"
            );
        }

        validateNewPassword(
                normalizedUsername,
                user.getEmail(),
                newPassword
        );

        user.setPasswordHash(
                passwordEncoder.encode(newPassword)
        );

        repository.save(user);
    }

    public boolean existsByUsername(String username) {
        return repository.existsById(username);
    }

    @Transactional
    public boolean validateLogin(
            String username,
            String password) throws Exception {

        String normalizedUsername =
                username == null ? "" : username.trim();

        if (
                normalizedUsername.isBlank() ||
                        password == null ||
                        password.isBlank()
        ) {
            return false;
        }

        var userOptional =
                repository.findById(normalizedUsername);

        if (userOptional.isEmpty()) {
            return false;
        }

        UserEntity user =
                userOptional.get();

        String storedHash =
                user.getPasswordHash();

        if (storedHash == null || storedHash.isBlank()) {
            return false;
        }

        if (isBcryptHash(storedHash)) {
            return passwordEncoder.matches(
                    password,
                    storedHash
            );
        }

        if (!isLegacySha256Hash(storedHash)) {
            return false;
        }

        boolean legacyPasswordMatches =
                verifyLegacySha256Password(
                        password,
                        storedHash
                );

        if (!legacyPasswordMatches) {
            return false;
        }

        user.setPasswordHash(
                passwordEncoder.encode(password)
        );

        repository.save(user);

        return true;
    }

    private void validateNewPassword(
            String username,
            String email,
            String password) {

        if (password == null || password.isBlank()) {
            throw new RuntimeException(
                    "New password is required"
            );
        }

        int passwordLength =
                password.codePointCount(
                        0,
                        password.length()
                );

        if (passwordLength < MIN_PASSWORD_LENGTH) {
            throw new RuntimeException(
                    "Password must be at least 15 characters"
            );
        }

        int passwordBytes =
                password.getBytes(
                        StandardCharsets.UTF_8
                ).length;

        if (passwordBytes > MAX_PASSWORD_BYTES) {
            throw new RuntimeException(
                    "Password must not exceed 72 UTF-8 bytes"
            );
        }

        if (
                commonPasswordService.isBlocked(
                        password,
                        username,
                        email
                )
        ) {
            throw new RuntimeException(
                    "Choose a less common password"
            );
        }

        CompromisedPasswordService.CheckResult
                compromisedPasswordResult =
                compromisedPasswordService.check(password);

        if (
                compromisedPasswordResult
                        == CompromisedPasswordService.CheckResult.COMPROMISED
        ) {
            throw new RuntimeException(
                    "Choose a password that has not appeared in known data breaches"
            );
        }
    }

    private boolean passwordMatchesStoredHash(
            String password,
            String storedHash) throws Exception {

        if (
                password == null ||
                        storedHash == null ||
                        storedHash.isBlank()
        ) {
            return false;
        }

        if (isBcryptHash(storedHash)) {
            return passwordEncoder.matches(
                    password,
                    storedHash
            );
        }

        if (isLegacySha256Hash(storedHash)) {
            return verifyLegacySha256Password(
                    password,
                    storedHash
            );
        }

        return false;
    }

    private boolean isBcryptHash(String storedHash) {
        return storedHash.startsWith("$2a$") ||
                storedHash.startsWith("$2b$") ||
                storedHash.startsWith("$2y$");
    }

    private boolean isLegacySha256Hash(String storedHash) {
        return storedHash.length() == 44;
    }

    private boolean verifyLegacySha256Password(
            String password,
            String storedHash) throws Exception {

        MessageDigest digest =
                MessageDigest.getInstance("SHA-256");

        byte[] submittedHash =
                digest.digest(
                        password.getBytes(StandardCharsets.UTF_8)
                );

        byte[] storedHashBytes;

        try {
            storedHashBytes =
                    Base64.getDecoder().decode(storedHash);

        } catch (IllegalArgumentException e) {
            return false;
        }

        return MessageDigest.isEqual(
                submittedHash,
                storedHashBytes
        );
    }

    public SecureMessagingSystem.User findDomainUser(
            String username) {

        var user =
                repository.findById(username);

        if (user.isEmpty()) {
            throw new RuntimeException(
                    "User not found: " + username
            );
        }

        return UserMapper.toDomain(user.get());
    }
}
