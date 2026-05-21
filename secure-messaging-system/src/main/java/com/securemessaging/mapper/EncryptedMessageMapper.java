package com.securemessaging.mapper;

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
}