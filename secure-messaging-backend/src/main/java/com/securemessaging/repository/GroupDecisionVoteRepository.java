package com.securemessaging.repository;

import com.securemessaging.entity.GroupDecisionVoteChoice;
import com.securemessaging.entity.GroupDecisionVoteEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface GroupDecisionVoteRepository
        extends JpaRepository<GroupDecisionVoteEntity, Long> {

    Optional<GroupDecisionVoteEntity>
    findByDecisionIdAndVoterUsername(
            Long decisionId,
            String voterUsername
    );

    List<GroupDecisionVoteEntity>
    findByDecisionIdOrderByCreatedAtAsc(
            Long decisionId
    );

    long countByDecisionId(
            Long decisionId
    );

    long countByDecisionIdAndVoteChoice(
            Long decisionId,
            GroupDecisionVoteChoice voteChoice
    );

    boolean existsByDecisionIdAndVoterUsername(
            Long decisionId,
            String voterUsername
    );

    void deleteByDecisionId(
            Long decisionId
    );
}
