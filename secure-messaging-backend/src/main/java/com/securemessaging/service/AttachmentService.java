package com.securemessaging.service;

import com.securemessaging.core.SecureMessagingSystem;
import com.securemessaging.entity.AttachmentEntity;
import com.securemessaging.repository.AttachmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

@Service
public class AttachmentService {

    private static final int GCM_TAG_LENGTH = 128;
    private static final int IV_LENGTH_BYTES = 12;

    private final AttachmentRepository attachmentRepository;
    private final DatabaseUserService databaseUserService;

    public AttachmentService(AttachmentRepository attachmentRepository,
                             DatabaseUserService databaseUserService) {
        this.attachmentRepository = attachmentRepository;
        this.databaseUserService = databaseUserService;
    }

    public AttachmentEntity saveEncryptedAttachment(String senderUsername,
                                                    String receiverUsername,
                                                    MultipartFile file,
                                                    Long messageId) throws Exception {
        SecureMessagingSystem.User sender =
                databaseUserService.findDomainUser(senderUsername);

        SecureMessagingSystem.User receiver =
                databaseUserService.findDomainUser(receiverUsername);

        SecretKey aesKey = generateAesKey();
        byte[] iv = generateIv();

        byte[] encryptedFileBytes = encryptFileBytes(file.getBytes(), aesKey, iv);

        String encryptedKeyForReceiverBase64 =
                encryptAesKeyForUser(aesKey, receiver);

        String encryptedKeyForSenderBase64 =
                encryptAesKeyForUser(aesKey, sender);
        AttachmentEntity attachment = new AttachmentEntity(
                senderUsername,
                receiverUsername,
                file.getOriginalFilename(),
                file.getContentType(),
                file.getSize(),
                encryptedFileBytes,
                Base64.getEncoder().encodeToString(iv),
                encryptedKeyForReceiverBase64,
                encryptedKeyForSenderBase64,
                LocalDateTime.now()
        );

        attachment.setMessageId(messageId);

        return attachmentRepository.save(attachment);
    }

    public AttachmentEntity saveEncryptedGroupAttachment(String senderUsername,
                                                         Long groupId,
                                                         Long groupMessageId,
                                                         MultipartFile file) throws Exception {
        SecureMessagingSystem.User sender =
                databaseUserService.findDomainUser(senderUsername);

        SecretKey aesKey = generateAesKey();
        byte[] iv = generateIv();

        byte[] encryptedFileBytes = encryptFileBytes(file.getBytes(), aesKey, iv);

        String encryptedKeyForSenderBase64 =
                encryptAesKeyForUser(aesKey, sender);

        AttachmentEntity attachment = new AttachmentEntity(
                senderUsername,
                senderUsername,
                file.getOriginalFilename(),
                file.getContentType(),
                file.getSize(),
                encryptedFileBytes,
                Base64.getEncoder().encodeToString(iv),
                encryptedKeyForSenderBase64,
                encryptedKeyForSenderBase64,
                LocalDateTime.now()
        );

        attachment.setGroupId(groupId);
        attachment.setGroupMessageId(groupMessageId);

        return attachmentRepository.save(attachment);
    }

    public byte[] decryptAttachmentForUser(Long attachmentId, String currentUsername) throws Exception {
        AttachmentEntity attachment = findById(attachmentId);

        if (!currentUsername.equals(attachment.getSender())
                && !currentUsername.equals(attachment.getReceiver())) {
            throw new SecurityException("Not authorized to download this attachment");
        }

        SecureMessagingSystem.User currentUser =
                databaseUserService.findDomainUser(currentUsername);

        String encryptedKeyBase64 = currentUsername.equals(attachment.getReceiver())
                ? attachment.getEncryptedKeyForReceiverBase64()
                : attachment.getEncryptedKeyForSenderBase64();

        byte[] aesKeyBytes = decryptAesKeyForUser(encryptedKeyBase64, currentUser);
        byte[] iv = Base64.getDecoder().decode(attachment.getIvBase64());

        return decryptFileBytes(attachment.getEncryptedFileBytes(), aesKeyBytes, iv);
    }

    public AttachmentEntity findById(Long id) {
        return attachmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Attachment not found"));
    }

    public List<AttachmentEntity> findInbox(String receiver) {
        return attachmentRepository.findByReceiverOrderByTimestampDesc(receiver);
    }

    public List<AttachmentEntity> findGroupAttachments(Long groupId) {
        return attachmentRepository.findByGroupIdOrderByTimestampDesc(groupId);
    }

    private SecretKey generateAesKey() throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(256);
        return keyGenerator.generateKey();
    }

    private byte[] generateIv() {
        byte[] iv = new byte[IV_LENGTH_BYTES];
        new SecureRandom().nextBytes(iv);
        return iv;
    }

    private byte[] encryptFileBytes(byte[] fileBytes, SecretKey aesKey, byte[] iv) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, aesKey, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
        return cipher.doFinal(fileBytes);
    }

    private byte[] decryptFileBytes(byte[] encryptedFileBytes, byte[] aesKeyBytes, byte[] iv) throws Exception {
        SecretKey aesKey = new javax.crypto.spec.SecretKeySpec(aesKeyBytes, "AES");

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, aesKey, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
        return cipher.doFinal(encryptedFileBytes);
    }

    private String encryptAesKeyForUser(SecretKey aesKey, SecureMessagingSystem.User user) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, user.getRsaPublicKey());
        byte[] encryptedKey = cipher.doFinal(aesKey.getEncoded());
        return Base64.getEncoder().encodeToString(encryptedKey);
    }

    private byte[] decryptAesKeyForUser(String encryptedKeyBase64,
                                        SecureMessagingSystem.User user) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, user.getRsaPrivateKey());
        return cipher.doFinal(Base64.getDecoder().decode(encryptedKeyBase64));
    }
}