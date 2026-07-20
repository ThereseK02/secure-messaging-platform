package com.securemessaging.service;

import com.securemessaging.entity.GroupDecisionCategory;
import com.securemessaging.entity.GroupDecisionEntity;
import com.securemessaging.entity.GroupDecisionGovernanceMode;
import com.securemessaging.entity.GroupDecisionStatus;
import com.securemessaging.entity.GroupDecisionThreshold;
import com.securemessaging.entity.GroupDecisionEventEntity;
import com.securemessaging.entity.GroupDecisionEventType;
import com.securemessaging.entity.GroupMemberEntity;
import com.securemessaging.entity.GroupMessageEntity;
import com.securemessaging.entity.GroupRole;
import com.securemessaging.repository.GroupDecisionEventRepository;
import com.securemessaging.repository.GroupDecisionRepository;
import com.securemessaging.repository.GroupMemberEntityRepository;
import com.securemessaging.repository.GroupMessageEntityRepository;
import com.securemessaging.repository.GroupDecisionVoteRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GroupDecisionServiceTest {

    private GroupDecisionRepository decisionRepository;
    private GroupDecisionEventRepository decisionEventRepository;
    private GroupDecisionVoteRepository decisionVoteRepository;
    private GroupMemberEntityRepository groupMemberRepository;
    private GroupMessageEntityRepository groupMessageRepository;
    private GroupDecisionService groupDecisionService;

    @BeforeEach
    void setUp() {

        decisionRepository =
                mock(GroupDecisionRepository.class);

        decisionEventRepository =
                mock(GroupDecisionEventRepository.class);
        decisionVoteRepository =
                mock(GroupDecisionVoteRepository.class);

        groupMemberRepository =
                mock(GroupMemberEntityRepository.class);

        groupMessageRepository =
                mock(GroupMessageEntityRepository.class);

        groupDecisionService =
                new GroupDecisionService(
                        decisionRepository,
                        decisionEventRepository,
                        decisionVoteRepository,
                        groupMemberRepository,
                        groupMessageRepository
                );
    }

    @Test
    void ownerCreatesDecisionAndCreationEvent() {

        Long groupId = 12L;
        Long messageId = 44L;

        GroupMemberEntity owner =
                new GroupMemberEntity(
                        groupId,
                        "Tom",
                        GroupRole.OWNER
                );

        GroupMessageEntity message =
                new GroupMessageEntity(
                        groupId,
                        "Gombo",
                        "Deploy the approved release Friday.",
                        LocalDateTime.now()
                );

        GroupDecisionEntity savedDecision =
                new GroupDecisionEntity(
                        groupId,
                        messageId,
                        "Gombo",
                        "Deploy the approved release Friday.",
                        "Tom",
                        LocalDateTime.now()
                );

        setEntityId(savedDecision, 91L);

        when(
                groupMemberRepository
                        .findByGroupIdAndUsername(
                                groupId,
                                "Tom"
                        )
        ).thenReturn(Optional.of(owner));

        when(groupMessageRepository.findById(messageId))
                .thenReturn(Optional.of(message));

        when(
                decisionRepository
                        .existsBySourceMessageId(messageId)
        ).thenReturn(false);

        when(decisionRepository.save(any(GroupDecisionEntity.class)))
                .thenReturn(savedDecision);

        GroupDecisionEntity result =
                groupDecisionService.createDecision(
                        groupId,
                        messageId,
                        " Tom "
                );

        assertEquals(savedDecision, result);

        ArgumentCaptor<GroupDecisionEntity> decisionCaptor =
                ArgumentCaptor.forClass(
                        GroupDecisionEntity.class
                );

        verify(decisionRepository)
                .save(decisionCaptor.capture());

        GroupDecisionEntity capturedDecision =
                decisionCaptor.getValue();

        assertEquals(groupId, capturedDecision.getGroupId());
        assertEquals(
                messageId,
                capturedDecision.getSourceMessageId()
        );
        assertEquals(
                "Gombo",
                capturedDecision.getSourceSender()
        );
        assertEquals(
                "Deploy the approved release Friday.",
                capturedDecision.getDecisionTextSnapshot()
        );
        assertEquals(
                "Tom",
                capturedDecision.getCreatedBy()
        );
        assertEquals(
                GroupDecisionStatus.PROPOSED,
                capturedDecision.getStatus()
        );
        assertEquals(
                GroupDecisionCategory.ROUTINE_OPERATION,
                capturedDecision.getCategory()
        );
        assertEquals(
                GroupDecisionThreshold.SIMPLE_MAJORITY,
                capturedDecision.getThreshold()
        );
        assertEquals(
                GroupDecisionGovernanceMode.OWNER_REVIEW,
                capturedDecision.getGovernanceMode()
        );
        assertNull(capturedDecision.getVotingDeadline());
        assertNull(capturedDecision.getTieBreakDeadline());

        ArgumentCaptor<GroupDecisionEventEntity> eventCaptor =
                ArgumentCaptor.forClass(
                        GroupDecisionEventEntity.class
                );

        verify(decisionEventRepository)
                .save(eventCaptor.capture());

        GroupDecisionEventEntity capturedEvent =
                eventCaptor.getValue();

        assertEquals(
                savedDecision.getId(),
                capturedEvent.getDecisionId()
        );
        assertEquals(groupId, capturedEvent.getGroupId());
        assertEquals(
                "Tom",
                capturedEvent.getActorUsername()
        );
        assertTrue(
                capturedEvent
                        .getEventDetails()
                        .contains(messageId.toString())
        );
    }

    @Test
    void ownerMaySelectOwnerLedGovernance() {

        Long groupId = 12L;
        Long messageId = 44L;

        GroupMemberEntity owner =
                new GroupMemberEntity(
                        groupId,
                        "Tom",
                        GroupRole.OWNER
                );

        when(
                groupMemberRepository
                        .findByGroupIdAndUsername(
                                groupId,
                                "Tom"
                        )
        ).thenReturn(Optional.of(owner));

        when(
                groupMessageRepository.findById(messageId)
        ).thenReturn(Optional.empty());

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> groupDecisionService.createDecision(
                                groupId,
                                messageId,
                                "Tom",
                                GroupDecisionGovernanceMode.OWNER_LED
                        )
                );

        assertEquals(
                "Group message not found",
                exception.getMessage()
        );

        verify(groupMessageRepository)
                .findById(messageId);
    }

    @Test
    void regularMemberCannotSelectOwnerLedGovernance() {

        Long groupId = 12L;

        GroupMemberEntity member =
                new GroupMemberEntity(
                        groupId,
                        "Kelly",
                        GroupRole.MEMBER
                );

        when(
                groupMemberRepository
                        .findByGroupIdAndUsername(
                                groupId,
                                "Kelly"
                        )
        ).thenReturn(Optional.of(member));

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> groupDecisionService.createDecision(
                                groupId,
                                44L,
                                "Kelly",
                                GroupDecisionGovernanceMode.OWNER_LED
                        )
                );

        assertEquals(
                "Only the group owner can select owner-led governance",
                exception.getMessage()
        );

        verify(groupMessageRepository, never())
                .findById(any());
    }

    @Test
    void regularMemberMaySelectMemberVoteGovernance() {

        Long groupId = 12L;
        Long messageId = 44L;

        GroupMemberEntity member =
                new GroupMemberEntity(
                        groupId,
                        "Kelly",
                        GroupRole.MEMBER
                );

        when(
                groupMemberRepository
                        .findByGroupIdAndUsername(
                                groupId,
                                "Kelly"
                        )
        ).thenReturn(Optional.of(member));

        when(
                groupMessageRepository.findById(messageId)
        ).thenReturn(Optional.empty());

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> groupDecisionService.createDecision(
                                groupId,
                                messageId,
                                "Kelly",
                                GroupDecisionGovernanceMode.MEMBER_VOTE
                        )
                );

        assertEquals(
                "Group message not found",
                exception.getMessage()
        );

        verify(groupMessageRepository)
                .findById(messageId);
    }

    @Test
    void regularMemberPassesProposalAuthorization() {

        Long groupId = 12L;
        Long messageId = 44L;

        GroupMemberEntity member =
                new GroupMemberEntity(
                        groupId,
                        "Kelly",
                        GroupRole.MEMBER
                );

        when(
                groupMemberRepository
                        .findByGroupIdAndUsername(
                                groupId,
                                "Kelly"
                        )
        ).thenReturn(Optional.of(member));

        when(
                groupMessageRepository.findById(messageId)
        ).thenReturn(Optional.empty());

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> groupDecisionService.createDecision(
                                groupId,
                                messageId,
                                "Kelly"
                        )
                );

        assertEquals(
                "Group message not found",
                exception.getMessage()
        );

        verify(groupMessageRepository)
                .findById(messageId);

        verify(decisionRepository, never())
                .save(any());

        verify(decisionEventRepository, never())
                .save(any());
    }

    @Test
    void messageFromAnotherGroupCannotBecomeDecision() {

        Long groupId = 12L;
        Long messageId = 44L;

        GroupMemberEntity admin =
                new GroupMemberEntity(
                        groupId,
                        "Kelly",
                        GroupRole.ADMIN
                );

        GroupMessageEntity message =
                new GroupMessageEntity(
                        99L,
                        "Gombo",
                        "Unrelated group message",
                        LocalDateTime.now()
                );

        when(
                groupMemberRepository
                        .findByGroupIdAndUsername(
                                groupId,
                                "Kelly"
                        )
        ).thenReturn(Optional.of(admin));

        when(groupMessageRepository.findById(messageId))
                .thenReturn(Optional.of(message));

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> groupDecisionService.createDecision(
                                groupId,
                                messageId,
                                "Kelly"
                        )
                );

        assertEquals(
                "Group message does not belong to this group",
                exception.getMessage()
        );

        verify(decisionRepository, never())
                .save(any());

        verify(decisionEventRepository, never())
                .save(any());
    }

    @Test
    void duplicateDecisionIsRejected() {

        Long groupId = 12L;
        Long messageId = 44L;

        GroupMemberEntity owner =
                new GroupMemberEntity(
                        groupId,
                        "Tom",
                        GroupRole.OWNER
                );

        GroupMessageEntity message =
                new GroupMessageEntity(
                        groupId,
                        "Tom",
                        "This message is already a decision.",
                        LocalDateTime.now()
                );

        when(
                groupMemberRepository
                        .findByGroupIdAndUsername(
                                groupId,
                                "Tom"
                        )
        ).thenReturn(Optional.of(owner));

        when(groupMessageRepository.findById(messageId))
                .thenReturn(Optional.of(message));

        when(
                decisionRepository
                        .existsBySourceMessageId(messageId)
        ).thenReturn(true);

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> groupDecisionService.createDecision(
                                groupId,
                                messageId,
                                "Tom"
                        )
                );

        assertEquals(
                "This group message is already a decision",
                exception.getMessage()
        );

        verify(decisionRepository, never())
                .save(any());

        verify(decisionEventRepository, never())
                .save(any());
    }

    @Test
    void ownerOpensMemberVotingAndCreatesEvent() {

        Long groupId = 12L;
        Long decisionId = 91L;

        LocalDateTime votingDeadline =
                LocalDateTime.now().plusDays(2);

        GroupMemberEntity owner =
                new GroupMemberEntity(
                        groupId,
                        "Tom",
                        GroupRole.OWNER
                );

        GroupDecisionEntity decision =
                new GroupDecisionEntity(
                        groupId,
                        44L,
                        "Gombo",
                        "Deploy the release Friday.",
                        "Gombo",
                        GroupDecisionGovernanceMode.MEMBER_VOTE,
                        LocalDateTime.now()
                );

        setEntityId(decision, decisionId);

        when(
                groupMemberRepository
                        .findByGroupIdAndUsername(
                                groupId,
                                "Tom"
                        )
        ).thenReturn(Optional.of(owner));

        when(
                decisionRepository.findByIdAndGroupId(
                        decisionId,
                        groupId
                )
        ).thenReturn(Optional.of(decision));

        when(decisionRepository.save(decision))
                .thenReturn(decision);

        GroupDecisionEntity result =
                groupDecisionService.openVoting(
                        groupId,
                        decisionId,
                        " Tom ",
                        votingDeadline
                );

        assertEquals(decision, result);

        assertEquals(
                GroupDecisionStatus.VOTING_OPEN,
                result.getStatus()
        );

        assertEquals(
                votingDeadline,
                result.getVotingDeadline()
        );

        verify(decisionRepository)
                .save(decision);

        ArgumentCaptor<GroupDecisionEventEntity> eventCaptor =
                ArgumentCaptor.forClass(
                        GroupDecisionEventEntity.class
                );

        verify(decisionEventRepository)
                .save(eventCaptor.capture());

        GroupDecisionEventEntity capturedEvent =
                eventCaptor.getValue();

        assertEquals(
                GroupDecisionEventType.VOTING_OPENED,
                capturedEvent.getEventType()
        );

        assertEquals(
                decisionId,
                capturedEvent.getDecisionId()
        );

        assertEquals(
                groupId,
                capturedEvent.getGroupId()
        );

        assertEquals(
                "Tom",
                capturedEvent.getActorUsername()
        );

        assertTrue(
                capturedEvent
                        .getEventDetails()
                        .contains(votingDeadline.toString())
        );
    }

    @Test
    void regularMemberCannotOpenVoting() {

        Long groupId = 12L;
        Long decisionId = 91L;

        GroupMemberEntity member =
                new GroupMemberEntity(
                        groupId,
                        "Kelly",
                        GroupRole.MEMBER
                );

        when(
                groupMemberRepository
                        .findByGroupIdAndUsername(
                                groupId,
                                "Kelly"
                        )
        ).thenReturn(Optional.of(member));

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> groupDecisionService.openVoting(
                                groupId,
                                decisionId,
                                "Kelly",
                                LocalDateTime.now().plusDays(2)
                        )
                );

        assertEquals(
                "Only the group owner or an admin can open voting",
                exception.getMessage()
        );

        verify(decisionRepository, never())
                .findByIdAndGroupId(
                        any(),
                        any()
                );

        verify(decisionRepository, never())
                .save(any());

        verify(decisionEventRepository, never())
                .save(any());
    }

    private void setEntityId(
            GroupDecisionEntity entity,
            Long id) {

        try {
            var idField =
                    GroupDecisionEntity.class
                            .getDeclaredField("id");

            idField.setAccessible(true);
            idField.set(entity, id);

        } catch (ReflectiveOperationException exception) {
            throw new RuntimeException(exception);
        }
    }
}