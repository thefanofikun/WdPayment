# Project Overview

## 1. Project Goal

This workspace is building a `ToB fiat payment platform` with the current focus on:

- channel gateway
- payment workflow
- routing and monitoring

Current product shape:

- frontend is a `single web console`
- backend is a `multi-module Spring Boot application`
- database is split by module into multiple MySQL schemas

The current phase is a pragmatic MVP for fast integration and workflow validation, while keeping the code structure close to enterprise-style layering.

## 2. Current Overall Architecture

### 2.1 High-level flow

The current request path is:

1. User operates the Vue web console
2. Frontend calls backend `/api/...` endpoints through Vite proxy
3. Backend accepts normalized upstream payloads
4. Gateway or payment services process the request
5. If channel interaction is needed, the gateway selects a channel adapter
6. The adapter translates the normalized payload into channel-specific fields
7. The downstream result is normalized and returned to frontend
8. Monitoring, routing history, gateway audit, and payment records are persisted into MySQL

### 2.2 Backend modules

The backend is now a real Maven multi-module project.

#### `platform-common`

Purpose:

- shared response wrapper
- shared exception handling
- shared web config
- shared contracts across modules
- shared reference-data models

Representative code:

- `ApiResponse`
- `ErrorResponse`
- `ApiExceptionHandler`
- `ChannelCatalogProvider`
- `ChannelExecutionRecorder`
- `PaymentReferenceLookup`

#### `platform-gateway`

Purpose:

- unified upstream gateway APIs
- channel routing by `channelCode`
- downstream field translation
- channel adapters
- gateway audit persistence

Representative capabilities:

- onboarding
- virtual account creation
- beneficiary creation
- payout
- webhook ingest

#### `platform-payment`

Purpose:

- order capture for inbound and outbound payments
- checker / L1 / L2 approvals
- idempotency and duplicate prevention
- status transitions
- gateway handoff after approval
- payment search and operations actions

Representative capabilities:

- create payment order
- checker approve / reject
- L1 approve / reject
- L2 approve / reject
- cancel
- mark completed
- mark failed
- retry failed submission

#### `platform-ops`

Purpose:

- monitoring
- routing ranking
- route history persistence
- channel metric snapshot persistence

Current routing policy:

- sort by `successRate desc`
- then by `averageLatencyMs asc`

#### `platform-app`

Purpose:

- final assembly module
- only Spring Boot startup module
- runtime configuration location

This means:

- code is modular
- deployment is still one backend process for now

This is a deliberate tradeoff to keep startup and local联调 simple.

## 3. Database Design

Current MySQL schemas:

- `gateway_db`
- `payment_db`
- `ops_db`

### 3.1 `gateway_db`

Used for:

- gateway audit log
- channel config seeds
- webhook event log reservation

Typical persisted data:

- normalized request trace
- translated request trace
- translated response trace
- downstream result trace

### 3.2 `payment_db`

Used for:

- payment orders
- payment approval records
- payment event timeline

Typical persisted data:

- merchant and customer context
- idempotency key
- requested channel and routed channel
- approval decisions
- payment state transitions
- gateway submission result

### 3.3 `ops_db`

Used for:

- channel metric snapshots
- route history

Typical persisted data:

- success rate
- failure count
- average latency
- last status
- last message
- ranked route results

## 4. Frontend Page Interaction

The frontend is intentionally built as a `single control console`, not as many separate pages.  
Top-level entry file:

- `frontend/src/App.vue`

There are three workspaces:

- `Channel Gateway`
- `Routing & Monitoring`
- `Payment Module`

Switching tabs does not change browser routing. It simply swaps the workspace component inside one page.

### 4.1 App shell behavior

`App.vue` holds a `workspaceTab` state.

Interaction pattern:

1. user clicks a tab button
2. `workspaceTab` changes
3. matching component is rendered:
   - `GatewayWorkspace`
   - `MonitoringWorkspace`
   - `PaymentWorkspace`

This makes the console simple and fast for internal operation use.

### 4.2 Gateway workspace interaction

