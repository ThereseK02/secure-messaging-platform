package com.securemessaging.service;
import java.util.List;
import com.securemessaging.core.SecureMessagingSystem.EncryptedMessage;
import com.securemessaging.entity.EncryptedMessageEntity;
import com.securemessaging.mapper.EncryptedMessageMapper;
import com.securemessaging.repository.EncryptedMessageEntityRepository;
import org.springframework.stereotype.Service;

@Service
public class DatabaseMessagingService {

    private final EncryptedMessageEntityRepository repository;

    public DatabaseMessagingService(EncryptedMessageEntityRepository repository) {
        this.repository = repository;
    }

    public void saveEncryptedMessage(EncryptedMessage message) {

        EncryptedMessageEntity entity =
                EncryptedMessageMapper.toEntity(message);

        repository.save(entity);
    }
    public List<EncryptedMessageEntity> findInbox(String receiver) {
        return repository.findByReceiver(receiver);
    }
}
