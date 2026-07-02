package com.securemessaging.repository;

import com.securemessaging.entity.AttachmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AttachmentRepository extends JpaRepository<AttachmentEntity, Long> {
    List<AttachmentEntity> findByReceiverOrderByTimestampDesc(String receiver);
    List<AttachmentEntity> findBySenderOrderByTimestampDesc(String sender);

    List<AttachmentEntity> findByGroupIdOrderByTimestampDesc(Long groupId);
    List<AttachmentEntity> findByGroupMessageId(Long groupMessageId);
}