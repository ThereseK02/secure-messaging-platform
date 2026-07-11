package com.securemessaging.repository;

import com.securemessaging.entity.GroupAttachmentKeyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface GroupAttachmentKeyRepository
        extends JpaRepository<GroupAttachmentKeyEntity, Long> {

    Optional<GroupAttachmentKeyEntity> findByAttachmentIdAndUsername(
            Long attachmentId,
            String username
    );

    List<GroupAttachmentKeyEntity> findByAttachmentId(Long attachmentId);

    @Transactional
    @Modifying
    void deleteByAttachmentIdIn(Collection<Long> attachmentIds);
}
