package com.securemessaging.repository;

import com.securemessaging.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserEntityRepository extends JpaRepository<UserEntity, String> {
}