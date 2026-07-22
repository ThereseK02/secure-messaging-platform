package com.securemessaging.repository;

import com.securemessaging.entity.GroupDecisionAcknowledgmentRequirementEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroupDecisionAcknowledgmentRequirementRepository
        extends JpaRepository<
        GroupDecisionAcknowledgmentRequirementEntity,
        Long> {

    boolean existsByDecisionId(Long decisionId);

    boolean existsByDecisionIdAndUsername(
            Long decisionId,
            String username
    );

    long countByDecisionId(Long decisionId);

    List<GroupDecisionAcknowledgmentRequirementEntity>
    findByDecisionIdOrderByUsernameAsc(
            Long decisionId
    );
}