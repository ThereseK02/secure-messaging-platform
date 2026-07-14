package com.securemessaging.mapper;

import com.securemessaging.core.SecureMessagingSystem.User;
import com.securemessaging.entity.UserEntity;

import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class UserMapper {

    public static UserEntity toEntity(User user) {
        return new UserEntity(
                user.getUsername(),
                null,
                user.getPasswordHash(),
                Base64.getEncoder().encodeToString(user.getRsaPublicKey().getEncoded()),
                Base64.getEncoder().encodeToString(user.getRsaPrivateKey().getEncoded()),
                user.getCreatedAt()
        );
    }

    public static User toDomain(UserEntity entity) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");

            byte[] publicKeyBytes = Base64.getDecoder().decode(entity.getPublicKeyBase64());
            byte[] privateKeyBytes = Base64.getDecoder().decode(entity.getPrivateKeyBase64());

            RSAPublicKey publicKey = (RSAPublicKey) keyFactory.generatePublic(
                    new X509EncodedKeySpec(publicKeyBytes)
            );

            RSAPrivateKey privateKey = (RSAPrivateKey) keyFactory.generatePrivate(
                    new PKCS8EncodedKeySpec(privateKeyBytes)
            );

            return new User(
                    entity.getUsername(),
                    entity.getPasswordHash(),
                    publicKey,
                    privateKey
            );

        } catch (Exception e) {
            throw new RuntimeException("Failed to convert UserEntity to User", e);
        }
    }
}