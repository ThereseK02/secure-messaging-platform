package com.securemessaging.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.HexFormat;
import java.util.Locale;

@Service
public class CompromisedPasswordService {

    public enum CheckResult {
        COMPROMISED,
        NOT_COMPROMISED,
        UNAVAILABLE
    }

    @FunctionalInterface
    interface RangeClient {

        RangeResponse fetch(HttpRequest request)
                throws Exception;
    }

    record RangeResponse(
            int statusCode,
            String body) {
    }

    private static final Logger logger =
            LoggerFactory.getLogger(
                    CompromisedPasswordService.class
            );

    private static final URI RANGE_ENDPOINT =
            URI.create(
                    "https://api.pwnedpasswords.com/range/"
            );

    private static final Duration CONNECT_TIMEOUT =
            Duration.ofSeconds(3);

    private static final Duration REQUEST_TIMEOUT =
            Duration.ofSeconds(5);

    private static final String USER_AGENT =
            "SecureMessagingPlatform-PasswordScreening";

    private final RangeClient rangeClient;
    private final URI rangeEndpoint;
    private final Duration requestTimeout;

    public CompromisedPasswordService() {

        HttpClient httpClient =
                HttpClient.newBuilder()
                        .connectTimeout(CONNECT_TIMEOUT)
                        .build();

        this.rangeClient =
                request -> {

                    HttpResponse<String> response =
                            httpClient.send(
                                    request,
                                    HttpResponse.BodyHandlers.ofString(
                                            StandardCharsets.UTF_8
                                    )
                            );

                    return new RangeResponse(
                            response.statusCode(),
                            response.body()
                    );
                };

        this.rangeEndpoint =
                RANGE_ENDPOINT;

        this.requestTimeout =
                REQUEST_TIMEOUT;
    }

    CompromisedPasswordService(
            RangeClient rangeClient,
            URI rangeEndpoint,
            Duration requestTimeout) {

        this.rangeClient = rangeClient;
        this.rangeEndpoint = rangeEndpoint;
        this.requestTimeout = requestTimeout;
    }

    public CheckResult check(String password) {

        if (password == null || password.isEmpty()) {
            return CheckResult.NOT_COMPROMISED;
        }

        try {
            String fullHash =
                    sha1Hex(password);

            String prefix =
                    fullHash.substring(0, 5);

            String expectedSuffix =
                    fullHash.substring(5);

            HttpRequest request =
                    HttpRequest.newBuilder(
                                    rangeEndpoint.resolve(prefix)
                            )
                            .timeout(requestTimeout)
                            .header(
                                    "Add-Padding",
                                    "true"
                            )
                            .header(
                                    "User-Agent",
                                    USER_AGENT
                            )
                            .GET()
                            .build();

            RangeResponse response =
                    rangeClient.fetch(request);

            if (response.statusCode() != 200) {
                logger.warn(
                        "Compromised-password service returned HTTP {}",
                        response.statusCode()
                );

                return CheckResult.UNAVAILABLE;
            }

            return containsCompromisedSuffix(
                    response.body(),
                    expectedSuffix
            )
                    ? CheckResult.COMPROMISED
                    : CheckResult.NOT_COMPROMISED;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            logger.warn(
                    "Compromised-password check was interrupted"
            );

            return CheckResult.UNAVAILABLE;

        } catch (Exception e) {
            logger.warn(
                    "Compromised-password service is unavailable: {}",
                    e.getClass().getSimpleName()
            );

            return CheckResult.UNAVAILABLE;
        }
    }

    private boolean containsCompromisedSuffix(
            String responseBody,
            String expectedSuffix) {

        if (responseBody == null || responseBody.isBlank()) {
            return false;
        }

        String normalizedExpectedSuffix =
                expectedSuffix.toUpperCase(Locale.ROOT);

        for (String line : responseBody.split("\\R")) {

            String trimmedLine =
                    line.trim();

            if (trimmedLine.isBlank()) {
                continue;
            }

            String[] parts =
                    trimmedLine.split(":", 2);

            if (parts.length != 2) {
                continue;
            }

            String returnedSuffix =
                    parts[0]
                            .trim()
                            .toUpperCase(Locale.ROOT);

            long occurrenceCount;

            try {
                occurrenceCount =
                        Long.parseLong(
                                parts[1].trim()
                        );

            } catch (NumberFormatException e) {
                continue;
            }

            if (
                    occurrenceCount > 0
                            && returnedSuffix.equals(
                                    normalizedExpectedSuffix
                            )
            ) {
                return true;
            }
        }

        return false;
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
