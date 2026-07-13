# Current Application State

This document summarizes the currently implemented and deployed behavior of the Secure Messaging Platform. It is updated after significant feature milestones and may evolve as security policies and application workflows are refined.

## Authentication

## Direct Messaging

## Group Messaging

## Group Membership and Roles
- All groups are private and invitation-only.
- Users cannot join groups directly by entering a group ID.
- Membership is created only when an invited user accepts a pending invitation.
- Group owners and administrators can invite registered users.
## Group Invitations
- Owners and administrators can invite registered users by username.
- Invited users can accept or decline pending invitations.
- Accepting an invitation creates group membership.
- Declining an invitation does not create membership.
- The legacy direct-join endpoint returns `403 Forbidden`.
## Group Attachments

## Message Search

## Read Status

## Edit and Delete

## Pinning

## Real-Time Updates

## Deployment

## Known Limitations
