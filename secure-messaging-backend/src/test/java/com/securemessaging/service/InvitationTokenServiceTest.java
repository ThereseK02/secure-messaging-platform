package com.securemessaging.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InvitationTokenServiceTest {

    private final InvitationTokenService tokenService =
            new InvitationTokenService();

    @Test
    void generateTokenCreatesUrlSafeRandomToken() {

        String token = tokenService.generateToken();

        assertNotNull(token);
        assertFalse(token.isBlank());
        assertEquals(43, token.length());
        assertTrue(token.matches("^[A-Za-z0-9_-]+$"));
    }

    @Test
    void generateTokenCreatesDifferentTokens() {

        String firstToken = tokenService.generateToken();
        String secondToken = tokenService.generateToken();

        assertNotEquals(firstToken, secondToken);
    }

    @Test
    void hashTokenCreatesDeterministicSha256Hash() {

        String token = tokenService.generateToken();

        String firstHash = tokenService.hashToken(token);
        String secondHash = tokenService.hashToken(token);

        assertEquals(firstHash, secondHash);
        assertEquals(64, firstHash.length());
        assertTrue(firstHash.matches("^[0-9a-f]{64}$"));
    }

    @Test
    void hashTokenCreatesDifferentHashesForDifferentTokens() {

        String firstHash = tokenService.hashToken(
                tokenService.generateToken()
        );

        String secondHash = tokenService.hashToken(
                tokenService.generateToken()
        );

        assertNotEquals(firstHash, secondHash);
    }

    @Test
    void hashTokenRejectsMissingToken() {

        IllegalArgumentException nullException =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> tokenService.hashToken(null)
                );

        assertEquals(
                "Invitation token is required",
                nullException.getMessage()
        );

        IllegalArgumentException blankException =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> tokenService.hashToken("   ")
                );

        assertEquals(
                "Invitation token is required",
                blankException.getMessage()
        );
    }
}