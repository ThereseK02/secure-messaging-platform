package com.securemessaging.repository;

import com.securemessaging.entity.EncryptedMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EncryptedMessageEntityRepository
        extends JpaRepository<EncryptedMessageEntity, Long> {

    List<EncryptedMessageEntity> findByReceiver(String receiver);
}