package com.securemessaging.service;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginAttemptService {

    private static final int MAX_FAILED_ATTEMPTS = 5;

    private static final Duration BLOCK_DURATION =
            Duration.ofMinutes(15);

    private final ConcurrentHashMap<String, AttemptRecord> attempts =
            new ConcurrentHashMap<>();

    public boolean isBlocked(String username) {

        String key = normalizeUsername(username);

        if (key.isBlank()) {
            return false;
        }

        AttemptRecord record =
                attempts.get(key);

        if (record == null) {
            return false;
        }

        if (record.blockedUntil() == null) {
            return false;
        }

        if (Instant.now().isBefore(record.blockedUntil())) {
            return true;
        }

        attempts.remove(key, record);

        return false;
    }

    public void recordFailure(String username) {

        String key = normalizeUsername(username);

        if (key.isBlank()) {
            return;
        }

        attempts.compute(
                key,
                (ignored, existingRecord) -> {

                    Instant now = Instant.now();

                    if (
                            existingRecord == null ||
                            blockExpired(existingRecord, now)
                    ) {
                        return new AttemptRecord(
                                1,
                                null
                        );
                    }

                    int updatedFailures =
                            existingRecord.failedAttempts() + 1;

                    Instant blockedUntil =
                            updatedFailures >= MAX_FAILED_ATTEMPTS
                                    ? now.plus(BLOCK_DURATION)
                                    : null;

                    return new AttemptRecord(
                            updatedFailures,
                            blockedUntil
                    );
                }
        );
    }

    public void recordSuccess(String username) {

        String key = normalizeUsername(username);

        if (!key.isBlank()) {
            attempts.remove(key);
        }
    }

    int getFailedAttempts(String username) {

        AttemptRecord record =
                attempts.get(normalizeUsername(username));

        return record == null
                ? 0
                : record.failedAttempts();
    }

    private boolean blockExpired(
            AttemptRecord record,
            Instant now) {

        return record.blockedUntil() != null &&
                !now.isBefore(record.blockedUntil());
    }

    private String normalizeUsername(String username) {

        return username == null
                ? ""
                : username.trim().toLowerCase(Locale.ROOT);
    }

    private record AttemptRecord(
            int failedAttempts,
            Instant blockedUntil) {
    }
}
