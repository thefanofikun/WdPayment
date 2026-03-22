# Current Status

## Snapshot

Date: `2026-03-22`

This workspace has been repurposed into a `multi-channel payment gateway + payment workflow` project for a `ToB fiat payment platform`.

## Current Architecture

### Backend

Path: `backend/`

Tech stack:

- Spring Boot `3.5.12`
- Java `17`
- Maven `3.9.12`
- MySQL + Spring Data JPA

Current backend is now a real `Maven multi-module` project:

- `backend/platform-common`
  - shared API response objects
  - shared exception handling
  - shared web config
  - shared channel contracts
  - shared reference-data contracts and models
- `backend/platform-gateway`
  - unified gateway controllers
  - gateway DTOs
  - channel registry
  - channel adapters
  - gateway audit persistence
- `backend/platform-payment`
  - payment workflow controllers
  - payment DTOs
  - checker / L1 / L2 approval logic
  - payment state machine
  - payment persistence
  - reference-data service
- `backend/platform-ops`
  - monitoring controllers and services
  - routing services
  - ops persistence
- `backend/platform-app`
  - Spring Boot launcher module
  - runtime configuration assembly

Current launcher file:

- `backend/platform-app/src/main/java/com/payment/gateway/ChannelGatewayApplication.java`

Current config file:

- `backend/platform-app/src/main/resources/application.properties`

Important shared contracts introduced in this session:

- `com.payment.gateway.common.channel.ChannelCatalogProvider`
- `com.payment.gateway.common.channel.ChannelExecutionRecorder`
- `com.payment.gateway.common.reference.PaymentReferenceLookup`

Important shared models moved into `platform-common`:

- `ChannelDescriptor`
- `PayoutType`
- `MockMerchantProfile`
- `MockCustomerProfile`
- `MockBeneficiaryProfile`
- `MockSourceAccountProfile`
- `PaymentStateException`

Design result:

- `gateway` no longer directly depends on `ops` concrete implementation
- `ops` no longer directly depends on `gateway` concrete implementation
- `gateway` no longer directly depends on `payment` reference-data service
- module boundaries are cleaner and easier to expand later

### Frontend

Path: `frontend/`

Tech stack:

- Vue `3.5`
- Vite `8`

Current frontend responsibilities:

- gateway console
- routing and monitoring console
- payment workflow console

### Docs

Path: `docs/`

Important files:

- `docs/payment-platform-blueprint.md`
- `docs/codex-project-context.md`
- `docs/channel-gateway-mvp.md`
- `docs/project-overview.md`
- `docs/project-overview.zh-CN.md`
- `docs/current-status.md`
- `docs/sql/mysql-module-schema.sql`

## Session Rule

Required ongoing project rule:

1. Every meaningful task completion must update `docs/current-status.md`
2. If the task changes startup, environment, architecture, or operating instructions, update `README.md` too
3. New chat sessions should continue from these updated markdown files first

## What Is Already Done

Completed platform capabilities:

1. Removed the old message-board workspace and rebuilt it as `backend + frontend + docs`
2. Built unified gateway APIs for:
   - customer onboarding
   - virtual account creation
   - beneficiary creation
   - payout initiation
   - webhook ingestion
3. Implemented multi-channel routing based on channel adapters
4. Added mock channels:
   - `APEX_PAY`
   - `HARBOR_SWITCH`
5. Added first real-provider integration scaffold:
   - `SGB`
6. Mapped SGB published interfaces for:
   - `POST /va/account/create`
   - `POST /payment/remittance/payout`
   - `POST /payment/intra/transfer`
   - pay-in webhook normalization
   - pay-out webhook normalization
7. Enabled SGB simulation mode when real credentials are not configured
8. Added routing recommendations sorted by success rate and latency
9. Added channel monitoring snapshots and persistence
10. Added route history persistence
11. Built payment workflow module with:
   - inbound and outbound order creation
   - checker / L1 / L2 approvals
   - status machine
   - idempotency and duplicate prevention
   - gateway handoff after approval
   - cancel / complete / fail / retry operations
   - payment event timeline
   - payment filters
