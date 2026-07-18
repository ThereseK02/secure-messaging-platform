package com.securemessaging.service;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

@Service
public class CommonPasswordService {

    private static final String BLOCKLIST_RESOURCE =
            "common-passwords.txt";

    private final Set<String> blockedPasswords;

    public CommonPasswordService() {
        this.blockedPasswords =
                loadBlockedPasswords();
    }

    public boolean isBlocked(
            String password,
            String username,
            String email) {

        if (password == null) {
            return false;
        }

        String normalizedPassword =
                normalize(password);

        if (blockedPasswords.contains(normalizedPassword)) {
            return true;
        }

        Set<String> contextSpecificPasswords =
                buildContextSpecificPasswords(
                        username,
                        email
                );

        return contextSpecificPasswords.contains(
                normalizedPassword
        );
    }

    private Set<String> loadBlockedPasswords() {

        Set<String> passwords =
                new HashSet<>();

        ClassPathResource resource =
                new ClassPathResource(
                        BLOCKLIST_RESOURCE
                );

        try (
                BufferedReader reader =
                        new BufferedReader(
                                new InputStreamReader(
                                        resource.getInputStream(),
                                        StandardCharsets.UTF_8
                                )
                        )
        ) {
            reader.lines()
                    .map(String::trim)
                    .filter(line -> !line.isBlank())
                    .filter(line -> !line.startsWith("#"))
                    .map(this::normalize)
                    .forEach(passwords::add);

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Unable to load common-password blocklist",
                    e
            );
        }

        return Set.copyOf(passwords);
    }

    private Set<String> buildContextSpecificPasswords(
            String username,
            String email) {

        Set<String> candidates =
                new HashSet<>();

        addContextCandidates(
                candidates,
                username
        );

        addContextCandidates(
                candidates,
                extractEmailLocalPart(email)
        );

        addContextCandidates(
                candidates,
                "securemessaging"
        );

        addContextCandidates(
                candidates,
                "brainsecuremessaging"
        );

        return candidates;
    }

    private void addContextCandidates(
            Set<String> candidates,
            String value) {

        String normalizedValue =
                normalize(value);

        if (normalizedValue.isBlank()) {
            return;
        }

        candidates.add(normalizedValue);
        candidates.add(normalizedValue + "123");
        candidates.add(normalizedValue + "123!");
        candidates.add(normalizedValue + "2026");
        candidates.add(normalizedValue + "2026!");
    }

    private String extractEmailLocalPart(String email) {

        if (email == null) {
            return "";
        }

        int atIndex =
                email.indexOf('@');

        if (atIndex <= 0) {
            return "";
        }

        return email.substring(
                0,
                atIndex
        );
    }

    private String normalize(String value) {

        return value == null
                ? ""
                : value.trim().toLowerCase(Locale.ROOT);
    }
}
