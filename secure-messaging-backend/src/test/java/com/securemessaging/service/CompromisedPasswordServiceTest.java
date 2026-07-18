package com.securemessaging.service;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.HexFormat;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CompromisedPasswordServiceTest {

    private static final URI TEST_ENDPOINT =
            URI.create(
                    "https://example.test/range/"
            );

    private static final Duration TEST_TIMEOUT =
            Duration.ofSeconds(2);

    @Test
    void detectsCompromisedPassword()
            throws Exception {

        String password =
                "known breached test password";

        String suffix =
                sha1Hex(password).substring(5);

        CompromisedPasswordService service =
                serviceReturning(
                        200,
                        suffix + ":42\r\n"
                );

        assertEquals(
                CompromisedPasswordService.CheckResult.COMPROMISED,
                service.check(password)
        );
    }

    @Test
    void returnsNotCompromisedWhenSuffixIsAbsent() {

        CompromisedPasswordService service =
                serviceReturning(
                        200,
                        "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA:10\r\n"
                );

        assertEquals(
                CompromisedPasswordService.CheckResult.NOT_COMPROMISED,
                service.check(
                        "a different test password"
                )
        );
    }

    @Test
    void ignoresPaddedZeroCountMatch()
            throws Exception {

        String password =
                "padding test password";

        String suffix =
                sha1Hex(password).substring(5);

        CompromisedPasswordService service =
                serviceReturning(
                        200,
                        suffix + ":0\r\n"
                );

        assertEquals(
                CompromisedPasswordService.CheckResult.NOT_COMPROMISED,
                service.check(password)
        );
    }

    @Test
    void sendsOnlyFiveCharacterPrefixAndRequestsPadding()
            throws Exception {

        String password =
                "request privacy test password";

        String fullHash =
                sha1Hex(password);

        AtomicReference<HttpRequest> capturedRequest =
                new AtomicReference<>();

        CompromisedPasswordService service =
                new CompromisedPasswordService(
                        request -> {
                            capturedRequest.set(request);

                            return new CompromisedPasswordService.RangeResponse(
                                    200,
                                    ""
                            );
                        },
                        TEST_ENDPOINT,
                        TEST_TIMEOUT
                );

        service.check(password);

        HttpRequest request =
                capturedRequest.get();

        String requestUri =
                request.uri().toString();

        assertTrue(
                requestUri.endsWith(
                        fullHash.substring(0, 5)
                )
        );

        assertFalse(
                requestUri.contains(
                        fullHash.substring(5)
                )
        );

        assertFalse(
                requestUri.contains(password)
        );

        assertEquals(
                "true",
                request.headers()
                        .firstValue("Add-Padding")
                        .orElse("")
        );
    }

    @Test
    void returnsUnavailableForNonSuccessfulResponse() {

        CompromisedPasswordService service =
                serviceReturning(
                        503,
                        ""
                );

        assertEquals(
                CompromisedPasswordService.CheckResult.UNAVAILABLE,
                service.check(
                        "service unavailable test password"
                )
        );
    }

    @Test
    void returnsUnavailableWhenRequestFails() {

        CompromisedPasswordService service =
                new CompromisedPasswordService(
                        request -> {
                            throw new IOException(
                                    "Simulated network failure"
                            );
                        },
                        TEST_ENDPOINT,
                        TEST_TIMEOUT
                );

        assertEquals(
                CompromisedPasswordService.CheckResult.UNAVAILABLE,
                service.check(
                        "network failure test password"
                )
        );
    }

    private CompromisedPasswordService serviceReturning(
            int statusCode,
            String body) {

        return new CompromisedPasswordService(
                request ->
                        new CompromisedPasswordService.RangeResponse(
                                statusCode,
                                body
                        ),
                TEST_ENDPOINT,
                TEST_TIMEOUT
        );
    }

    private String sha1Hex(String password)
            throws Exception {

        MessageDigest digest =
                MessageDigest.getInstance("SHA-1");

        byte[] hash =
                digest.digest(
                        password.getBytes(
                                StandardCharsets.UTF_8
                        )
                );

        return HexFormat.of()
                .withUpperCase()
                .formatHex(hash);
    }
}
