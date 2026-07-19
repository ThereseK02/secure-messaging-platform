package com.securemessaging.service;

import com.securemessaging.entity.GroupDecisionEntity;
import com.securemessaging.entity.GroupDecisionEventEntity;
import com.securemessaging.entity.GroupDecisionEventType;
import com.securemessaging.entity.GroupMessageEntity;
import com.securemessaging.repository.GroupDecisionEventRepository;
import com.securemessaging.repository.GroupDecisionRepository;
import com.securemessaging.repository.GroupMemberEntityRepository;
import com.securemessaging.repository.GroupMessageEntityRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class GroupDecisionService {

    private final GroupDecisionRepository decisionRepository;
    private final GroupDecisionEventRepository decisionEventRepository;
    private final GroupMemberEntityRepository groupMemberRepository;
    private final GroupMessageEntityRepository groupMessageRepository;

    public GroupDecisionService(
            GroupDecisionRepository decisionRepository,
            GroupDecisionEventRepository decisionEventRepository,
            GroupMemberEntityRepository groupMemberRepository,
            GroupMessageEntityRepository groupMessageRepository) {

        this.decisionRepository = decisionRepository;
        this.decisionEventRepository = decisionEventRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.groupMessageRepository = groupMessageRepository;
    }

    @Transactional
    public GroupDecisionEntity createDecision(
            Long groupId,
            Long sourceMessageId,
            String actorUsername) {

        if (groupId == null) {
            throw new RuntimeException("Group ID is required");
        }

        if (sourceMessageId == null) {
            throw new RuntimeException("Source message ID is required");
        }

        String normalizedActorUsername =
                actorUsername == null
                        ? ""
                        : actorUsername.trim();

        if (normalizedActorUsername.isBlank()) {
            throw new RuntimeException(
                    "Authenticated user is required"
            );
        }

        groupMemberRepository
                        .findByGroupIdAndUsername(
                                groupId,
                                normalizedActorUsername
                        )
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "You are not a member of this group"
                                )
                        );


        GroupMessageEntity sourceMessage =
                groupMessageRepository
                        .findById(sourceMessageId)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Group message not found"
                                )
                        );

        if (!groupId.equals(sourceMessage.getGroupId())) {
            throw new RuntimeException(
                    "Group message does not belong to this group"
            );
        }

        String messageText =
                sourceMessage.getMessage();

        if (messageText == null || messageText.isBlank()) {
            throw new RuntimeException(
                    "A blank group message cannot become a decision"
            );
        }

        if (
                decisionRepository.existsBySourceMessageId(
                        sourceMessageId
                )
        ) {
            throw new RuntimeException(
                    "This group message is already a decision"
            );
        }

        LocalDateTime createdAt =
                LocalDateTime.now();

        GroupDecisionEntity decision =
                new GroupDecisionEntity(
                        groupId,
                        sourceMessageId,
                        sourceMessage.getSender(),
                        messageText,
                        normalizedActorUsername,
                        createdAt
                );

        GroupDecisionEntity savedDecision =
                decisionRepository.save(decision);

        GroupDecisionEventEntity creationEvent =
                new GroupDecisionEventEntity(
                        savedDecision.getId(),
                        groupId,
                        GroupDecisionEventType.CREATED,
                        normalizedActorUsername,
                        createdAt,
                        "Decision created from group message " +
                                sourceMessageId
                );

        decisionEventRepository.save(creationEvent);

        return savedDecision;
    }
}