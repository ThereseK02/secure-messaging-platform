package com.securemessaging.entity;

public enum GroupDecisionStatus {
    DRAFT,
    PROPOSED,
    DISCUSSION_OPEN,
    VOTING_OPEN,
    WAITING_FOR_TIE_BREAK,
    APPROVED,
    REJECTED,
    WITHDRAWN,
    EXPIRED_WITHOUT_QUORUM,
    EXPIRED_WITHOUT_DECISION
}