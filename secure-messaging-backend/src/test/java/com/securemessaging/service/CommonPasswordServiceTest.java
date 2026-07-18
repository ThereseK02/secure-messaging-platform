package com.securemessaging.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommonPasswordServiceTest {

    private CommonPasswordService commonPasswordService;

    @BeforeEach
    void setUp() {
        commonPasswordService =
                new CommonPasswordService();
    }

    @Test
    void blocksPasswordFromLocalBlocklist() {

        assertTrue(
                commonPasswordService.isBlocked(
                        "Password123456",
                        "Tom",
                        "tom@example.com"
                )
        );
    }

    @Test
    void blocklistComparisonIgnoresCaseAndOuterWhitespace() {

        assertTrue(
                commonPasswordService.isBlocked(
                        "  PASSWORD123456  ",
                        "Tom",
                        "tom@example.com"
                )
        );
    }

    @Test
    void blocksUsernameBasedPassword() {

        assertTrue(
                commonPasswordService.isBlocked(
                        "longusername2026!",
                        "LongUsername",
                        "person@example.com"
                )
        );
    }

    @Test
    void blocksEmailLocalPartBasedPassword() {

        assertTrue(
                commonPasswordService.isBlocked(
                        "securitystudent123!",
                        "DifferentUsername",
                        "securitystudent@example.com"
                )
        );
    }

    @Test
    void allowsNonListedPassphrase() {

        assertFalse(
                commonPasswordService.isBlocked(
                        "orchids travel quietly after midnight",
                        "Tom",
                        "tom@example.com"
                )
        );
    }
}
