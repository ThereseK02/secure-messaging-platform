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
- Registration passwords are checked against the Have I Been Pwned Pwned Passwords range service.
- Only the first five characters of a locally generated SHA-1 hash are transmitted for compromised-password checking.
- The plaintext password and complete hash remain within the backend.
- Padded range responses are requested to reduce response-size information leakage.
- If the external compromised-password service is unavailable, registration continues while the local blocklist remains active.
- Username whitespace is normalized before login and JWT creation.
- Login throttling temporarily blocks a normalized username for 15 minutes after five failed attempts.
- Successful authentication clears previous failed-attempt records.
- Login-attempt state is currently stored in backend application memory.
- Authenticated users can change their password from the dashboard.
- Password changes require the correct current password.
- New passwords must satisfy the existing minimum-length, BCrypt byte-limit, local-screening, and compromised-password requirements.
- A successful password change clears the frontend authentication state and redirects the user to Login.
- Invalid login credentials return a generic authentication error without being misreported as an expired session.
- Password reset, token revocation, Multi-Factor Authentication, recovery codes, and passkeys remain planned.
- Browser compromised-password warnings may appear for weak or commonly breached test-account passwords.

## Direct Messaging
- Authenticated users can send and receive encrypted direct messages without sharing a group.
- Unread messages display an `UNREAD` badge and an explicit `Mark as read` control.
- Selecting an unread message card also marks the message as read.
- Marking a message as read updates the unread count and persists after refresh.
- Direct-message attachments can be uploaded and downloaded.
- Authenticated users can check whether they have blocked a registered recipient.
- Users can block or unblock another registered user from the direct-message composition page.
- Block relationships are persisted in PostgreSQL and remain effective after refresh, logout and login, and application restart.
- Direct-message blocking is enforced by the backend in both directions when either participant has blocked the other.
- Blocked direct-message attempts return the neutral response `Direct messaging is unavailable between these users`.
- The frontend prevents sending to a user the current user has blocked and displays `Unblock this user before sending a direct message.`
- Changing the recipient clears the previous recipient's frontend block status before the new status is retrieved.
- Blocking affects only direct messaging and does not change shared private-group membership or governance permissions.
- Downloading an attachment does not automatically mark its parent message as read.
- The Inbox displays `Needs attention: 0` as an AI-ready placeholder; no active AI classifier currently calculates this value.
- Direct messaging and private-group membership remain separate workflows.

## Platform Access Model
- Opening the public website does not require an invitation.
- Normal registration and login do not require an invitation.
- Authenticated users can create their own private groups and automatically become the group owner.
- Invitations apply only when a user needs to join another user's private group.
- An uninvited user cannot see or access another user's private group.
- Group membership is created only after the invited user explicitly accepts the invitation.

## Group Messaging
- Users can create private groups.
- Group conversations display the group name and group ID.
- Group messages update through polling and STOMP/SockJS real-time notifications.
- Users can send text messages and attachments.
- Enter sends a message and Shift+Enter creates a new line.
- Smart autoscroll keeps sent messages visible while avoiding unwanted forced scrolling.
- Message spacing has been refined for consecutive and different senders.
- Compact notification toasts appear in the upper-right area of the interface.
- Success notifications use navy backgrounds, light-blue borders, and pale-blue text.
- Error, validation, authentication, and denied-action notifications use dark-slate backgrounds with beige borders and text instead of red.
- The notification palette is deployed, browser-tested, and accepted across direct messaging, group chat, registration, and login.

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
- Users can pin and unpin their own eligible group messages through the message-actions menu.
- Pinned messages display a pinned label and identify the user who pinned them.
- Pinning identifies important messages but does not yet create a formal decision, required acknowledgment, or audit record.

## Group Decision Governance
- Eligible text group messages can be converted into persisted decision records.
- Decision records preserve the source group, source message, source sender, decision-text snapshot, creator, governance mode, status, and creation time.
- Owner Review, Member Vote, and Owner Led governance workflows are implemented.
- Owner Review decisions begin with the `PROPOSED` status.
- Only the group owner can approve or reject an unresolved Owner Review decision.
- Owner Review Approve and Reject controls remain hidden behind a `Decision actions` control until needed.
- Member Vote decisions begin with the `PROPOSED` status.
- Only the group owner can set the voting deadline and open voting.
- Eligible members can cast secret Approve, Reject, or Abstain ballots.
- Members may replace their ballot while voting remains open; only the latest ballot is counted.
- Individual secret-ballot choices are not displayed in the ordinary group interface.
- Voting cannot be resolved before the configured deadline.
- Deterministic resolution applies quorum and aggregate vote-total rules.
- Tied voting enters the `WAITING_FOR_TIE_BREAK` status.
- The group owner can cast a public Approve or Reject tie-break decision.
- Owner Led decisions support owner-controlled final resolution.
- Finalized decisions can require and record explicit member acknowledgment.
- Acknowledgment records are persisted and exposed through the decision workflow.
- Governance activity is recorded through append-only decision events.
- Persisted event types include creation, discussion opening, voting opening, ballot submission or replacement, quorum failure, tie-break requirement, final resolution, withdrawal, and acknowledgment.
- Secret-ballot audit events do not reveal individual ballot choices.
- Resolution events may preserve aggregate vote totals without exposing individual ballots.
- Decision creation, resolution, and acknowledgment updates are broadcast through the group WebSocket topic.
- Connected group members receive governance updates without requiring a browser refresh.
- A dedicated authorized governance-history interface remains future administrative work.

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
- The corrected EC2 deployment workflow was successfully verified beginning with commit `c09fbc0 Fix EC2 deployment workflow`.
- The production health endpoint returns HTTP 200.
- The backend STOMP simple broker starts successfully during application startup.
- GitHub Actions deploys pushes to `main` through an SSH connection to EC2.
- The deployment workflow fetches `origin/main`, checks out `main`, and resets the EC2 repository to the latest remote commit.
- Docker images are rebuilt without deleting the PostgreSQL volume.
- Application containers are force-recreated and verified after deployment.
- The workflow stops on the first failed remote command.
- Port 22 is opened temporarily for GitHub-hosted runner access and returned to `My IP` after deployment verification.

