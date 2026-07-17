package com.securemessaging.service;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class UserPresenceService {

    private static final Duration ONLINE_TIMEOUT =
            Duration.ofSeconds(30);

    private final ConcurrentHashMap<String, LocalDateTime>
            lastSeenByUsername = new ConcurrentHashMap<>();

    public void recordHeartbeat(String username) {
        if (username == null || username.isBlank()) {
            return;
        }

        lastSeenByUsername.put(
                username,
                LocalDateTime.now()
        );
    }

    public boolean isOnline(String username) {
        if (username == null || username.isBlank()) {
            return false;
        }

        LocalDateTime lastSeen =
                lastSeenByUsername.get(username);

        if (lastSeen == null) {
            return false;
        }

        boolean online =
                Duration.between(
                        lastSeen,
                        LocalDateTime.now()
                ).compareTo(ONLINE_TIMEOUT) <= 0;

        if (!online) {
            lastSeenByUsername.remove(username, lastSeen);
        }

        return online;
    }

    public Set<String> getOnlineUsernames() {
        return lastSeenByUsername
                .keySet()
                .stream()
                .filter(this::isOnline)
                .collect(Collectors.toSet());
    }

    public void markOffline(String username) {
        if (username == null || username.isBlank()) {
            return;
        }

        lastSeenByUsername.remove(username);
    }
}
