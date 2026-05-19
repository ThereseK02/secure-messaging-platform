/**
 * SECURE MESSAGING SYSTEM
 *
 * Original concept: hybrid RSA + AES encryption with digital signatures,
 * user authentication, in-memory repositories, and a messaging service.
 *
 * This version keeps the original academic/demo architecture, but places it
 * inside a Spring Boot-ready package so it can be exposed through a web UI.
 *
 * Author: Kabayanja, Therese
 * Original Date: March 12, 2026
 */
package com.securemessaging.core;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SecureMessagingSystem {

    // =========================
    // Encryption Subsystem
    // =========================

    public static class RSAService {

        public KeyPair generateKeyPair() throws Exception {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            return generator.generateKeyPair();
        }

        public byte[] encrypt(byte[] data, RSAPublicKey publicKey) throws Exception {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            return cipher.doFinal(data);
        }

        public byte[] decrypt(byte[] data, RSAPrivateKey privateKey) throws Exception {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            return cipher.doFinal(data);
        }
    }

    public static class AESService {

        public SecretKey generateSessionKey() throws Exception {
            KeyGenerator generator = KeyGenerator.getInstance("AES");
            generator.init(256);
            return generator.generateKey();
        }

        public byte[] generateIV() {
            byte[] iv = new byte[16];
            new SecureRandom().nextBytes(iv);
            return iv;
        }

        public byte[] encrypt(String message, SecretKey key, byte[] iv) throws Exception {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));
            return cipher.doFinal(message.getBytes(StandardCharsets.UTF_8));
        }

        public String decrypt(byte[] cipherText, SecretKey key, byte[] iv) throws Exception {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
            return new String(cipher.doFinal(cipherText), StandardCharsets.UTF_8);
        }
    }

    public static class DigitalSignatureService {

        public byte[] sign(byte[] data, PrivateKey privateKey) throws Exception {
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(privateKey);
            signature.update(data);
            return signature.sign();
        }

        public boolean verify(byte[] data, byte[] signatureBytes, PublicKey publicKey) throws Exception {
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initVerify(publicKey);
            signature.update(data);
            return signature.verify(signatureBytes);
        }
    }

    public static class HybridEncryptionService {

        private final RSAService rsaService = new RSAService();
        private final AESService aesService = new AESService();
        private final DigitalSignatureService signatureService = new DigitalSignatureService();

        public EncryptedMessage encrypt(String message, User sender, User receiver) throws Exception {
            SecretKey sessionKey = aesService.generateSessionKey();
            byte[] iv = aesService.generateIV();

            byte[] encryptedPayload = aesService.encrypt(message, sessionKey, iv);

            byte[] encryptedSessionKey = rsaService.encrypt(
                    sessionKey.getEncoded(),
                    receiver.getRsaPublicKey()
            );

            byte[] signature = signatureService.sign(
                    encryptedPayload,
                    sender.getRsaPrivateKey()
            );

            return new EncryptedMessage(
                    sender.getUsername(),
                    receiver.getUsername(),
                    encryptedPayload,
                    encryptedSessionKey,
                    iv,
                    signature
            );
        }

        public String decrypt(EncryptedMessage encryptedMessage, User receiver, User sender) throws Exception {
            boolean valid = signatureService.verify(
                    encryptedMessage.getEncryptedPayload(),
                    encryptedMessage.getDigitalSignature(),
                    sender.getRsaPublicKey()
            );

            if (!valid) {
                throw new SecurityException("Signature invalid!");
            }

            byte[] sessionKeyBytes = rsaService.decrypt(
                    encryptedMessage.getEncryptedSessionKey(),
                    receiver.getRsaPrivateKey()
            );

            SecretKey sessionKey = new SecretKeySpec(sessionKeyBytes, "AES");

            return aesService.decrypt(
                    encryptedMessage.getEncryptedPayload(),
                    sessionKey,
                    encryptedMessage.getIv()
            );
        }
    }

    // =========================
    // Message Object
    // =========================

    public static class EncryptedMessage {

        private final String sender;
        private final String receiver;
        private final byte[] encryptedPayload;
        private final byte[] encryptedSessionKey;
        private final byte[] iv;
        private final byte[] digitalSignature;
        private final LocalDateTime timestamp;

        public EncryptedMessage(String sender,
                                String receiver,
                                byte[] encryptedPayload,
                                byte[] encryptedSessionKey,
                                byte[] iv,
                                byte[] digitalSignature) {
            this.sender = sender;
            this.receiver = receiver;
            this.encryptedPayload = encryptedPayload;
            this.encryptedSessionKey = encryptedSessionKey;
            this.iv = iv;
            this.digitalSignature = digitalSignature;
            this.timestamp = LocalDateTime.now();
        }

        public String getSender() {
            return sender;
        }

        public String getReceiver() {
            return receiver;
        }

        public byte[] getEncryptedPayload() {
            return encryptedPayload;
        }

        public byte[] getEncryptedSessionKey() {
            return encryptedSessionKey;
        }

        public byte[] getIv() {
            return iv;
        }

        public byte[] getDigitalSignature() {
            return digitalSignature;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        public String getEncryptedPayloadBase64() {
            return Base64.getEncoder().encodeToString(encryptedPayload);
        }

        public String getEncryptedSessionKeyBase64() {
            return Base64.getEncoder().encodeToString(encryptedSessionKey);
        }

        public String getIvBase64() {
            return Base64.getEncoder().encodeToString(iv);
        }

        public String getDigitalSignatureBase64() {
            return Base64.getEncoder().encodeToString(digitalSignature);
        }
    }

    // =========================
    // Authentication Subsystem
    // =========================

    public static class User {

        private final String username;
        private final String passwordHash;
        private final RSAPublicKey rsaPublicKey;
        private final RSAPrivateKey rsaPrivateKey;
        private final LocalDateTime createdAt;

        public User(String username,
                    String passwordHash,
                    RSAPublicKey publicKey,
                    RSAPrivateKey privateKey) {
            this.username = username;
            this.passwordHash = passwordHash;
            this.rsaPublicKey = publicKey;
            this.rsaPrivateKey = privateKey;
            this.createdAt = LocalDateTime.now();
        }

        public String getUsername() {
            return username;
        }

        public String getPasswordHash() {
            return passwordHash;
        }

        public RSAPublicKey getRsaPublicKey() {
            return rsaPublicKey;
        }

        public RSAPrivateKey getRsaPrivateKey() {
            return rsaPrivateKey;
        }

        public LocalDateTime getCreatedAt() {
            return createdAt;
        }
    }

    public static class PasswordHasher {

        public String hash(String password) throws Exception {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashed);
        }

        public boolean verify(String password, String storedHash) throws Exception {
            return hash(password).equals(storedHash);
        }
    }

    public static class UserRepository {

        private final Map<String, User> users = new HashMap<>();

        public void save(User user) {
            users.put(user.getUsername(), user);
        }

        public User findByUsername(String username) {
            return users.get(username);
        }

        public List<User> findAll() {
            return new ArrayList<>(users.values());
        }
    }

    public static class AuthService {

        private final UserRepository repository;
        private final PasswordHasher hasher = new PasswordHasher();
        private final RSAService rsaService = new RSAService();

        public AuthService(UserRepository repository) {
            this.repository = repository;
        }

        public User register(String username, String password) throws Exception {
            if (repository.findByUsername(username) != null) {
                throw new Exception("Username already exists");
            }

            String hashedPassword = hasher.hash(password);
            KeyPair keyPair = rsaService.generateKeyPair();

            User user = new User(
                    username,
                    hashedPassword,
                    (RSAPublicKey) keyPair.getPublic(),
                    (RSAPrivateKey) keyPair.getPrivate()
            );

            repository.save(user);
            return user;
        }

        public User login(String username, String password) throws Exception {
            User user = repository.findByUsername(username);

            if (user == null) {
                throw new Exception("User not found");
            }

            if (!hasher.verify(password, user.getPasswordHash())) {
                throw new Exception("Invalid password");
            }

            return user;
        }
    }

    // =========================
    // Backend Subsystem
    // =========================

    public static class MessageRepository {

        private final List<EncryptedMessage> messages = new ArrayList<>();

        public void save(EncryptedMessage message) {
            messages.add(message);
        }

        public List<EncryptedMessage> findByReceiver(String username) {
            List<EncryptedMessage> result = new ArrayList<>();

            for (EncryptedMessage msg : messages) {
                if (msg.getReceiver().equals(username)) {
                    result.add(msg);
                }
            }

            return result;
        }

        public List<EncryptedMessage> findAll() {
            return new ArrayList<>(messages);
        }
    }

    public static class MessagingService {

        private final HybridEncryptionService encryptionService = new HybridEncryptionService();
        private final MessageRepository repository;
        private final UserRepository users;

        public MessagingService(MessageRepository repository, UserRepository users) {
            this.repository = repository;
            this.users = users;
        }

        public void sendMessage(String text, User sender, User receiver) throws Exception {
            EncryptedMessage encrypted = encryptionService.encrypt(text, sender, receiver);
            repository.save(encrypted);
        }

        public List<DecryptedMessageView> receiveMessages(User receiver) throws Exception {
            List<EncryptedMessage> messages = repository.findByReceiver(receiver.getUsername());
            List<DecryptedMessageView> result = new ArrayList<>();

            for (EncryptedMessage msg : messages) {
                User sender = users.findByUsername(msg.getSender());
                String decrypted = encryptionService.decrypt(msg, receiver, sender);

                result.add(new DecryptedMessageView(
                        msg.getSender(),
                        msg.getReceiver(),
                        decrypted,
                        msg.getTimestamp()
                ));
            }

            return result;
        }
    }

    public record DecryptedMessageView(
            String sender,
            String receiver,
            String message,
            LocalDateTime timestamp
    ) {
    }
}
