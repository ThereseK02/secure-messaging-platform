package com.securemessaging.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

@Service
public class AttentionClassificationService {

    public record ClassificationResult(
            boolean available,
            boolean needsAttention,
            double attentionProbability,
            double threshold,
            String modelVersion) {

        public static ClassificationResult unavailable(
                double threshold) {

            return new ClassificationResult(
                    false,
                    false,
                    0.0,
                    threshold,
                    "unavailable"
            );
        }
    }

    @FunctionalInterface
    interface AttentionClient {

        AttentionResponse send(HttpRequest request)
                throws Exception;
    }

    record AttentionResponse(
            int statusCode,
            String body) {
    }

    private static final Logger logger =
            LoggerFactory.getLogger(
                    AttentionClassificationService.class
            );

    private static final Duration CONNECT_TIMEOUT =
            Duration.ofSeconds(2);

    private static final Duration REQUEST_TIMEOUT =
            Duration.ofSeconds(3);

    private final boolean enabled;
    private final URI classifyEndpoint;
    private final String internalApiKey;
    private final double defaultThreshold;
    private final Duration requestTimeout;
    private final ObjectMapper objectMapper;
    private final AttentionClient attentionClient;

    @Autowired
    public AttentionClassificationService(
            @Value("${app.ai.attention.enabled:false}")
            boolean enabled,
            @Value("${app.ai.attention.url:http://127.0.0.1:8090}")
            String serviceUrl,
            @Value("${app.ai.attention.api-key:}")
            String internalApiKey,
            @Value("${app.ai.attention.threshold:0.50}")
            double defaultThreshold,
            ObjectMapper objectMapper) {

        HttpClient httpClient =
                HttpClient.newBuilder()
                        .connectTimeout(CONNECT_TIMEOUT)
                        .build();

        this.enabled = enabled;
        this.classifyEndpoint =
                URI.create(
                        normalizeServiceUrl(serviceUrl)
                                + "/classify"
                );
        this.internalApiKey =
                internalApiKey == null
                        ? ""
                        : internalApiKey.trim();
        this.defaultThreshold =
                defaultThreshold;
        this.requestTimeout =
                REQUEST_TIMEOUT;
        this.objectMapper =
                objectMapper;
        this.attentionClient =
                request -> {

                    HttpResponse<String> response =
                            httpClient.send(
                                    request,
                                    HttpResponse.BodyHandlers.ofString(
                                            StandardCharsets.UTF_8
                                    )
                            );

                    return new AttentionResponse(
                            response.statusCode(),
                            response.body()
                    );
                };
    }

    AttentionClassificationService(
            boolean enabled,
            URI classifyEndpoint,
            String internalApiKey,
            double defaultThreshold,
            Duration requestTimeout,
            ObjectMapper objectMapper,
            AttentionClient attentionClient) {

        this.enabled = enabled;
        this.classifyEndpoint = classifyEndpoint;
        this.internalApiKey = internalApiKey;
        this.defaultThreshold = defaultThreshold;
        this.requestTimeout = requestTimeout;
        this.objectMapper = objectMapper;
        this.attentionClient = attentionClient;
    }

    public ClassificationResult classify(
            String messageText) {

        if (!enabled) {
            return ClassificationResult.unavailable(
                    defaultThreshold
            );
        }

        if (messageText == null
                || messageText.isBlank()) {

            return ClassificationResult.unavailable(
                    defaultThreshold
            );
        }

        if (internalApiKey == null
                || internalApiKey.isBlank()) {

            logger.warn(
                    "Attention classification is enabled "
                            + "but the internal API key is missing"
            );

            return ClassificationResult.unavailable(
                    defaultThreshold
            );
        }

        try {
            String requestBody =
                    objectMapper.writeValueAsString(
                            Map.of(
                                    "message",
                                    messageText,
                                    "threshold",
                                    defaultThreshold
                            )
                    );

            HttpRequest request =
                    HttpRequest.newBuilder(
                                    classifyEndpoint
                            )
                            .timeout(requestTimeout)
                            .header(
                                    "Content-Type",
                                    "application/json"
                            )
                            .header(
                                    "X-Internal-API-Key",
                                    internalApiKey
                            )
                            .POST(
                                    HttpRequest.BodyPublishers.ofString(
                                            requestBody,
                                            StandardCharsets.UTF_8
                                    )
                            )
                            .build();

            AttentionResponse response =
                    attentionClient.send(request);

            if (response.statusCode() != 200) {
                logger.warn(
                        "Attention service returned HTTP {} from {} with body: {}",
                        response.statusCode(),
                        classifyEndpoint,
                        response.body()
                );

                return ClassificationResult.unavailable(
                        defaultThreshold
                );
            }

            JsonNode responseJson =
                    objectMapper.readTree(
                            response.body()
                    );

            if (!responseJson.has("needsAttention")
                    || !responseJson.has(
                            "attentionProbability"
                    )
                    || !responseJson.has("threshold")
                    || !responseJson.has("modelVersion")) {

                logger.warn(
                        "Attention service response "
                                + "is missing required fields"
                );

                return ClassificationResult.unavailable(
                        defaultThreshold
                );
            }

            return new ClassificationResult(
                    true,
                    responseJson
                            .get("needsAttention")
                            .asBoolean(),
                    responseJson
                            .get("attentionProbability")
                            .asDouble(),
                    responseJson
                            .get("threshold")
                            .asDouble(),
                    responseJson
                            .get("modelVersion")
                            .asText()
            );

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            logger.warn(
                    "Attention classification was interrupted"
            );

            return ClassificationResult.unavailable(
                    defaultThreshold
            );

        } catch (Exception e) {
            logger.warn(
                    "Attention classification failed for {}: {}: {}",
                    classifyEndpoint,
                    e.getClass().getSimpleName(),
                    e.getMessage(),
                    e
            );

            return ClassificationResult.unavailable(
                    defaultThreshold
            );
        }
    }

    private static String normalizeServiceUrl(
            String serviceUrl) {

        String normalized =
                serviceUrl == null
                        ? ""
                        : serviceUrl.trim();

        while (normalized.endsWith("/")) {
            normalized =
                    normalized.substring(
                            0,
                            normalized.length() - 1
                    );
        }

        if (normalized.isBlank()) {
            return "http://127.0.0.1:8090";
        }

        return normalized;
    }
}
