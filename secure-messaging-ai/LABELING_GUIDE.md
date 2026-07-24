# SMP Attention Classification Labeling Guide

## Objective

Classify whether a received direct message likely requires the receiver's attention or action.

## Labels

### `1` — Needs Attention

Use this label when the message contains one or more of the following:

- A direct question requiring a response
- A request or assigned action
- A deadline or time-sensitive requirement
- An urgent or high-risk situation
- An unresolved issue
- A request for confirmation
- A follow-up that remains incomplete

### `0` — Normal

Use this label when the message is primarily:

- Informational
- Acknowledgment
- Social conversation
- A completed-status update
- A greeting or closing
- A statement that does not request action

## Categories

Suggested categories:

- `urgent`
- `question`
- `follow_up`
- `deadline`
- `unresolved`
- `normal`

## Important Rule

The classifier is advisory. It does not automatically send messages, mark messages as read, block users, submit reports, or perform governance actions.
