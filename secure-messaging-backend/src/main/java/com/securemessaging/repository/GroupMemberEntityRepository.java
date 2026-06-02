package com.securemessaging.repository;

import com.securemessaging.entity.GroupMemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.repository.Modifying;


public interface GroupMemberEntityRepository
        extends JpaRepository<GroupMemberEntity, Long> {

    List<GroupMemberEntity> findByUsername(String username);

    List<GroupMemberEntity> findByGroupId(Long groupId);

    Optional<GroupMemberEntity> findByGroupIdAndUsername(Long groupId, String username);
@Transactional
@Modifying
void deleteByGroupIdAndUsername(Long groupId, String username);
}
