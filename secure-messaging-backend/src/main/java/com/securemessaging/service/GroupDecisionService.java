package com.securemessaging.service;

import com.securemessaging.entity.GroupDecisionEntity;
import com.securemessaging.entity.GroupDecisionEventEntity;
import com.securemessaging.entity.GroupDecisionEventType;
import com.securemessaging.entity.GroupDecisionGovernanceMode;
import com.securemessaging.entity.GroupDecisionStatus;
import com.securemessaging.entity.GroupDecisionVoteChoice;
import com.securemessaging.entity.GroupDecisionVoteEntity;
import com.securemessaging.entity.GroupMemberEntity;
import com.securemessaging.entity.GroupMessageEntity;
import com.securemessaging.entity.GroupRole;
import com.securemessaging.repository.GroupDecisionEventRepository;
import com.securemessaging.repository.GroupDecisionRepository;
import com.securemessaging.repository.GroupMemberEntityRepository;
import com.securemessaging.repository.GroupMessageEntityRepository;
import com.securemessaging.repository.GroupDecisionVoteRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class GroupDecisionService {

    private final GroupDecisionRepository decisionRepository;
    private final GroupDecisionEventRepository decisionEventRepository;
    private final GroupDecisionVoteRepository decisionVoteRepository;
    private final GroupMemberEntityRepository groupMemberRepository;
    private final GroupMessageEntityRepository groupMessageRepository;

    public GroupDecisionService(
            GroupDecisionRepository decisionRepository,
            GroupDecisionEventRepository decisionEventRepository,
            GroupDecisionVoteRepository decisionVoteRepository,
            GroupMemberEntityRepository groupMemberRepository,
            GroupMessageEntityRepository groupMessageRepository) {

        this.decisionRepository = decisionRepository;
        this.decisionEventRepository = decisionEventRepository;
        this.decisionVoteRepository = decisionVoteRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.groupMessageRepository = groupMessageRepository;
    }

    @Transactional
    public GroupDecisionEntity createDecision(
            Long groupId,
            Long sourceMessageId,
            String actorUsername) {

        return createDecision(
                groupId,
                sourceMessageId,
                actorUsername,
                GroupDecisionGovernanceMode.OWNER_REVIEW
        );
    }

    @Transactional
    public GroupDecisionEntity createDecision(
            Long groupId,
            Long sourceMessageId,
            String actorUsername,
            GroupDecisionGovernanceMode requestedGovernanceMode) {

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

        GroupDecisionGovernanceMode governanceMode =
                requestedGovernanceMode == null
                        ? GroupDecisionGovernanceMode.OWNER_REVIEW
                        : requestedGovernanceMode;

        GroupMemberEntity currentMember =
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

        if (
                governanceMode ==
                        GroupDecisionGovernanceMode.OWNER_LED &&
                currentMember.getRole() != GroupRole.OWNER
        ) {
            throw new RuntimeException(
                    "Only the group owner can select owner-led governance"
            );
        }

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
                        governanceMode,
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

    @Transactional
    public GroupDecisionEntity approveDecision(
            Long groupId,
            Long decisionId,
            String actorUsername) {

        return resolveOwnerReviewDecision(
                groupId,
                decisionId,
                actorUsername,
                GroupDecisionStatus.APPROVED
        );
    }

    @Transactional
    public GroupDecisionEntity rejectDecision(
            Long groupId,
            Long decisionId,
            String actorUsername) {

        return resolveOwnerReviewDecision(
                groupId,
                decisionId,
                actorUsername,
                GroupDecisionStatus.REJECTED
        );
    }

    private GroupDecisionEntity resolveOwnerReviewDecision(
            Long groupId,
            Long decisionId,
            String actorUsername,
            GroupDecisionStatus targetStatus) {

        if (groupId == null) {
            throw new RuntimeException("Group ID is required");
        }

        if (decisionId == null) {
            throw new RuntimeException("Decision ID is required");
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

        GroupMemberEntity currentMember =
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

        if (currentMember.getRole() != GroupRole.OWNER) {
            throw new RuntimeException(
                    "Only the group owner can approve or reject this proposal"
            );
        }

        GroupDecisionEntity decision =
                decisionRepository
                        .findByIdAndGroupId(
                                decisionId,
                                groupId
                        )
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Group decision not found"
                                )
                        );

        try {
            if (targetStatus == GroupDecisionStatus.APPROVED) {
                decision.approve();
            } else if (targetStatus == GroupDecisionStatus.REJECTED) {
                decision.reject();
            } else {
                throw new IllegalArgumentException(
                        "Unsupported owner decision status"
                );
            }
        } catch (IllegalStateException exception) {
            throw new RuntimeException(exception.getMessage());
        }

        GroupDecisionEntity savedDecision =
                decisionRepository.save(decision);

        LocalDateTime eventAt = LocalDateTime.now();

        GroupDecisionEventType eventType =
                targetStatus == GroupDecisionStatus.APPROVED
                        ? GroupDecisionEventType.APPROVED
                        : GroupDecisionEventType.REJECTED;

        String eventDetails =
                targetStatus == GroupDecisionStatus.APPROVED
                        ? "Proposal approved by group owner"
                        : "Proposal rejected by group owner";

        decisionEventRepository.save(
                new GroupDecisionEventEntity(
                        savedDecision.getId(),
                        groupId,
                        eventType,
                        normalizedActorUsername,
                        eventAt,
                        eventDetails
                )
        );

        return savedDecision;
    }

    @Transactional
    public GroupDecisionEntity openVoting(
            Long groupId,
            Long decisionId,
            String actorUsername,
            LocalDateTime votingDeadline) {

        if (groupId == null) {
            throw new RuntimeException("Group ID is required");
        }

        if (decisionId == null) {
            throw new RuntimeException("Decision ID is required");
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

        GroupMemberEntity currentMember =
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

        if (
                currentMember.getRole() != GroupRole.OWNER &&
                        currentMember.getRole() != GroupRole.ADMIN
        ) {
            throw new RuntimeException(
                    "Only the group owner or an admin can open voting"
            );
        }

        GroupDecisionEntity decision =
                decisionRepository
                        .findByIdAndGroupId(
                                decisionId,
                                groupId
                        )
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Group decision not found"
                                )
                        );

        try {
            decision.openVoting(votingDeadline);
        } catch (
                IllegalStateException |
                IllegalArgumentException exception
        ) {
            throw new RuntimeException(exception.getMessage());
        }

        GroupDecisionEntity savedDecision =
                decisionRepository.save(decision);

        LocalDateTime eventAt =
                LocalDateTime.now();

        decisionEventRepository.save(
                new GroupDecisionEventEntity(
                        savedDecision.getId(),
                        groupId,
                        GroupDecisionEventType.VOTING_OPENED,
                        normalizedActorUsername,
                        eventAt,
                        "Voting opened until " + votingDeadline
                )
        );

        return savedDecision;
    }

    @Transactional
    public GroupDecisionVoteEntity castVote(
            Long groupId,
            Long decisionId,
            String actorUsername,
            GroupDecisionVoteChoice voteChoice) {

        if (groupId == null) {
            throw new RuntimeException("Group ID is required");
        }

        if (decisionId == null) {
            throw new RuntimeException("Decision ID is required");
        }

        if (voteChoice == null) {
            throw new RuntimeException("Vote choice is required");
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

        GroupDecisionEntity decision =
                decisionRepository
                        .findByIdAndGroupId(
                                decisionId,
                                groupId
                        )
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Group decision not found"
                                )
                        );

        LocalDateTime voteTime =
                LocalDateTime.now();

        if (!decision.isVotingOpenAt(voteTime)) {
            throw new RuntimeException(
                    "Voting is not open for this decision"
            );
        }

        GroupDecisionEventType eventType;

        GroupDecisionVoteEntity vote =
                decisionVoteRepository
                        .findByDecisionIdAndVoterUsername(
                                decisionId,
                                normalizedActorUsername
                        )
                        .map(existingVote -> {
                            existingVote.changeVote(
                                    voteChoice,
                                    voteTime
                            );

                            return existingVote;
                        })
                        .orElseGet(
                                () -> new GroupDecisionVoteEntity(
                                        groupId,
                                        decisionId,
                                        normalizedActorUsername,
                                        voteChoice,
                                        voteTime
                                )
                        );

        if (vote.getId() == null) {
            eventType = GroupDecisionEventType.VOTE_CAST;
        } else {
            eventType = GroupDecisionEventType.VOTE_CHANGED;
        }

        GroupDecisionVoteEntity savedVote =
                decisionVoteRepository.save(vote);

        decisionEventRepository.save(
                new GroupDecisionEventEntity(
                        decisionId,
                        groupId,
                        eventType,
                        normalizedActorUsername,
                        voteTime,
                        eventType == GroupDecisionEventType.VOTE_CAST
                                ? "Vote cast: " + voteChoice
                                : "Vote changed to: " + voteChoice
                )
        );

        return savedVote;
    }

    @Transactional(readOnly = true)
    public List<GroupDecisionEntity> getGroupDecisions(
            Long groupId,
            String actorUsername) {

        if (groupId == null) {
            throw new RuntimeException("Group ID is required");
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

        if (
                groupMemberRepository
                        .findByGroupIdAndUsername(
                                groupId,
                                normalizedActorUsername
                        )
                        .isEmpty()
        ) {
            throw new RuntimeException(
                    "You are not a member of this group"
            );
        }

        return decisionRepository
                .findByGroupIdOrderByCreatedAtDesc(groupId);
    }
}
