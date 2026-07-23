CREATE TABLE blocked_users (
    blocker_username VARCHAR(255) NOT NULL,
    blocked_username VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_blocked_users
        PRIMARY KEY (blocker_username, blocked_username),

    CONSTRAINT fk_blocked_users_blocker
        FOREIGN KEY (blocker_username)
        REFERENCES users(username)
        ON DELETE CASCADE,

    CONSTRAINT fk_blocked_users_blocked
        FOREIGN KEY (blocked_username)
        REFERENCES users(username)
        ON DELETE CASCADE
);
