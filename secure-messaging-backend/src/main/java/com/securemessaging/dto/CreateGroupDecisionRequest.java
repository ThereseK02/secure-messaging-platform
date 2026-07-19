package com.securemessaging.dto;

import com.securemessaging.entity.GroupDecisionGovernanceMode;

public record CreateGroupDecisionRequest(
        GroupDecisionGovernanceMode governanceMode
) {
}
