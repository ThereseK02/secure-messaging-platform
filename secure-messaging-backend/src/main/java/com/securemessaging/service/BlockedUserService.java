package com.securemessaging.service;

import com.securemessaging.entity.BlockedUser;
import com.securemessaging.entity.BlockedUserId;
import com.securemessaging.entity.UserEntity;
import com.securemessaging.repository.BlockedUserRepository;
import com.securemessaging.repository.UserEntityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional
public class BlockedUserService {

    private final BlockedUserRepository blockedUserRepository;
    private final UserEntityRepository userEntityRepository;

    public BlockedUserService(
            BlockedUserRepository blockedUserRepository,
            UserEntityRepository userEntityRepository) {

        this.blockedUserRepository = blockedUserRepository;
        this.userEntityRepository = userEntityRepository;
    }

    public void blockUser(
            String blockerUsername,
            String blockedUsername) {

        UserEntity blockerUser = userEntityRepository
                .findByUsername(blockerUsername)
                .orElseThrow(() ->
                        new NoSuchElementException("User not found."));

        UserEntity blockedUser = userEntityRepository
                .findByUsername(blockedUsername)
                .orElseThrow(() ->
                        new NoSuchElementException("User not found."));

        if (blockerUser.getUsername().equalsIgnoreCase(
                blockedUser.getUsername())) {

            throw new IllegalArgumentException(
                    "You cannot block yourself.");
        }

        BlockedUserId id = new BlockedUserId(
                blockerUser.getUsername(),
                blockedUser.getUsername());

        if (blockedUserRepository.existsById(id)) {
            return;
        }

        blockedUserRepository.save(new BlockedUser(id));
    }

    public void unblockUser(
            String blockerUsername,
            String blockedUsername) {

        UserEntity blockerUser = userEntityRepository
                .findByUsername(blockerUsername)
                .orElseThrow(() ->
                        new NoSuchElementException("User not found."));

        UserEntity blockedUser = userEntityRepository
                .findByUsername(blockedUsername)
                .orElseThrow(() ->
                        new NoSuchElementException("User not found."));

        BlockedUserId id = new BlockedUserId(
                blockerUser.getUsername(),
                blockedUser.getUsername());

        if (blockedUserRepository.existsById(id)) {
            blockedUserRepository.deleteById(id);
        }
    }

    @Transactional(readOnly = true)
    public boolean isBlocked(
            String blockerUsername,
            String blockedUsername) {

        BlockedUserId id = new BlockedUserId(
                blockerUsername,
                blockedUsername);

        return blockedUserRepository.existsById(id);
    }

    @Transactional(readOnly = true)
    public List<String> getBlockedUsers(
            String blockerUsername) {

        return blockedUserRepository
                .findByIdBlockerUsername(blockerUsername)
                .stream()
                .map(blockedUser ->
                        blockedUser
                                .getId()
                                .getBlockedUsername())
                .toList();
    }
}