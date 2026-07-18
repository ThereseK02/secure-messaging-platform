package com.securemessaging.repository;

import com.securemessaging.entity.GroupDecisionEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroupDecisionEventRepository
        extends JpaRepository<GroupDecisionEventEntity, Long> {

    List<GroupDecisionEventEntity>
    findByDecisionIdOrderByEventAtAsc(Long decisionId);

    List<GroupDecisionEventEntity>
    findByGroupIdOrderByEventAtDesc(Long groupId);
}