12. Added Vue frontend support for:
   - gateway workspace
   - monitoring workspace
   - payment workspace
13. Switched runtime storage from `H2` to `MySQL`
14. Split MySQL storage into:
   - `gateway_db`
   - `payment_db`
   - `ops_db`
15. Verified MySQL persistence for:
   - payment order
   - approval records
   - payment events
   - gateway audit logs
   - monitoring snapshots
   - route history
16. Upgraded backend runtime to:
   - Spring Boot `3.5.12`
   - Java `17`
17. Upgraded frontend toolchain to:
   - Vue `3.5`
   - Vite `8`
18. Refactored backend package layout into functional domains:
   - `common`
   - `gateway`
   - `payment`
   - `ops`
19. Upgraded backend again in this session from package-level modularization to a real `Maven multi-module` structure:
   - `platform-common`
   - `platform-gateway`
   - `platform-payment`
   - `platform-ops`
   - `platform-app`
20. Extracted shared contracts and models into `platform-common` to remove cross-module coupling
21. Verified backend multi-module compile successfully with Java 17:
   - using temporary `JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.18.8-hotspot`
   - command: `mvn -q -DskipTests compile`
22. Updated startup documentation for IntelliJ IDEA:
   - Spring Boot run config for `platform-app`
   - npm run config for frontend
   - Compound Run Configuration
   - Services window for grouped startup and console management
23. Added `docs/project-overview.md` as a fuller handoff document covering:
   - frontend interaction flow
   - backend module responsibilities
   - request and data flow
   - database split
   - current scope and missing enterprise gaps
24. Added `docs/project-overview.zh-CN.md` as a Chinese handoff version for easier local review and future collaboration

## Current Runtime and Startup Notes

Current local runtime model:

- backend is multi-module in code and Maven structure
- backend is still one Spring Boot process at runtime
- only `platform-app` should be started directly

IntelliJ startup recommendation:

1. import `backend/pom.xml`
2. set project SDK and Maven runner to `JDK 17`
3. run `platform-app` main class
4. run frontend with `npm run dev`
5. create one `Compound` configuration to start both together
6. manage them from the `Services` window

Current machine-specific environment fact:

- Maven exists at `C:\tools\apache-maven-3.9.12`
- JDK 17 exists at `C:\Program Files\Eclipse Adoptium\jdk-17.0.18.8-hotspot`
- shell `JAVA_HOME` is still pointing to old JDK 8 unless overridden manually

## Current Limitations

Not done yet:

1. SGB production traffic still requires real credentials and real API host
2. Monitoring retention and cleanup strategy are not implemented yet
3. No authentication or authorization yet
4. No merchant-level credential management yet
5. No merchant-level route policy management yet
6. Webhook signature verification and replay protection are still incomplete
7. The backend has been split into Maven modules, but not yet into multiple independently deployable Spring Boot apps
8. Old empty `backend/src` directories may still remain visible in the filesystem because cleanup was blocked in this environment, but active code has already moved into the new module directories

## Recommended Next Step

Best next engineering path:

1. continue filling out `payment` module capabilities
2. add auth and maker-checker permission boundaries
3. introduce merchant / channel credential management
4. harden webhook verification and retry handling
5. if needed later, split `platform-app` into multiple deployable Boot services

## How To Resume In The Next Chat

At the beginning of a new conversation, send:

```text
Please read docs/codex-project-context.md, docs/payment-platform-blueprint.md, docs/channel-gateway-mvp.md, docs/project-overview.md, docs/project-overview.zh-CN.md, and docs/current-status.md before continuing.
```

Then add your next instruction, for example:

```text
Continue from the current multi-module payment platform workspace and keep extending the payment module.
```

## Important Reminder

The model does not automatically keep permanent memory across all future chats.

The reliable continuation method is:

1. keep these markdown files in the repo
2. ask Codex to read them first in every new session
