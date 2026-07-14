package com.securemessaging.repository;

import com.securemessaging.entity.EmailGroupInvitationEntity;
import com.securemessaging.entity.EmailGroupInvitationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmailGroupInvitationRepository
        extends JpaRepository<EmailGroupInvitationEntity, Long> {

    Optional<EmailGroupInvitationEntity>
    findByGroupIdAndInvitedEmailIgnoreCase(
            Long groupId,
            String invitedEmail
    );

    Optional<EmailGroupInvitationEntity> findByTokenHash(
            String tokenHash
    );

    List<EmailGroupInvitationEntity>
    findByInvitedEmailIgnoreCaseAndStatusOrderByCreatedAtDesc(
            String invitedEmail,
            EmailGroupInvitationStatus status
    );

    List<EmailGroupInvitationEntity>
    findByGroupIdAndStatusOrderByCreatedAtDesc(
            Long groupId,
            EmailGroupInvitationStatus status
    );

    void deleteByGroupId(Long groupId);
}
