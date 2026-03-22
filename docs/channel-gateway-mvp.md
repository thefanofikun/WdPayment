# Channel Gateway MVP

## Current objective

Build the first `multi-channel gateway` in this workspace before integrating the rest of the payment platform.

## Upstream capabilities

- customer onboarding
- virtual account creation
- beneficiary creation
- payout initiation
- webhook ingestion

## Core design

1. Upstream always sends normalized payloads.
2. Requests must include `channelCode`.
3. Backend resolves a channel adapter through a registry.
4. Each adapter translates the normalized payload into its own downstream format.
5. The gateway returns both the translated request and a mock downstream response for fast UI feedback.

## Why this structure

This lets us add real downstream providers later without changing the upstream API contract.

## Current mock channels

- `APEX_PAY`
- `HARBOR_SWITCH`

These are placeholders to prove:

- multi-channel routing
- field translation
- operation coverage consistency
- frontend workflow

## Next likely steps

1. Replace mock adapters with real HTTP clients for selected providers
2. Add request signing, encryption, idempotency, and retry policies
3. Persist request and response audit trails
4. Introduce merchant-level routing and channel configuration
5. Split channel credentials and per-channel validation rules

## Implemented after the initial MVP note

The workspace now also includes:

1. routing recommendations based on channel success rate
2. channel monitoring snapshots
3. payment workflow endpoints and UI
4. checker, L1, and L2 approval flow
5. idempotent payment creation using merchant plus idempotency key
