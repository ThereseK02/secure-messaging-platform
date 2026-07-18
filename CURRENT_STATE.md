# Current Application State

This document summarizes the currently implemented and deployed behavior of the Secure Messaging Platform. It is updated after significant feature milestones and may evolve as security policies and application workflows are refined.

## Authentication
- Users can register and log in with a username and password.
- Authentication uses JWT bearer tokens with a one-hour expiration period.
- Authenticated frontend requests include the JWT in the Authorization header.
- New passwords are stored using BCrypt with strength 12.
- Existing legacy SHA-256 password hashes remain usable temporarily and migrate automatically to BCrypt after a successful login.
- Incorrect legacy passwords do not trigger migration or modify the stored hash.
- Blank passwords are rejected during registration and login.
- New registration passwords must contain at least 15 Unicode characters.
- Registration passwords cannot exceed 72 UTF-8 bytes because the current password encoder is BCrypt.
- Passphrases and spaces are allowed; uppercase letters, numbers, and symbols are not mandatory.
- The registration interface requires password confirmation and uses custom application-styled validation messages.
- New registration passwords are checked against a local common-password blocklist.
- Password screening also rejects predictable username-, email-, and application-name-based values.
- Password comparisons for screening are case-insensitive and ignore outer whitespace.
- Username whitespace is normalized before login and JWT creation.
- Login throttling temporarily blocks a normalized username for 15 minutes after five failed attempts.
- Successful authentication clears previous failed-attempt records.
- Login-attempt state is currently stored in backend application memory.
- Password changes, password resets, token invalidation, and passkeys remain planned.
- Browser compromised-password warnings may appear for weak or commonly breached test-account passwords.

## Direct Messaging
- Authenticated users can send and receive encrypted direct messages.
- Received direct messages can be marked as read.
- Direct-message attachments can be uploaded and downloaded.
- Direct messaging and group messaging remain separate workflows.

## Group Messaging
- Users can create private groups.
- Group conversations display the group name and group ID.
- Group messages update through polling and STOMP/SockJS real-time notifications.
- Users can send text messages and attachments.
- Enter sends a message and Shift+Enter creates a new line.
- Smart autoscroll keeps sent messages visible while avoiding unwanted forced scrolling.
- Message spacing has been refined for consecutive and different senders.
- Compact notification toasts appear in the upper-right area of the interface.

## Group Membership and Roles
- All groups are private and invitation-only.
- Users cannot join groups directly by entering a group ID.
- Membership is created only when an invited user accepts a pending invitation.
- Group roles are Owner, Admin, and Member.
- Group owners can promote Members to Admin.
- Group owners can demote Admins to Members.
- Group owners and administrators can remove regular members.
- Group owners cannot remove themselves through the member-removal control.
- Group owners can permanently delete groups.
- Group deletion removes associated members, messages, read records, attachments, and attachment keys.

## Group Invitations
- Owners and administrators can invite registered users by username.
- Users who are not registered can be invited by email.
- Email invitations use a single-use registration token with an expiration period.
- Invited registered users can accept or decline pending invitations.
- Accepting an invitation creates group membership.
- Declining an invitation does not create membership.
- Duplicate invitations are prevented.
- The legacy direct-join endpoint returns `403 Forbidden`.

## Group Attachments
- Group members can upload attachments associated with group messages.
- Group attachment metadata is stored separately from encryption keys.
- Encrypted attachment keys are stored for authorized group members.
- Authorized group members can download attachments.
- Group deletion removes attachment records and their related encrypted keys.

## Message Search
- Users can search within the selected group conversation.
- Search includes sender names, message text, timestamps, date labels, and attachment filenames.
- Search results update without changing stored messages.
- A Clear control resets the current search.

## Read Status
- Group messages display seen counts.
- Read records are stored per user and group message.
- The group list displays unread message counts.
- Seen status indicates that a message was displayed, not that the user formally acknowledged a decision.

## Edit and Delete
- Users can edit their own text-only group messages.
- Edited messages display an edited label.
- Users can delete their own eligible group messages.
- Messages containing attachments cannot currently be edited.

## Pinning
- Authorized users can pin and unpin group messages.
- Pinned messages display a pinned label.
- Pinning currently identifies important messages but does not yet create a formal decision or acknowledgment workflow.

## Typing Indicators
- Group members can see when another member is typing.
- Typing indicators are scoped to the selected group.
- Typing activity does not leak into other groups.
- The indicator disappears after typing stops, the draft is cleared, the message is sent, or the user leaves the conversation.
- A user does not see their own typing indicator.

## Online and Offline Presence
- Active users send an authenticated presence heartbeat every 10 seconds.
- A user is considered offline after approximately 30 seconds without a heartbeat.
- The selected group refreshes member presence approximately every 3 seconds.
- Online status represents activity anywhere in the Secure Messaging Platform.
- Switching to another group does not make the user offline.
- Online members display a light-blue indicator.
- Offline members display a muted slate indicator.
- Presence is currently stored in backend memory and resets when the backend restarts.

## Real-Time Updates
- Group conversations use a SockJS endpoint with STOMP subscriptions.
- Group typing events are delivered through the group topic.
- Message, attachment, membership, and group events trigger conversation refreshes.
- The frontend reconnects automatically after temporary WebSocket interruptions.
- Production Vite hot-module-reload warnings may appear in browser developer tools but do not affect the deployed application features.

## Deployment
- The application is deployed at `https://brain-secure-messaging.com`.
- The backend runs as a Spring Boot container.
- PostgreSQL runs in the `secure-postgres` container.
- The frontend is built with Node and served from an Nginx production container.
- Nginx provides SPA fallback routing and reverse-proxy access to the backend.
- The latest deployed feature commit is `0f9d144 Add group member online presence`.
- The production health endpoint returns HTTP 200.
- The backend STOMP simple broker starts successfully during application startup.

## Current Phase Status
- Group search is complete.
- Group read and seen status is complete.
- Group unread counts are complete.
- Group message edit and delete are complete for text-only messages.
- Group message pinning is complete.
- Group member role management is complete.
- Group member removal is complete.
- Group deletion is complete.
- Group attachment support is complete for the current workflow.
- Group typing indicators are complete.
- Online and offline member presence is complete.
- Phase 5 conversation and member tools are complete for the current planned scope.

## Planned Next Work
- Privacy-preserving compromised-password screening
- Change Password
- Password reset and token invalidation
- Passkey enrollment and sign-in
- Mark Message as Decision
- Required member acknowledgment
- Immutable decision audit records

## Known Limitations
- Messages with attachments cannot yet be edited.
- Group attachment ownership and deletion policy is still evolving.
- Presence data is not persisted across backend restarts.
- Presence indicates application activity rather than the currently viewed group.
- Login-attempt records are stored in application memory and reset whenever the backend restarts.
- Login throttling is currently keyed only by normalized username and is not yet distributed across multiple backend instances.
- Legacy SHA-256 support remains temporarily available until active accounts have migrated to BCrypt.
- Large-scale compromised-password screening, password changes, password recovery, and passkeys are not yet implemented.
- Pinned messages do not yet support formal decisions, required acknowledgments, or audit records.
