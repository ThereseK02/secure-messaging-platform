package com.securemessaging.repository;

import com.securemessaging.entity.GroupMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface GroupMessageEntityRepository
        extends JpaRepository<GroupMessageEntity, Long> {

    List<GroupMessageEntity> findByGroupIdOrderByTimestampAsc(Long groupId);

    @Transactional
    @Modifying
    void deleteByGroupId(Long groupId);
}
