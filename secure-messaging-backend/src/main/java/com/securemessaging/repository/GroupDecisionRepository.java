package com.securemessaging.repository;

import com.securemessaging.entity.GroupDecisionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GroupDecisionRepository
        extends JpaRepository<GroupDecisionEntity, Long> {

    Optional<GroupDecisionEntity> findBySourceMessageId(
            Long sourceMessageId
    );

    List<GroupDecisionEntity> findByGroupIdOrderByCreatedAtDesc(
            Long groupId
    );

    boolean existsBySourceMessageId(Long sourceMessageId);
}