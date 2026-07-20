package com.securemessaging.dto;

import com.securemessaging.entity.GroupDecisionVoteChoice;

public record CastGroupDecisionVoteRequest(
        GroupDecisionVoteChoice voteChoice
) {
}
