# Changelog

## Unreleased

### Added
- Registered-user group invitations by username
- Email invitations for users who are not yet registered
- Pending invitation Accept and Decline controls
- Group Owner and Admin invitation permissions
- Scrollable member list for growing groups
- Group member role controls
- Owner promotion and demotion controls
- Owner and Admin member removal controls
- Group deletion by the group owner
- Group message pinning and pinned-message labels
- Group message search
- Group read status and seen counts
- Unread message counts in the group list
- Edit and delete controls for text-only group messages
- Group attachment upload and download
- Group typing indicators
- Online and offline indicators for group members
- Application-wide presence heartbeat and automatic offline timeout
- Real-time group updates through STOMP and SockJS

### Changed
- Improved group member management layout
- Updated role labels for Owner, Admin, and Member
- Improved group attachment presentation
- Improved group notification toast layout and responsiveness
- Reduced group message vertical spacing
- Improved spacing between messages from different senders
- Improved group conversation autoscroll behavior
- Optimized the production frontend Docker image using a Node build stage and Nginx runtime
- Made all groups private and invitation-only
- Removed direct group joining by group ID from the user interface
- Blocked the legacy direct-join endpoint from creating memberships
- Scoped typing indicators to the currently selected group
- Defined online status as activity anywhere in the Secure Messaging Platform

### Security
- New registration passwords are checked against a local common-password blocklist
- Password screening also blocks predictable values based on the username, email local part, and application name
- Common-password comparisons are case-insensitive and ignore outer whitespace
- Rejected passwords return a generic message without revealing the matched blocklist entry
- New registration passwords must contain at least 15 Unicode characters
- Registration passwords are limited to 72 UTF-8 bytes to remain within BCrypt's supported input size
- Passwords may contain spaces and are not forced into predictable composition rules
- Registration includes a password-confirmation field
- Registration validation uses application-styled messages instead of browser-native validation popups
- Login attempts are tracked per normalized username in application memory
- Five failed login attempts trigger a temporary 15-minute block
- Successful authentication clears previous failed-attempt records
- Blocked and invalid login attempts return the same generic browser message
- New registrations store passwords using BCrypt with strength 12
- Existing SHA-256 password hashes migrate automatically to BCrypt after successful login
- Legacy password migration uses constant-time hash comparison
- Blank passwords are rejected during registration and login
- Group typing updates require authenticated group membership
- Group presence queries require authenticated group membership
- Group member removal is restricted by group role
- Group deletion is restricted to the group owner
- Group deletion removes related members, messages, read records, attachments, and attachment keys

### Tested
- Added five automated tests for local and context-specific password screening
- Added a registration integration test for common-password rejection
- Verified Password1234567 is rejected in production
- Verified a non-listed passphrase registers successfully
- Verified all 22 backend tests pass
- Added automated tests for short, oversized, and valid registration passwords
- Verified a password shorter than 15 characters is rejected
- Verified mismatched password confirmation is rejected in the browser
- Verified a valid passphrase registers successfully and authenticates normally
- Verified the new Gana account stores a 60-character BCrypt hash with the $2a$ prefix
- Verified registration errors use the application's custom notification styling
- Added automated tests for login throttling, successful-attempt reset, username normalization, and blank usernames
- Verified four failed attempts still allow a correct login
- Verified the fifth failed attempt activates temporary blocking
- Verified a correct password remains rejected while the temporary block is active
- Verified restarting the backend clears the current in-memory login-attempt state
- Added automated tests for BCrypt authentication and legacy password migration
- Verified successful production migration of Tom from a 44-character SHA-256 hash to a 60-character BCrypt hash
- Verified subsequent login using the migrated BCrypt hash
- Verified group typing indicators between multiple users
- Verified typing indicators do not leak between groups
- Verified online users become offline after heartbeat expiration
- Verified users return online after reopening the application
- Verified non-admin users cannot remove members
- Verified group owners cannot remove themselves
- Verified non-owners cannot delete groups
- Verified complete deletion of a populated group and its related database records
- Verified the production deployment and health endpoint after the latest changes

### Known Limitations
- Messages with attachments cannot yet be edited
- Group attachment ownership and deletion policy is still evolving
- Online presence is stored in application memory and resets when the backend restarts
- Online status shows activity anywhere in the application, not the group currently being viewed
- Large-scale compromised-password screening, password management, distributed login throttling, and passkeys are not yet implemented
- Decision acknowledgment and audit-record workflows are not yet implemented
