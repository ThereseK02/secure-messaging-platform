package com.securemessaging.repository;

import com.securemessaging.entity.GroupMessageReadEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface GroupMessageReadRepository
        extends JpaRepository<GroupMessageReadEntity, Long> {

    Optional<GroupMessageReadEntity> findByGroupMessageIdAndUsername(
            Long groupMessageId,
            String username
    );

    long countByGroupMessageId(Long groupMessageId);

    List<GroupMessageReadEntity> findByGroupMessageIdIn(Collection<Long> groupMessageIds);
}
