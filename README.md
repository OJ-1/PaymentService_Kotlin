# Payment Service - README

**V1.0.0 — Initial Release**

---

# Overview

A Kotlin-based payment processing service built around Ktor, Koin, Restate, PostgreSQL, and TigerBeetle.

The service separates application state from financial ledger state while using durable workflows to coordinate payment processing.

---

# Technology Stack and Purpose

| Technology  | Purpose                     |
| ----------- |-----------------------------|
| Kotlin      | Application language        |
| Ktor        | HTTP API                    |
| Koin        | Dependency injection        |
| Restate     | Durable payment workflows   |
| PostgreSQL  | Data store and persistence  |
| Exposed     | Database access             |
| HikariCP    | Database connection pooling |
| TigerBeetle | Financial ledger            |

---

# Architecture

```text
                 ┌──────────────┐
                 │    Client    │
                 └──────┬───────┘
                        │
                        ▼
                 ┌──────────────┐
                 │    Ktor      │
                 │     API      │
                 └──────┬───────┘
                        │
                        ▼
               ┌─────────────────┐
               │ PaymentService  │
               └────────┬────────┘
                        │
                        ▼
               ┌─────────────────┐
               │     Restate     │
               └────────┬────────┘
                        │
                        ▼
               ┌─────────────────┐
               │ PaymentWorkflow │
               └───────┬─┬───────┘
                       │ │
              ┌────────┘ └─────────────┐
              ▼                        ▼
       ┌─────────────┐          ┌─────────────┐
       │ PostgreSQL  │          │ LedgerPort  │
       │ Application │          └──────┬──────┘
       │    State    │                 │
       └─────────────┘                 ▼
                              ┌─────────────────────────┐
                              │ TigerBeetleLedgerAdapter│
                              └────────────┬────────────┘
                                           │
                                           ▼
                                    ┌──────────────┐
                                    │ TigerBeetle  │
                                    │    Ledger    │
                                    └──────────────┘
```

PostgreSQL stores application state.

TigerBeetle provides the financial ledger.

Restate provides durable execution of the payment workflow.

---

# Project Structure

```text
src/main/kotlin/com/ojsolutions/

├── Application.kt
│
├── api/
│   ├── *routes
│   ├── request/
│   ├── response/
│   └── dto/
│
├── application/
│   └── *services
│
├── domain/
│   ├── *entities
│   ├── enums
│   ├── ledger/
│   └── port/
│
└── infrastructure/
├── database/
│   ├── DatabaseFactory
│   ├── DatabaseConfig
│   ├── DatabaseSeeder
│   ├── repository/
│   └── table/
│
├── ledger/
│   ├── TigerBeetleLedgerAdapter
│   └── LedgerSeeder
│
└── workflow/
├── PaymentWorkflow
├── RestateModule
└── RestateWorkflowAdapter
```

The project follows a ports-and-adapters / hexagonal architecture, keeping the  domain and application layers independent of infrastructure implementations.

---

# Getting Started

## Prerequisites

You will need:

* JDK
* Gradle - only required if you are not using the project's Gradle Wrapper.
* Docker
* Docker Compose
* Powershell and CMD

---

## Development Environment

| Service                   | Address          |
| ------------------------- | ---------------- |
| Ktor API                  | `localhost:8080` |
| Restate Admin / UI        | `localhost:9070` |
| Restate Ingress           | `localhost:9072` |
| Restate Workflow Endpoint | `localhost:9090` |
| PostgreSQL                | `localhost:5433` |
| TigerBeetle               | `localhost:3000` |

These are local development settings.

## API

The service exposes APIs for:

* Customers
* Merchants
* Systems
* Accounts
* Payments
* Account transfers

Swagger/OpenAPI is available from the running application.

`http://localhost:8080/swagger`

`http://localhost:8080/openapi`

---

# Getting Started

## Getting Started

### First Time Setup

Before starting the application for the first time, create the TigerBeetle ledger:

```powershell
.\format-tigerbeetle.ps1
```

This is only required when creating a new empty TigerBeetle ledger or after intentionally deleting the ledger data.

---

## Start the Application

The recommended way to run the complete application is with Docker Compose.

From the project root:

```powershell
cd docker

docker compose up --build -d
```

This starts:

* PostgreSQL
* TigerBeetle
* Restate
* PaymentService
* Restate deployment registration

Once running, access:

| Service                       | URL                                  |
| ----------------------------- | ------------------------------------ |
| **Payment API / Swagger**     | `http://localhost:8080/swagger`      |
| **Restate UI**                | `http://localhost:9070/ui/workflows` |
| **Restate Ingress**           | `http://localhost:9072`              |
| **Restate Workflow Endpoint** | `http://localhost:9090`              |

To verify that the containers are running:

```powershell
docker ps
```

To view logs:

```powershell
docker logs payment-service
docker logs restate
docker logs restate-register
```

---

## Running the Application from IntelliJ

If you want to run the Kotlin application directly from IntelliJ instead of Docker, first stop the `payment-service` container.

From the `docker` directory:

```powershell
docker compose stop payment-service
```

The remaining infrastructure services will continue running in Docker.

You can then start the application from IntelliJ.

When finished, you can start the container again:

```powershell
docker compose start payment-service
```

> Do not run the PaymentService from IntelliJ and Docker at the same time, as both configurations use the same application ports.

---

## Restate Deployment Registration

The `restate-register` container should register the PaymentService deployment automatically.

If the Restate deployment was not registered, or Restate data was reset, you can register it manually:

```powershell
$body = @{
    uri = "http://host.docker.internal:9090"
} | ConvertTo-Json

Invoke-RestMethod `
    -Method Post `
    -Uri "http://localhost:9070/deployments" `
    -ContentType "application/json" `
    -Body $body
```

Verify the deployment:

```powershell
Invoke-RestMethod "http://localhost:9070/deployments"
```

Payment workflow executions can then be viewed at:

`http://localhost:9070/ui/workflows`

---

## Stop the Application

To stop all containers:

```powershell
docker compose down
```

This stops and removes the containers but does not delete the persisted data directories.

---

## Reset Data

### Full Reset

This deletes all PostgreSQL, TigerBeetle, and Restate data:

```powershell
docker compose down

Remove-Item .\tigerbeetle\data\0_0.tigerbeetle
Remove-Item .\postgres\data\* -Recurse -Force
Remove-Item .\restate\data\* -Recurse -Force

.\format-tigerbeetle.ps1

docker compose up -d
```

---

## Run Tests

From the project root:

```powershell
.\gradlew clean test
```

---

# USEFUL DOCKER COMMANDS

| Description                       | Command                           |
| --------------------------------- | --------------------------------- |
| View all running containers       | `docker ps`                       |
| View logs for the Payment Service | `docker logs payment-service`     |
| Follow Payment Service logs       | `docker logs -f payment-service`  |
| View Restate logs                 | `docker logs restate`             |
| View Restate registration logs    | `docker logs -f restate-register` |
