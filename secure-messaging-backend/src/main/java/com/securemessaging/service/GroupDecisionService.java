package com.securemessaging.service;

import com.securemessaging.entity.GroupDecisionAcknowledgmentEntity;
import com.securemessaging.entity.GroupDecisionAcknowledgmentRequirementEntity;
import com.securemessaging.entity.GroupDecisionEntity;
import com.securemessaging.entity.GroupDecisionEventEntity;
import com.securemessaging.entity.GroupDecisionEventType;
import com.securemessaging.entity.GroupDecisionGovernanceMode;
import com.securemessaging.entity.GroupDecisionStatus;
import com.securemessaging.entity.GroupDecisionThreshold;
import com.securemessaging.entity.GroupDecisionVoteChoice;
import com.securemessaging.entity.GroupDecisionVoteEntity;
import com.securemessaging.entity.GroupMemberEntity;
import com.securemessaging.entity.GroupMessageEntity;
import com.securemessaging.entity.GroupRole;
import com.securemessaging.repository.GroupDecisionAcknowledgmentRepository;
import com.securemessaging.repository.GroupDecisionAcknowledgmentRequirementRepository;
import com.securemessaging.repository.GroupDecisionEventRepository;
import com.securemessaging.repository.GroupDecisionRepository;
import com.securemessaging.repository.GroupMemberEntityRepository;
import com.securemessaging.repository.GroupMessageEntityRepository;
import com.securemessaging.repository.GroupDecisionVoteRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class GroupDecisionService {

    private final GroupDecisionRepository decisionRepository;
    private final GroupDecisionEventRepository decisionEventRepository;
    private final GroupDecisionVoteRepository decisionVoteRepository;
    private final GroupDecisionAcknowledgmentRepository
            decisionAcknowledgmentRepository;
    private final GroupDecisionAcknowledgmentRequirementRepository
            decisionAcknowledgmentRequirementRepository;
    private final GroupMemberEntityRepository groupMemberRepository;
    private final GroupMessageEntityRepository groupMessageRepository;

    public GroupDecisionService(
            GroupDecisionRepository decisionRepository,
            GroupDecisionEventRepository decisionEventRepository,
            GroupDecisionVoteRepository decisionVoteRepository,
            GroupDecisionAcknowledgmentRepository
                    decisionAcknowledgmentRepository,
            GroupDecisionAcknowledgmentRequirementRepository
                    decisionAcknowledgmentRequirementRepository,
            GroupMemberEntityRepository groupMemberRepository,
            GroupMessageEntityRepository groupMessageRepository) {

        this.decisionRepository = decisionRepository;
        this.decisionEventRepository = decisionEventRepository;
        this.decisionVoteRepository = decisionVoteRepository;
        this.decisionAcknowledgmentRepository =
                decisionAcknowledgmentRepository;
        this.decisionAcknowledgmentRequirementRepository =
                decisionAcknowledgmentRequirementRepository;
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

        if (
                governanceMode ==
                        GroupDecisionGovernanceMode.OWNER_LED
        ) {
            decisionEventRepository.save(
                    new GroupDecisionEventEntity(
                            savedDecision.getId(),
                            groupId,
                            GroupDecisionEventType.DISCUSSION_OPENED,
                            normalizedActorUsername,
                            createdAt,
                            "Owner-led consultation opened for member discussion"
                    )
            );
        }

        return savedDecision;
    }

    @Transactional
    public GroupDecisionEntity approveOwnerLedDecision(
            Long groupId,
            Long decisionId,
            String actorUsername) {

        return finalizeOwnerLedDecision(
                groupId,
                decisionId,
                actorUsername,
                GroupDecisionStatus.APPROVED
        );
    }

    @Transactional
    public GroupDecisionEntity rejectOwnerLedDecision(
            Long groupId,
            Long decisionId,
            String actorUsername) {

        return finalizeOwnerLedDecision(
                groupId,
                decisionId,
                actorUsername,
                GroupDecisionStatus.REJECTED
        );
    }

    @Transactional
    public GroupDecisionEntity withdrawOwnerLedDecision(
            Long groupId,
            Long decisionId,
            String actorUsername) {

        return finalizeOwnerLedDecision(
                groupId,
                decisionId,
                actorUsername,
                GroupDecisionStatus.WITHDRAWN
        );
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

    private GroupDecisionEntity finalizeOwnerLedDecision(
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
                    "Only the group owner can finalize an owner-led decision"
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
                decision.approveOwnerLedDecision();
            } else if (targetStatus == GroupDecisionStatus.REJECTED) {
                decision.rejectOwnerLedDecision();
            } else if (targetStatus == GroupDecisionStatus.WITHDRAWN) {
                decision.withdrawOwnerLedDecision();
            } else {
                throw new IllegalArgumentException(
                        "Unsupported owner-led decision status"
                );
            }
        } catch (IllegalStateException exception) {
            throw new RuntimeException(exception.getMessage());
        }

        GroupDecisionEntity savedDecision =
                decisionRepository.save(decision);

        LocalDateTime eventAt = LocalDateTime.now();

        GroupDecisionEventType eventType;

        String eventDetails;

        if (targetStatus == GroupDecisionStatus.APPROVED) {
            eventType = GroupDecisionEventType.APPROVED;
            eventDetails =
                    "Owner-led decision approved after member consultation";
        } else if (targetStatus == GroupDecisionStatus.REJECTED) {
            eventType = GroupDecisionEventType.REJECTED;
            eventDetails =
                    "Owner-led decision rejected after member consultation";
        } else {
            eventType = GroupDecisionEventType.WITHDRAWN;
            eventDetails =
                    "Owner-led decision withdrawn after member consultation";
        }
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

        createAcknowledgmentRequirementSnapshot(
                groupId,
                savedDecision.getId(),
                eventAt
        );

        return savedDecision;
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

        createAcknowledgmentRequirementSnapshot(
                groupId,
                savedDecision.getId(),
                eventAt
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
                        "SYSTEM",
                        voteTime,
                        eventType == GroupDecisionEventType.VOTE_CAST
                                ? "Secret ballot submitted"
                                : "Secret ballot updated"
                )
        );

        return savedVote;
    }

    @Transactional
    public GroupDecisionEntity resolveMemberVote(
            Long groupId,
            Long decisionId,
            String actorUsername) {

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
                    "Only the group owner or an admin can resolve voting"
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

        if (
                decision.getThreshold() !=
                        GroupDecisionThreshold.SIMPLE_MAJORITY
        ) {
            throw new RuntimeException(
                    "Only simple-majority resolution is currently supported"
            );
        }

        LocalDateTime resolvedAt =
                LocalDateTime.now();

        long totalMembers =
                groupMemberRepository
                        .findByGroupId(groupId)
                        .size();

        long totalVotes =
                decisionVoteRepository
                        .countByDecisionId(decisionId);

        long approveVotes =
                decisionVoteRepository
                        .countByDecisionIdAndVoteChoice(
                                decisionId,
                                GroupDecisionVoteChoice.APPROVE
                        );

        long rejectVotes =
                decisionVoteRepository
                        .countByDecisionIdAndVoteChoice(
                                decisionId,
                                GroupDecisionVoteChoice.REJECT
                        );

        long abstainVotes =
                decisionVoteRepository
                        .countByDecisionIdAndVoteChoice(
                                decisionId,
                                GroupDecisionVoteChoice.ABSTAIN
                        );

        long quorumRequired =
                (totalMembers / 2) + 1;

        GroupDecisionStatus outcomeStatus;

        if (totalVotes < quorumRequired) {
            outcomeStatus =
                    GroupDecisionStatus.EXPIRED_WITHOUT_QUORUM;
        } else if (approveVotes > rejectVotes) {
            outcomeStatus =
                    GroupDecisionStatus.APPROVED;
        } else if (rejectVotes > approveVotes) {
            outcomeStatus =
                    GroupDecisionStatus.REJECTED;
        } else {
            outcomeStatus =
                    GroupDecisionStatus.WAITING_FOR_TIE_BREAK;
        }

        try {
            decision.resolveMemberVote(
                    outcomeStatus,
                    resolvedAt
            );
        } catch (
                IllegalStateException |
                IllegalArgumentException exception
        ) {
            throw new RuntimeException(exception.getMessage());
        }

        GroupDecisionEntity savedDecision =
                decisionRepository.save(decision);

        GroupDecisionEventType eventType;

        if (outcomeStatus == GroupDecisionStatus.APPROVED) {
            eventType = GroupDecisionEventType.APPROVED;
        } else if (
                outcomeStatus == GroupDecisionStatus.REJECTED
        ) {
            eventType = GroupDecisionEventType.REJECTED;
        } else if (
                outcomeStatus ==
                        GroupDecisionStatus.WAITING_FOR_TIE_BREAK
        ) {
            eventType =
                    GroupDecisionEventType.TIE_BREAK_REQUIRED;
        } else {
            eventType =
                    GroupDecisionEventType.QUORUM_NOT_MET;
        }

        String eventDetails =
                "Voting resolved: members=" + totalMembers +
                        ", quorumRequired=" + quorumRequired +
                        ", totalVotes=" + totalVotes +
                        ", approve=" + approveVotes +
                        ", reject=" + rejectVotes +
                        ", abstain=" + abstainVotes +
                        ", outcome=" + outcomeStatus;

        decisionEventRepository.save(
                new GroupDecisionEventEntity(
                        decisionId,
                        groupId,
                        eventType,
                        normalizedActorUsername,
                        resolvedAt,
                        eventDetails
                )
        );

        if (
                outcomeStatus == GroupDecisionStatus.APPROVED ||
                        outcomeStatus == GroupDecisionStatus.REJECTED
        ) {
            createAcknowledgmentRequirementSnapshot(
                    groupId,
                    savedDecision.getId(),
                    resolvedAt
            );
        }

        return savedDecision;
    }

    @Transactional
    public GroupDecisionEntity resolveTieBreak(
            Long groupId,
            Long decisionId,
            String actorUsername,
            GroupDecisionVoteChoice tieBreakChoice) {

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
                    "Only the group owner can resolve a tie"
            );
        }

        if (tieBreakChoice == null) {
            throw new RuntimeException(
                    "Tie-break choice is required"
            );
        }

        if (
                tieBreakChoice !=
                        GroupDecisionVoteChoice.APPROVE &&
                        tieBreakChoice !=
                                GroupDecisionVoteChoice.REJECT
        ) {
            throw new RuntimeException(
                    "Tie-break choice must be APPROVE or REJECT"
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

        LocalDateTime resolvedAt = LocalDateTime.now();

        GroupDecisionStatus finalStatus =
                tieBreakChoice ==
                        GroupDecisionVoteChoice.APPROVE
                        ? GroupDecisionStatus.APPROVED
                        : GroupDecisionStatus.REJECTED;

        try {
            decision.resolveTieBreak(
                    finalStatus,
                    resolvedAt
            );
        } catch (
                IllegalStateException |
                IllegalArgumentException exception
        ) {
            throw new RuntimeException(exception.getMessage());
        }

        GroupDecisionEntity savedDecision =
                decisionRepository.save(decision);

        GroupDecisionEventType eventType =
                finalStatus == GroupDecisionStatus.APPROVED
                        ? GroupDecisionEventType.APPROVED
                        : GroupDecisionEventType.REJECTED;

        decisionEventRepository.save(
                new GroupDecisionEventEntity(
                        decisionId,
                        groupId,
                        eventType,
                        normalizedActorUsername,
                        resolvedAt,
                        "Tie-break resolved: outcome=" + finalStatus
                )
        );

        createAcknowledgmentRequirementSnapshot(
                groupId,
                savedDecision.getId(),
                resolvedAt
        );

        return savedDecision;
    }

    private void createAcknowledgmentRequirementSnapshot(
            Long groupId,
            Long decisionId,
            LocalDateTime requiredAt) {

        if (
                decisionAcknowledgmentRequirementRepository
                        .existsByDecisionId(decisionId)
        ) {
            return;
        }

        List<GroupDecisionAcknowledgmentRequirementEntity> requirements =
                groupMemberRepository
                        .findByGroupId(groupId)
                        .stream()
                        .map(
                                member ->
                                        new GroupDecisionAcknowledgmentRequirementEntity(
                                                decisionId,
                                                groupId,
                                                member.getUsername(),
                                                requiredAt
                                        )
                        )
                        .toList();

        decisionAcknowledgmentRequirementRepository
                .saveAll(requirements);
    }


    @Transactional
    public GroupDecisionAcknowledgmentResult acknowledgeDecision(
            Long groupId,
            Long decisionId,
            String actorUsername) {

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

        GroupDecisionStatus decisionStatus =
                decision.getStatus();

        if (
                decisionStatus != GroupDecisionStatus.APPROVED &&
                        decisionStatus != GroupDecisionStatus.REJECTED &&
                        decisionStatus != GroupDecisionStatus.WITHDRAWN
        ) {
            throw new RuntimeException(
                    "Only a finalized decision can be acknowledged"
            );
        }

        if (
                !decisionAcknowledgmentRequirementRepository
                        .existsByDecisionIdAndUsername(
                                decisionId,
                                normalizedActorUsername
                        )
        ) {
            throw new RuntimeException(
                    "You are not required to acknowledge this decision"
            );
        }

        Optional<GroupDecisionAcknowledgmentEntity> existingAcknowledgment =
                decisionAcknowledgmentRepository
                        .findByDecisionIdAndUsername(
                                decisionId,
                                normalizedActorUsername
                        );

        if (existingAcknowledgment.isPresent()) {
            return new GroupDecisionAcknowledgmentResult(
                    existingAcknowledgment.get(),
                    false
            );
        }

        LocalDateTime acknowledgedAt = LocalDateTime.now();

        GroupDecisionAcknowledgmentEntity acknowledgment =
                decisionAcknowledgmentRepository.save(
                        new GroupDecisionAcknowledgmentEntity(
                                decisionId,
                                groupId,
                                normalizedActorUsername,
                                acknowledgedAt
                        )
                );

        decisionEventRepository.save(
                new GroupDecisionEventEntity(
                        decisionId,
                        groupId,
                        GroupDecisionEventType.ACKNOWLEDGED,
                        normalizedActorUsername,
                        acknowledgedAt,
                        "Decision acknowledged: status=" + decisionStatus
                )
        );

        return new GroupDecisionAcknowledgmentResult(
                acknowledgment,
                true
        );
    }

    @Transactional(readOnly = true)
    public long getRequiredAcknowledgmentCount(
            Long groupId,
            Long decisionId,
            String actorUsername) {

        validateDecisionReadAccess(
                groupId,
                decisionId,
                actorUsername
        );

        return decisionAcknowledgmentRequirementRepository
                .countByDecisionId(decisionId);
    }


    @Transactional(readOnly = true)
    public long getAcknowledgmentCount(
            Long groupId,
            Long decisionId,
            String actorUsername) {

        validateDecisionReadAccess(
                groupId,
                decisionId,
                actorUsername
        );

        return decisionAcknowledgmentRepository
                .countByDecisionId(decisionId);
    }

    @Transactional(readOnly = true)
    public Optional<GroupDecisionAcknowledgmentEntity>
    getAcknowledgmentForUser(
            Long groupId,
            Long decisionId,
            String actorUsername) {

        String normalizedActorUsername =
                validateDecisionReadAccess(
                        groupId,
                        decisionId,
                        actorUsername
                );

        return decisionAcknowledgmentRepository
                .findByDecisionIdAndUsername(
                        decisionId,
                        normalizedActorUsername
                );
    }

    private String validateDecisionReadAccess(
            Long groupId,
            Long decisionId,
            String actorUsername) {

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

        if (
                decisionRepository
                        .findByIdAndGroupId(
                                decisionId,
                                groupId
                        )
                        .isEmpty()
        ) {
            throw new RuntimeException(
                    "Group decision not found"
            );
        }

        return normalizedActorUsername;
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
