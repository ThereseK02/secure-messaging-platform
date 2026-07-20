package com.securemessaging.dto;

import java.time.LocalDateTime;

public record OpenGroupDecisionVotingRequest(
        LocalDateTime votingDeadline
) {
}
