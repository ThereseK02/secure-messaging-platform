package com.securemessaging.repository;

import com.securemessaging.entity.GroupInvitationEntity;
import com.securemessaging.entity.GroupInvitationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GroupInvitationRepository
        extends JpaRepository<GroupInvitationEntity, Long> {

    Optional<GroupInvitationEntity> findByGroupIdAndInvitedUsername(
            Long groupId,
            String invitedUsername
    );

    List<GroupInvitationEntity> findByInvitedUsernameAndStatusOrderByCreatedAtDesc(
            String invitedUsername,
            GroupInvitationStatus status
    );

    List<GroupInvitationEntity> findByGroupIdAndStatusOrderByCreatedAtDesc(
            Long groupId,
            GroupInvitationStatus status
    );

    void deleteByGroupId(Long groupId);
}
