package com.securemessaging.repository;

import com.securemessaging.entity.GroupAttachmentKeyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GroupAttachmentKeyRepository
        extends JpaRepository<GroupAttachmentKeyEntity, Long> {

    Optional<GroupAttachmentKeyEntity> findByAttachmentIdAndUsername(Long attachmentId, String username);

    List<GroupAttachmentKeyEntity> findByAttachmentId(Long attachmentId);
}
