package com.securemessaging.dto;

public record LoginRequest(
        String username,
        String password
) {
}
