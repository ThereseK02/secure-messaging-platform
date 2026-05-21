package com.securemessaging.service;

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

    public boolean existsByUsername(String username) {
        return repository.existsById(username);
    }
}