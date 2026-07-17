package com.securemessaging.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoginAttemptServiceTest {

    private LoginAttemptService loginAttemptService;

    @BeforeEach
    void setUp() {
        loginAttemptService =
                new LoginAttemptService();
    }

    @Test
    void userIsBlockedAfterFiveFailures() {

        for (int attempt = 1; attempt <= 4; attempt++) {
            loginAttemptService.recordFailure("Tom");

            assertFalse(
                    loginAttemptService.isBlocked("Tom")
            );
        }

        loginAttemptService.recordFailure("Tom");

        assertTrue(
                loginAttemptService.isBlocked("Tom")
        );

        assertEquals(
                5,
                loginAttemptService.getFailedAttempts("Tom")
        );
    }

    @Test
    void successfulLoginClearsFailedAttempts() {

        loginAttemptService.recordFailure("Tom");
        loginAttemptService.recordFailure("Tom");

        assertEquals(
                2,
                loginAttemptService.getFailedAttempts("Tom")
        );

        loginAttemptService.recordSuccess("Tom");

        assertEquals(
                0,
                loginAttemptService.getFailedAttempts("Tom")
        );

        assertFalse(
                loginAttemptService.isBlocked("Tom")
        );
    }

    @Test
    void usernameNormalizationPreventsCaseAndWhitespaceBypass() {

        loginAttemptService.recordFailure(" Tom ");
        loginAttemptService.recordFailure("TOM");
        loginAttemptService.recordFailure("tom");
        loginAttemptService.recordFailure(" ToM ");
        loginAttemptService.recordFailure("tom");

        assertTrue(
                loginAttemptService.isBlocked(" TOM ")
        );

        assertEquals(
                5,
                loginAttemptService.getFailedAttempts("tom")
        );
    }

    @Test
    void blankUsernameIsNotTracked() {

        loginAttemptService.recordFailure("   ");
        loginAttemptService.recordFailure(null);

        assertEquals(
                0,
                loginAttemptService.getFailedAttempts("")
        );

        assertFalse(
                loginAttemptService.isBlocked("   ")
        );
    }
}
