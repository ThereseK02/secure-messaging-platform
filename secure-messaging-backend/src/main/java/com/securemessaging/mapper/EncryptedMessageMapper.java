package com.securemessaging.mapper;
import java.util.Base64;
import com.securemessaging.core.SecureMessagingSystem;

import com.securemessaging.core.SecureMessagingSystem.EncryptedMessage;
import com.securemessaging.entity.EncryptedMessageEntity;

public class EncryptedMessageMapper {

    public static EncryptedMessageEntity toEntity(EncryptedMessage message) {

        return new EncryptedMessageEntity(
                message.getSender(),
                message.getReceiver(),
                message.getEncryptedPayloadBase64(),
                message.getEncryptedSessionKeyBase64(),
                message.getIvBase64(),
                message.getDigitalSignatureBase64(),
                message.getTimestamp()
        );
    }
    public static SecureMessagingSystem.EncryptedMessage toDomain(
            EncryptedMessageEntity entity
    ) {
        return new SecureMessagingSystem.EncryptedMessage(
                entity.getSender(),
                entity.getReceiver(),
                Base64.getDecoder().decode(entity.getEncryptedPayloadBase64()),
                Base64.getDecoder().decode(entity.getEncryptedSessionKeyBase64()),
                Base64.getDecoder().decode(entity.getIvBase64()),
                Base64.getDecoder().decode(entity.getDigitalSignatureBase64())
        );
    }
}