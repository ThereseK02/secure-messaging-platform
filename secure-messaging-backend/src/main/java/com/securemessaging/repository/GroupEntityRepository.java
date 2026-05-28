package com.securemessaging.repository;

import com.securemessaging.entity.GroupEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroupEntityRepository
        extends JpaRepository<GroupEntity, Long> {

    List<GroupEntity> findByCreatedBy(String createdBy);
}
