package com.securemessaging.repository;

import com.securemessaging.entity.GroupMessageReadEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

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

    @Query("""
            SELECT COUNT(message)
            FROM GroupMessageEntity message
            WHERE message.groupId = :groupId
              AND message.sender <> :username
              AND NOT EXISTS (
                  SELECT readRecord.id
                  FROM GroupMessageReadEntity readRecord
                  WHERE readRecord.groupMessageId = message.id
                    AND readRecord.username = :username
              )
            """)
    long countUnreadByGroupIdAndUsername(
            @Param("groupId") Long groupId,
            @Param("username") String username
    );

    @Transactional
    @Modifying
    @Query(
            value = """
                    INSERT INTO group_message_reads
                        (group_message_id, group_id, username, read_at)
                    VALUES
                        (:groupMessageId, :groupId, :username, :readAt)
                    ON CONFLICT (group_message_id, username)
                    DO NOTHING
                    """,
            nativeQuery = true
    )
    int insertReadIfAbsent(
            @Param("groupMessageId") Long groupMessageId,
            @Param("groupId") Long groupId,
            @Param("username") String username,
            @Param("readAt") java.time.LocalDateTime readAt
    );

    List<GroupMessageReadEntity> findByGroupMessageIdIn(
            Collection<Long> groupMessageIds
    );

    @Transactional
    @Modifying
    void deleteByGroupMessageId(Long groupMessageId);

    @Transactional
    @Modifying
    void deleteByGroupId(Long groupId);
}
