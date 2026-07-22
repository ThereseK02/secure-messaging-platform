package com.securemessaging.repository;

import com.securemessaging.entity.GroupDecisionAcknowledgmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GroupDecisionAcknowledgmentRepository
        extends JpaRepository<GroupDecisionAcknowledgmentEntity, Long> {

    Optional<GroupDecisionAcknowledgmentEntity>
    findByDecisionIdAndUsername(
            Long decisionId,
            String username
    );

    boolean existsByDecisionIdAndUsername(
            Long decisionId,
            String username
    );

    long countByDecisionId(Long decisionId);

    List<GroupDecisionAcknowledgmentEntity>
    findByDecisionIdOrderByAcknowledgedAtAsc(
            Long decisionId
    );
}