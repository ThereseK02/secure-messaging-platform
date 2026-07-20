package com.securemessaging.entity;

public enum GroupDecisionEventType {
    CREATED,
    VOTING_OPENED,
    VOTE_CAST,
    VOTE_CHANGED,
    TIE_BREAK_REQUIRED,
    QUORUM_NOT_MET,
    APPROVED,
    REJECTED
}