Main file:

- `frontend/src/components/GatewayWorkspace.vue`

#### On load

When the component mounts:

1. it calls `GET /api/catalog/channels`
2. it loads:
   - available channels
   - payout types
3. it auto-selects the first channel if none is selected yet

#### User interaction flow

The left panel controls:

- selected channel
- selected gateway operation

Supported operations:

- onboarding
- virtual account
- beneficiary
- payout
- webhook ingest

The center panel is the operation form.

The right panel is the inspector:

- request preview
- gateway response

#### Submission flow

When the user clicks `Send Request`:

1. frontend builds a normalized payload from the form
2. payload is converted into JSON
3. frontend calls one of these APIs:
   - `POST /api/gateway/customers/onboarding`
   - `POST /api/gateway/virtual-accounts`
   - `POST /api/gateway/beneficiaries`
   - `POST /api/gateway/payouts`
   - `POST /api/gateway/webhooks/ingest`
4. backend returns normalized gateway execution result
5. frontend shows the result in the right-side response inspector

#### Why this matters

This workspace is currently the fastest way to validate:

- unified upstream parameters
- channel-specific field translation
- downstream result normalization
- SGB simulation or real mapping behavior

### 4.3 Routing & Monitoring workspace interaction

Main file:

- `frontend/src/components/MonitoringWorkspace.vue`

This workspace has three blocks:

- routing ranking
- route history
- channel monitoring

#### On load

When mounted, it loads in parallel:

- `GET /api/routing/recommendations`
- `GET /api/monitoring/channels`
- `GET /api/routing/history`

#### Routing ranking interaction

User chooses an operation such as:

- CUSTOMER_ONBOARDING
- VIRTUAL_ACCOUNT
- BENEFICIARY
- PAYOUT
- WEBHOOK

Then frontend requests:

- `GET /api/routing/recommendations?operation=...`

The table shows:

- rank
- channel
- success rate
- latency
- total traffic
- recommendation reason

#### Route history interaction

The component also calls:

- `GET /api/routing/history?operation=...`

This lets operations users see persisted historical routing decisions, not only the latest in-memory ranking.

#### Monitoring interaction

The component calls:

- `GET /api/monitoring/channels`

It displays:

- success rate
- failure count
- average latency
- last status
- last message
- updated time

This workspace is currently the operational visibility page for channel health and route confidence.

### 4.4 Payment module workspace interaction

Main file:

- `frontend/src/components/PaymentWorkspace.vue`

This is currently the most business-oriented console.

#### On load

It loads four kinds of data in parallel:

- `GET /api/payment/reference-data`
- `GET /api/payment/orders`
- `GET /api/catalog/channels`
- `GET /api/routing/recommendations?operation=...`

#### Reference data usage

Reference data is still mock data for now, but it simulates future integration from CRM or master data systems:

- merchants
- customers
- beneficiaries
- source accounts

Frontend uses these for dropdowns and default values.

#### Dynamic form behavior

There are two important reactive behaviors:

1. when `direction` changes:
   - `INBOUND` forces payment method to `VIRTUAL_ACCOUNT`
   - outbound keeps beneficiary selection
   - frontend refreshes recommended routing
2. when `merchantId` changes:
   - customer list is filtered
   - beneficiary list is filtered
   - source account list is filtered
   - first matching values are auto-selected

#### Payment order creation flow

When the user clicks `Create Payment Order`:

1. frontend builds a payload from the form
2. `amount` is converted to number
3. optional channel is either:
   - explicit requested channel
   - blank for auto-routing
4. frontend calls:
   - `POST /api/payment/orders`
5. backend creates a payment order with idempotency protection
6. frontend refreshes order list
7. the latest backend response is shown in the response inspector

#### Approval flow

Each order card shows actions based on current status.

Available actions:

- Checker approve / reject
- L1 approve / reject
- L2 approve / reject

Frontend determines button visibility by status:

- checker only for `PENDING_CHECKER_REVIEW`
- L1 only for `PENDING_L1_REVIEW`
- L2 only for `PENDING_L2_REVIEW`

