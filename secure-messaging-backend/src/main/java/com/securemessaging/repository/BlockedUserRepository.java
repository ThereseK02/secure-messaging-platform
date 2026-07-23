package com.securemessaging.repository;

import com.securemessaging.entity.BlockedUser;
import com.securemessaging.entity.BlockedUserId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BlockedUserRepository
        extends JpaRepository<BlockedUser, BlockedUserId> {

    boolean existsById(BlockedUserId id);

    List<BlockedUser> findByIdBlockerUsername(String blockerUsername);

    List<BlockedUser> findByIdBlockedUsername(String blockedUsername);

    void deleteById(BlockedUserId id);
}