## Current Phase Status
- Group search, read and seen status, unread counts, message actions, member controls, attachments, typing indicators, and presence are complete for the current scope.
- Password policy enforcement, compromised-password screening, authenticated Change Password, and separated invalid-login and expired-session handling are implemented.
- Direct messages support explicit `Mark as read` controls and persistent unread-state updates.
- Direct-message user blocking is implemented, persisted, deployed, browser-tested, and accepted.
- README visual documentation includes the deployed direct-message blocking interface.
- README visual documentation includes the three-mode group governance selector.
- Block and unblock controls retrieve the persisted backend state for the entered recipient.
- Direct-message blocking is enforced bidirectionally without revealing which participant created the block.
- Direct-message attachment downloads remain independent from read status.
- Open registration, login, direct messaging, and private-group creation do not require invitations.
- Joining another user's private group requires an invitation and explicit acceptance.
- Owner Review governance is implemented, deployed, browser-tested, and accepted.
- Member Vote governance is implemented, deployed, browser-tested, and accepted.
- Owner Led governance is implemented, deployed, browser-tested, and accepted.
- Secret-ballot recording, ballot replacement, voting deadlines, quorum resolution, tie detection, and owner tie-break resolution are implemented.
- Explicit decision acknowledgments are implemented.
- Append-only governance audit events are persisted.
- Secret-ballot audit events preserve ballot secrecy.
- Governance updates are delivered through the existing group WebSocket workflow.
- The corrected GitHub Actions EC2 deployment workflow is implemented and tested.
- The Inbox includes an AI-ready `Needs attention` interface location, but AI classification is not yet active.

## AI Readiness and Roadmap
- AI-assisted attention detection may populate the existing `Needs attention` count.
- AI summaries may cover direct messages, group discussions, and governance activity.
- AI search may use semantic retrieval, embeddings, vector search, and Retrieval-Augmented Generation.
- An AI assistant may support drafting, conversation understanding, meeting notes, unresolved-question detection, and suggested next actions.
- Smart classification may identify topics, urgency, tasks, deadlines, decisions, and follow-up requests.
- User-directory and contact search may be improved through authorized username, name, organization, role, and contact discovery.
- AI responses must be restricted to information the requesting user is authorized to access.
- AI-generated results should identify supporting messages and remain reviewable by users.
- AI features must not reveal secret ballots or perform owner-, administrator-, or governance-restricted actions.
- Initial AI processing may use authenticated REST requests or controlled polling.
- An optional AI-specific WebSocket or server-push channel may be added if polling is insufficient for streaming responses, long-running summaries, or live classification updates.
- Any AI-specific transport should complement rather than replace the existing STOMP/SockJS group update system.

## Planned Next Work
- Begin the AI subsystem with attention detection and a real `Needs attention` calculation
- Implement secure AI summaries for authorized conversations
- Design AI search and Retrieval-Augmented Generation with permission-aware retrieval
- Add a controlled AI assistant workflow
- Add smart classification and action-item extraction
- Improve the user directory and contact-search workflow
- Add authorized governance-history views and administrative audit tools
- Consider aggregate governance analytics without exposing individual secret-ballot choices
- Support attachment-based governance decisions
- Implement password reset and secure reset-token invalidation
- Add Multi-Factor Authentication, recovery codes, and passkey support

## Known Limitations
- Messages with attachments cannot yet be edited.
- Group attachment ownership and deletion policy is still evolving.
- Presence data is not persisted across backend restarts.
- Presence indicates application activity rather than the currently viewed group.
- Login-attempt records are stored in application memory and reset whenever the backend restarts.
- Login throttling is currently keyed only by normalized username and is not yet distributed across multiple backend instances.
- Legacy SHA-256 support remains temporarily available until active accounts have migrated to BCrypt.
- Password recovery, token revocation, Multi-Factor Authentication, recovery codes, and passkeys are not yet implemented.
- Formal decisions are currently created from eligible text messages; attachment-based decisions remain future work.
- Append-only governance events are implemented, but a dedicated authorized governance-history interface is not yet available.
- The `Needs attention` value is currently a frontend placeholder rather than an AI-generated result.
- AI summaries, AI search/RAG, the AI assistant, smart classification, and improved directory search are not yet implemented.
- The current GitHub-hosted deployment workflow requires temporary public SSH access to port 22 during deployment.
