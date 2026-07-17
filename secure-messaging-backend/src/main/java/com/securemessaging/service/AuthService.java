package com.securemessaging.service;

import com.securemessaging.entity.EmailGroupInvitationEntity;
import com.securemessaging.entity.EmailGroupInvitationStatus;

import com.securemessaging.entity.GroupInvitationEntity;
import com.securemessaging.entity.GroupInvitationStatus;
import com.securemessaging.repository.EmailGroupInvitationRepository;
import com.securemessaging.repository.GroupEntityRepository;
import com.securemessaging.repository.GroupInvitationRepository;
import com.securemessaging.security.JwtUtil;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Locale;

@Service
public class AuthService {

    private final DatabaseUserService databaseUserService;
    private final JwtUtil jwtUtil;
    private final InvitationTokenService invitationTokenService;
    private final EmailGroupInvitationRepository emailGroupInvitationRepository;
    private final GroupInvitationRepository groupInvitationRepository;
    private final GroupEntityRepository groupRepository;

    public AuthService(
            DatabaseUserService databaseUserService,
            JwtUtil jwtUtil,
            InvitationTokenService invitationTokenService,
            EmailGroupInvitationRepository emailGroupInvitationRepository,
            GroupInvitationRepository groupInvitationRepository,
            GroupEntityRepository groupRepository) {

        this.databaseUserService = databaseUserService;
        this.jwtUtil = jwtUtil;
        this.invitationTokenService = invitationTokenService;
        this.emailGroupInvitationRepository =
                emailGroupInvitationRepository;
        this.groupInvitationRepository = groupInvitationRepository;
        this.groupRepository = groupRepository;
    }

    @Transactional
    public void register(
            String username,
            String email,
            String password,
            String invitationToken) {

        String normalizedUsername =
                username == null ? "" : username.trim();

        String normalizedEmail =
                email == null
                        ? ""
                        : email.trim().toLowerCase(Locale.ROOT);

        EmailGroupInvitationEntity emailInvitation = null;

        try {
            if (
                    invitationToken != null &&
                            !invitationToken.isBlank()
            ) {
                String tokenHash =
                        invitationTokenService.hashToken(
                                invitationToken
                        );

                emailInvitation =
                        emailGroupInvitationRepository
                                .findByTokenHash(tokenHash)
                                .orElseThrow(
                                        () -> new RuntimeException(
                                                "Invalid invitation token"
                                        )
                                );

                if (
                        emailInvitation.getStatus() !=
                                EmailGroupInvitationStatus.PENDING
                ) {
                    throw new RuntimeException(
                            "This invitation is no longer available"
                    );
                }

                LocalDateTime now = LocalDateTime.now();

                if (
                        !emailInvitation
                                .getExpiresAt()
                                .isAfter(now)
                ) {
                    throw new RuntimeException(
                            "This invitation has expired"
                    );
                }

                if (
                        !emailInvitation
                                .getInvitedEmail()
                                .equalsIgnoreCase(normalizedEmail)
                ) {
                    throw new RuntimeException(
                            "Registration email does not match the invited email"
                    );
                }

                groupRepository
                        .findById(
                                emailInvitation.getGroupId()
                        )
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "The invited group no longer exists"
                                )
                        );
            }

            databaseUserService.register(
                    normalizedUsername,
                    normalizedEmail,
                    password
            );

            if (emailInvitation != null) {
                GroupInvitationEntity existingInvitation =
                        groupInvitationRepository
                                .findByGroupIdAndInvitedUsername(
                                        emailInvitation.getGroupId(),
                                        normalizedUsername
                                )
                                .orElse(null);

                LocalDateTime now = LocalDateTime.now();

                GroupInvitationEntity groupInvitation;

                if (existingInvitation == null) {
                    groupInvitation =
                            new GroupInvitationEntity(
                                    emailInvitation.getGroupId(),
                                    normalizedUsername,
                                    emailInvitation.getInvitedBy(),
                                    GroupInvitationStatus.PENDING,
                                    now
                            );
                } else {
                    groupInvitation = existingInvitation;
                    groupInvitation.setInvitedBy(
                            emailInvitation.getInvitedBy()
                    );
                    groupInvitation.setStatus(
                            GroupInvitationStatus.PENDING
                    );
                    groupInvitation.setCreatedAt(now);
                    groupInvitation.setRespondedAt(null);
                }

                groupInvitationRepository.save(groupInvitation);

                emailInvitation.setStatus(
                        EmailGroupInvitationStatus.USED
                );
                emailInvitation.setUsedAt(now);
                emailInvitation.setRegisteredUsername(
                        normalizedUsername
                );

                emailGroupInvitationRepository.save(
                        emailInvitation
                );
            }

        } catch (RuntimeException e) {
            throw e;

        } catch (Exception e) {
            throw new RuntimeException(
                    "Registration failed"
            );
        }
    }

    public String login(String username, String password) {

        String normalizedUsername =
                username == null ? "" : username.trim();

        try {
            boolean valid =
                    databaseUserService.validateLogin(
                            normalizedUsername,
                            password
                    );

            if (!valid) {
                throw new RuntimeException(
                        "Invalid username or password"
                );
            }

            return jwtUtil.generateToken(normalizedUsername);

        } catch (Exception e) {
            throw new RuntimeException(
                    "Login failed: " + e.getMessage()
            );
        }
    }
    }
