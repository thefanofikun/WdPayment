# Multi-Channel Payment Platform Workspace

This workspace is dedicated to a `ToB fiat payment platform`. The current implementation focus is:

- `channel gateway`
- `payment workflow`
- `routing and monitoring`

## Working Rule

- Every meaningful task completion must update `docs/current-status.md`
- If startup, architecture, environment, or operating instructions change, update `README.md` too
- In a new chat, first ask Codex to read:
  - `docs/codex-project-context.md`
  - `docs/payment-platform-blueprint.md`
  - `docs/channel-gateway-mvp.md`
  - `docs/project-overview.md`
  - `docs/project-overview.zh-CN.md`
  - `docs/current-status.md`

## Workspace Structure

- `backend/`
  - Maven multi-module Spring Boot backend
- `frontend/`
  - Vue 3 + Vite web console
- `docs/`
  - architecture, progress, and SQL docs

Recommended full project handoff doc:

- `docs/project-overview.md`
- `docs/project-overview.zh-CN.md`

## Backend Modules

The backend has been upgraded from a single module into a real Maven multi-module project.

- `backend/platform-common`
  - shared API response models
  - shared exception handling
  - shared web config
  - shared channel contracts
  - shared reference-data contracts and mock reference models
- `backend/platform-gateway`
  - unified upstream gateway APIs
  - request normalization and downstream translation
  - channel registry
  - channel adapters
  - gateway audit persistence
- `backend/platform-payment`
  - inbound and outbound payment workflow
  - checker / L1 / L2 approvals
  - idempotency and duplicate prevention
  - payment order persistence
  - payment reference-data service
- `backend/platform-ops`
  - routing recommendations
  - channel monitoring
  - route history persistence
  - metrics snapshot persistence
- `backend/platform-app`
  - the only Spring Boot launcher module
  - shared runtime configuration
  - final assembly module used by IDEA and Maven startup

## Current Runtime Model

The backend is now `multi-module`, but still runs as `one Spring Boot process`.

This is intentional for the current phase:

- easier local startup
- lower integration risk
- clear module boundaries
- ready to evolve into multiple deployable services later

Current launcher:

- `backend/platform-app/src/main/java/com/payment/gateway/ChannelGatewayApplication.java`

Current config:

- `backend/platform-app/src/main/resources/application.properties`

## Current Gateway Scope

Unified upstream APIs currently cover:

- customer onboarding
- virtual account creation
- beneficiary creation
- payout initiation
- webhook ingestion

Current platform capabilities also include:

- success-rate based routing
- channel monitoring
- payment order creation
- checker / L1 / L2 approvals
- payment state machine
- idempotent request handling
- gateway audit logs
- MySQL persistence split by module database

## Channel Status

Mock channels:

- `APEX_PAY`
- `HARBOR_SWITCH`

Real channel in progress:

- `SGB`

Current SGB behavior:

- `gateway.channel.sgb.enabled=true` is enabled by default
- if SGB host or keys are empty, the adapter returns simulated successful responses
- once real SGB host and keys are configured, the same code path switches to real downstream HTTP calls

SGB config path:

- `backend/platform-app/src/main/resources/application.properties`

## Database

The project uses `MySQL`, not `H2`.

Current module databases:

- `gateway_db`
- `payment_db`
- `ops_db`

Initialization SQL:

- `docs/sql/mysql-module-schema.sql`

Default local config:

- host: `localhost`
- port: `3306`
- username: `root`
- password: `root`

Environment variable overrides:

- `MYSQL_HOST`
- `MYSQL_PORT`
- `MYSQL_DEFAULT_DB`
- `MYSQL_USER`
- `MYSQL_PASSWORD`

## How To Start Quickly

### Backend in IntelliJ IDEA

1. Open the `backend/` project by importing `backend/pom.xml`
2. Click Maven reload so IDEA recognizes:
   - `platform-common`
   - `platform-gateway`
   - `platform-payment`
   - `platform-ops`
   - `platform-app`
3. Open `backend/platform-app/src/main/java/com/payment/gateway/ChannelGatewayApplication.java`
4. Run the `main` method from `platform-app`

Recommended local JDK in IDEA:

- set project SDK to `JDK 17`
- set Maven runner JDK to `JDK 17`

On this machine, a detected JDK 17 path is:

- `C:\Program Files\Eclipse Adoptium\jdk-17.0.18.8-hotspot`

### Frontend in IntelliJ IDEA

Frontend does not use a Java `main` method.

Use either of these:

- open terminal in `frontend/` and run `npm run dev`
- create an `npm` run configuration for script `dev`

Frontend default address:

- `http://localhost:5173`

### Start Backend + Frontend Together

In IntelliJ IDEA, the feature you remember is usually:

- `Compound Run Configuration`
- with the `Services` tool window

Recommended setup:

1. Create one `Spring Boot` run configuration for `platform-app`
2. Create one `npm` run configuration for `frontend -> dev`
3. Create one `Compound` run configuration, for example `local-all`
4. Add both configurations into `local-all`
5. Open `View -> Tool Windows -> Services`
6. Start `local-all`

That gives you one place to:

- start both
- stop both
- restart one by one
- view grouped consoles

### If Later You Split Into Multiple Boot Apps

If later we split `gateway`, `payment`, `ops`, `merchant`, `risk` into independent deployable services, IDEA startup is still the same pattern:

1. create one Spring Boot run config per app
2. create one frontend `npm` config if needed
3. group them into one `Compound` config
4. manage them in the `Services` window

## Command Line Startup

### Backend

From `backend/`:

```bash
mvn -pl platform-app spring-boot:run
```

If your shell still points to Java 8, run with a temporary Java 17 override:

```powershell
$env:JAVA_HOME='C:\Program Files\Eclipse Adoptium\jdk-17.0.18.8-hotspot'
$env:Path="$env:JAVA_HOME\bin;C:\tools\apache-maven-3.9.12\bin;$env:Path"
mvn -pl platform-app spring-boot:run
```

Backend default address:

- `http://localhost:8080`

Health check:

- `http://localhost:8080/actuator/health`

### Frontend

From `frontend/`:

```bash
npm install
npm run dev
```

If backend uses another port:

```powershell
$env:VITE_API_TARGET='http://localhost:18080'
npm run dev
```

## Build Verification

Current verified compile command:

```powershell
$env:JAVA_HOME='C:\Program Files\Eclipse Adoptium\jdk-17.0.18.8-hotspot'
$env:Path="$env:JAVA_HOME\bin;C:\tools\apache-maven-3.9.12\bin;$env:Path"
mvn -q -DskipTests compile
```

## Key Backend APIs

- `POST /api/gateway/customers/onboarding`
- `POST /api/gateway/virtual-accounts`
- `POST /api/gateway/beneficiaries`
- `POST /api/gateway/payouts`
- `POST /api/gateway/webhooks/ingest`
- `GET /api/catalog/channels`
- `GET /api/routing/recommendations`
- `GET /api/monitoring/channels`
- `GET /api/payment/reference-data`
- `GET /api/payment/orders`
- `POST /api/payment/orders`
- `POST /api/payment/orders/{id}/checker/approve`
- `POST /api/payment/orders/{id}/l1/approve`
- `POST /api/payment/orders/{id}/l2/approve`

## Production Direction

Current recommendation:

- local development:
  - backend runs from IDEA `platform-app`
  - frontend runs by `npm run dev`
- UAT / production:
  - backend deploy to Linux or Docker
  - frontend build static assets and serve with Nginx, or package into Docker
