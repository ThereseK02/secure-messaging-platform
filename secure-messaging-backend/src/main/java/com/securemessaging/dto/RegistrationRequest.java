package com.securemessaging.dto;

public record RegistrationRequest(
        String username,
        String email,
        String password
) {
}
