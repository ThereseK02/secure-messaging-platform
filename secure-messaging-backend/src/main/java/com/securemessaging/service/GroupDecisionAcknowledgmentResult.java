package com.securemessaging.service;

import com.securemessaging.entity.GroupDecisionAcknowledgmentEntity;

public record GroupDecisionAcknowledgmentResult(
        GroupDecisionAcknowledgmentEntity acknowledgment,
        boolean newlyCreated
) {
}
