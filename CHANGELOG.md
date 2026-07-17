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
- Group typing updates require authenticated group membership
- Group presence queries require authenticated group membership
- Group member removal is restricted by group role
- Group deletion is restricted to the group owner
- Group deletion removes related members, messages, read records, attachments, and attachment keys

### Tested
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
- Authentication hardening, password management, login throttling, and passkeys are not yet implemented
- Decision acknowledgment and audit-record workflows are not yet implemented
