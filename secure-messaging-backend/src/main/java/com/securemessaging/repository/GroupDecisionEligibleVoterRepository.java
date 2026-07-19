package com.securemessaging.repository;

import com.securemessaging.entity.GroupDecisionEligibleVoterEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroupDecisionEligibleVoterRepository
        extends JpaRepository<
                GroupDecisionEligibleVoterEntity,
                Long
        > {

    List<GroupDecisionEligibleVoterEntity>
    findByDecisionIdOrderByUsernameAsc(Long decisionId);

    boolean existsByDecisionIdAndUsername(
            Long decisionId,
            String username
    );

    long countByDecisionId(Long decisionId);
}