When L2 approves:

1. backend submits to gateway
2. backend either uses requested channel or auto-routes
3. gateway adapter performs translation and submission
4. order status becomes:
   - `GATEWAY_SUBMITTED`, or
   - `FAILED`

#### Operations actions

After submission, operations can do:

- cancel
- mark completed
- mark failed
- retry submit

This gives the current MVP a complete operational lifecycle beyond approvals.

#### Search interaction

The search area calls:

- `GET /api/payment/orders`

with filters:

- merchantId
- status
- direction
- channelCode
- keyword

This is the current operational list view for the payment module.

## 5. Current Backend Request Flow

### 5.1 Gateway flow

For a gateway request:

1. controller receives normalized request
2. `ChannelGatewayService` calls registry
3. `ChannelRegistry` resolves the adapter by `channelCode`
4. adapter translates normalized fields into channel fields
5. adapter executes:
   - mock behavior, or
   - real SGB behavior, or
   - SGB simulation when credentials are missing
6. `ChannelGatewayService` records:
   - monitoring execution
   - gateway audit log
7. response is returned to frontend

### 5.2 Payment flow

For a payment order:

1. frontend submits create request
2. `PaymentService` checks idempotency
3. payment order is stored as `PENDING_CHECKER_REVIEW`
4. approvals are performed in sequence:
   - Checker
   - L1
   - L2
5. on L2 approval:
   - inbound requests may create virtual accounts
   - outbound requests create payouts
6. payment service calls gateway service
7. monitoring and gateway audit are recorded
8. payment order updates routed channel, gateway request ID, gateway message, and status

### 5.3 Routing and monitoring flow

When routing is queried:

1. `ChannelRoutingService` reads current channel catalog
2. it asks monitoring service for the snapshot of each operation/channel
3. it sorts by success rate and latency
4. it returns ranked recommendations
5. it persists route history into `ops_db`

When gateway execution happens:

1. `ChannelGatewayService` records success/failure and latency
2. `ChannelMonitoringService` updates in-memory accumulator
3. it also persists snapshot into `ops_db.channel_metric_snapshot`

## 6. Channel Integration Status

### 6.1 Mock channels

- `APEX_PAY`
- `HARBOR_SWITCH`

Purpose:

- validate normalized gateway contracts
- validate field translation architecture
- keep frontend and workflow moving before real providers are fully ready

### 6.2 SGB

Current SGB implementation status:

- virtual account mapping done
- remittance payout mapping done
- intra-bank payout mapping done
- webhook normalization done
- request signing logic scaffolded
- simulation mode enabled when keys are empty

Current limitation:

- production traffic still needs real host and real credentials

## 7. What Is Enterprise-like Already

The current project already includes several enterprise-oriented foundations:

- modular backend structure
- shared common module
- normalized gateway contract
- channel adapter pattern
- approval workflow
- explicit payment state machine
- idempotency protection
- routing by measurable performance
- monitoring persistence
- audit persistence
- separated databases by business concern

## 8. What Is Still Missing

For a more complete enterprise rollout, the next major gaps are:

- authentication and authorization
- maker-checker permission boundaries
- merchant-level credential management
- merchant-level route policy management
- risk and compliance checks
- webhook signature verification and replay protection
- retry and reconciliation strategy
- notification and alerting
- config management and secret management
- standardized error code system
- observability dashboards and alert thresholds

## 9. Recommended Near-term Evolution

A good next path would be:

1. keep extending `payment` module
2. split payment module internally into:
   - api
   - application
   - domain
   - infrastructure
3. add auth and roles
4. add merchant and channel credential management
5. complete SGB real smoke test
6. later decide whether to split one Boot app into multiple deployable services

## 10. How To Continue In Future Chats

At the start of a new session, use:

```text
Please read docs/codex-project-context.md, docs/payment-platform-blueprint.md, docs/channel-gateway-mvp.md, docs/project-overview.md, and docs/current-status.md before continuing.
```

That is the safest way to continue from the current state without losing context.
