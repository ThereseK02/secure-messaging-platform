package com.securemessaging.service;

import com.securemessaging.entity.UserEntity;
import com.securemessaging.repository.UserEntityRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DatabaseUserServiceTest {


    private UserEntityRepository repository;
    private PasswordEncoder passwordEncoder;
    private DatabaseUserService databaseUserService;

    @BeforeEach
    void setUp() {
        repository = mock(UserEntityRepository.class);

        /*
         * A low BCrypt strength keeps unit tests fast.
         * Production uses strength 12 through SecurityConfig.
         */
        passwordEncoder = new BCryptPasswordEncoder(4);

        databaseUserService =
                new DatabaseUserService(
                        repository,
                        passwordEncoder
                );
    }

    @Test
    void validLegacyPasswordMigratesHashToBcrypt()
            throws Exception {

        String password = "LegacyPassword123!";
        String legacyHash = legacySha256Hash(password);

        UserEntity user =
                createUser("Tom", legacyHash);

        when(repository.findById("Tom"))
                .thenReturn(Optional.of(user));

        boolean valid =
                databaseUserService.validateLogin(
                        "Tom",
                        password
                );

        assertTrue(valid);
        assertTrue(user.getPasswordHash().startsWith("$2"));
        assertTrue(
                passwordEncoder.matches(
                        password,
                        user.getPasswordHash()
                )
        );

        verify(repository).save(same(user));
    }

    @Test
    void invalidLegacyPasswordDoesNotMigrateHash()
            throws Exception {

        String legacyHash =
                legacySha256Hash("CorrectPassword123!");

        UserEntity user =
                createUser("Gombo", legacyHash);

        when(repository.findById("Gombo"))
                .thenReturn(Optional.of(user));

        boolean valid =
                databaseUserService.validateLogin(
                        "Gombo",
                        "WrongPassword123!"
                );

        assertFalse(valid);
        assertTrue(user.getPasswordHash().equals(legacyHash));

        verify(repository, never()).save(same(user));
    }

    @Test
    void existingBcryptPasswordAuthenticatesWithoutRehash()
            throws Exception {

        String password = "BcryptPassword123!";
        String bcryptHash =
                passwordEncoder.encode(password);

        UserEntity user =
                createUser("Kelly", bcryptHash);

        when(repository.findById("Kelly"))
                .thenReturn(Optional.of(user));

        boolean valid =
                databaseUserService.validateLogin(
                        " Kelly ",
                        password
                );

        assertTrue(valid);

        verify(repository, never()).save(same(user));
    }

    @Test
    void blankCredentialsAreRejectedWithoutDatabaseLookup()
            throws Exception {

        assertFalse(
                databaseUserService.validateLogin(
                        "   ",
                        "Password123!"
                )
        );

        assertFalse(
                databaseUserService.validateLogin(
                        "Tom",
                        "   "
                )
        );

        verify(repository, never())
                .findById(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void registrationRejectsPasswordShorterThanFifteenCharacters() {

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> databaseUserService.register(
                                "ShortPasswordUser",
                                "short@example.com",
                                "TooShort123!"
                        )
                );

        assertEquals(
                "Password must be at least 15 characters",
                exception.getMessage()
        );

        verify(repository, never())
                .save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void registrationRejectsPasswordLongerThanSeventyTwoBytes() {

        String password =
                "a".repeat(73);

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> databaseUserService.register(
                                "LongPasswordUser",
                                "long@example.com",
                                password
                        )
                );

        assertEquals(
                "Password must not exceed 72 UTF-8 bytes",
                exception.getMessage()
        );

        verify(repository, never())
                .save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void registrationAcceptsValidPasswordAndStoresBcryptHash()
            throws Exception {

        String password =
                "correct horse battery staple";

        when(repository.existsById("ValidPasswordUser"))
                .thenReturn(false);

        when(repository.findByEmailIgnoreCase(
                "valid@example.com"
        )).thenReturn(Optional.empty());

        databaseUserService.register(
                "ValidPasswordUser",
                "valid@example.com",
                password
        );

        ArgumentCaptor<UserEntity> entityCaptor =
                ArgumentCaptor.forClass(UserEntity.class);

        verify(repository).save(entityCaptor.capture());

        UserEntity savedUser =
                entityCaptor.getValue();

        assertTrue(
                savedUser.getPasswordHash().startsWith("$2")
        );

        assertTrue(
                passwordEncoder.matches(
                        password,
                        savedUser.getPasswordHash()
                )
        );
    }

    private UserEntity createUser(
            String username,
            String passwordHash) {

        return new UserEntity(
                username,
                username.toLowerCase() + "@example.com",
                passwordHash,
                "public-key",
                "private-key",
                LocalDateTime.now()
        );
    }

    private String legacySha256Hash(String password)
            throws Exception {

        MessageDigest digest =
                MessageDigest.getInstance("SHA-256");

        byte[] hash =
                digest.digest(
                        password.getBytes(StandardCharsets.UTF_8)
                );

        return Base64.getEncoder().encodeToString(hash);
    }
}
