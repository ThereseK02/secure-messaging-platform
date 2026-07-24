package com.securemessaging.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AttentionClassificationServiceTest {

    private static final URI CLASSIFY_ENDPOINT =
            URI.create(
                    "http://127.0.0.1:8090/classify"
            );

    private static final double THRESHOLD =
            0.50;

    private final ObjectMapper objectMapper =
            new ObjectMapper();

    @Test
    void disabledServiceReturnsUnavailableWithoutCallingClient() {

        AtomicBoolean clientCalled =
                new AtomicBoolean(false);

        AttentionClassificationService service =
                createService(
                        false,
                        "test-key",
                        request -> {
                            clientCalled.set(true);

                            return successfulResponse();
                        }
                );

        AttentionClassificationService.ClassificationResult result =
                service.classify(
                        "Please review the deployment."
                );

        assertFalse(result.available());
        assertFalse(result.needsAttention());
        assertEquals(0.0, result.attentionProbability());
        assertEquals(THRESHOLD, result.threshold());
        assertEquals("unavailable", result.modelVersion());
        assertFalse(clientCalled.get());
    }

    @Test
    void blankMessageReturnsUnavailableWithoutCallingClient() {

        AtomicBoolean clientCalled =
                new AtomicBoolean(false);

        AttentionClassificationService service =
                createService(
                        true,
                        "test-key",
                        request -> {
                            clientCalled.set(true);

                            return successfulResponse();
                        }
                );

        AttentionClassificationService.ClassificationResult result =
                service.classify("   ");

        assertFalse(result.available());
        assertFalse(clientCalled.get());
    }

    @Test
    void missingApiKeyReturnsUnavailableWithoutCallingClient() {

        AtomicBoolean clientCalled =
                new AtomicBoolean(false);

        AttentionClassificationService service =
                createService(
                        true,
                        "",
                        request -> {
                            clientCalled.set(true);

                            return successfulResponse();
                        }
                );

        AttentionClassificationService.ClassificationResult result =
                service.classify(
                        "Please review the report."
                );

        assertFalse(result.available());
        assertFalse(result.needsAttention());
        assertFalse(clientCalled.get());
    }

    @Test
    void successfulResponseReturnsClassificationAndBuildsAuthorizedRequest() {

        AtomicReference<HttpRequest> capturedRequest =
                new AtomicReference<>();

        AttentionClassificationService service =
                createService(
                        true,
                        "test-key",
                        request -> {
                            capturedRequest.set(request);

                            return successfulResponse();
                        }
                );

        AttentionClassificationService.ClassificationResult result =
                service.classify(
                        "Please investigate the failed deployment."
                );

        assertTrue(result.available());
        assertTrue(result.needsAttention());
        assertEquals(
                0.791976,
                result.attentionProbability(),
                0.000001
        );
        assertEquals(THRESHOLD, result.threshold());
        assertEquals(
                "attention-combined-tfidf-logreg-v1",
                result.modelVersion()
        );

        HttpRequest request =
                capturedRequest.get();

        assertEquals(
                CLASSIFY_ENDPOINT,
                request.uri()
        );
        assertEquals(
                "POST",
                request.method()
        );
        assertEquals(
                "application/json",
                request.headers()
                        .firstValue("Content-Type")
                        .orElseThrow()
        );
        assertEquals(
                "test-key",
                request.headers()
                        .firstValue("X-Internal-API-Key")
                        .orElseThrow()
        );
        assertTrue(
                request.timeout().isPresent()
        );
    }

    @Test
    void nonSuccessfulHttpResponseReturnsUnavailable() {

        AttentionClassificationService service =
                createService(
                        true,
                        "test-key",
                        request ->
                                new AttentionClassificationService.AttentionResponse(
                                        503,
                                        """
                                        {
                                          "detail": "Unavailable"
                                        }
                                        """
                                )
                );

        AttentionClassificationService.ClassificationResult result =
                service.classify(
                        "Please review the report."
                );

        assertFalse(result.available());
        assertFalse(result.needsAttention());
        assertEquals("unavailable", result.modelVersion());
    }

    @Test
    void responseMissingRequiredFieldsReturnsUnavailable() {

        AttentionClassificationService service =
                createService(
                        true,
                        "test-key",
                        request ->
                                new AttentionClassificationService.AttentionResponse(
                                        200,
                                        """
                                        {
                                          "needsAttention": true
                                        }
                                        """
                                )
                );

        AttentionClassificationService.ClassificationResult result =
                service.classify(
                        "Please review the report."
                );

        assertFalse(result.available());
        assertFalse(result.needsAttention());
    }

    @Test
    void malformedJsonReturnsUnavailable() {

        AttentionClassificationService service =
                createService(
                        true,
                        "test-key",
                        request ->
                                new AttentionClassificationService.AttentionResponse(
                                        200,
                                        "not-json"
                                )
                );

        AttentionClassificationService.ClassificationResult result =
                service.classify(
                        "Please review the report."
                );

        assertFalse(result.available());
        assertFalse(result.needsAttention());
    }

    @Test
    void clientExceptionReturnsUnavailable() {

        AttentionClassificationService service =
                createService(
                        true,
                        "test-key",
                        request -> {
                            throw new IOException(
                                    "Connection refused"
                            );
                        }
                );

        AttentionClassificationService.ClassificationResult result =
                service.classify(
                        "Please review the report."
                );

        assertFalse(result.available());
        assertFalse(result.needsAttention());
        assertEquals(THRESHOLD, result.threshold());
    }

    private AttentionClassificationService createService(
            boolean enabled,
            String apiKey,
            AttentionClassificationService.AttentionClient client) {

        return new AttentionClassificationService(
                enabled,
                CLASSIFY_ENDPOINT,
                apiKey,
                THRESHOLD,
                Duration.ofSeconds(1),
                objectMapper,
                client
        );
    }

    private AttentionClassificationService.AttentionResponse
    successfulResponse() {

        return new AttentionClassificationService.AttentionResponse(
                200,
                """
                {
                  "needsAttention": true,
                  "attentionProbability": 0.791976,
                  "threshold": 0.50,
                  "modelVersion":
                    "attention-combined-tfidf-logreg-v1"
                }
                """
        );
    }
}