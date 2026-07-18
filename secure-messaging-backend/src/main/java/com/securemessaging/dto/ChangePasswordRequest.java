package com.securemessaging.dto;

public record ChangePasswordRequest(
        String currentPassword,
        String newPassword
) {
}
