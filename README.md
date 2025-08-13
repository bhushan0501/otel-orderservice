# otel-orderservice

A Java Spring Boot microservice for managing orders, instrumented with OpenTelemetry for distributed tracing and metrics. This project is designed to demonstrate how to integrate observability into modern microservices using the OpenTelemetry Java Agent for no-code instrumentation and OpenTelemetry APIs for manual instrumentation, with logs and traces exported to [Signoz](https://signoz.io/) for monitoring and analysis.

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Endpoints](#endpoints)
- [Observability with OpenTelemetry](#observability-with-opentelemetry)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Running the App](#running-the-app)
- [Configuration](#configuration)

---

## Overview

`otel-orderservice` is an order management service implemented in Java using Spring Boot. It supports the creation and tracking of orders and includes built-in instrumentation for distributed tracing and metrics using OpenTelemetry. The service exposes REST endpoints for order operations and interacts with a database to persist order data.

## Features

- **Order Management**: Create orders, track status (`SUCCESS`, `FAILED`, `PROCESSING`), and check inventory.
- **Database Integration**: Uses Spring Data JPA for persistence.
- **OpenTelemetry Instrumentation**: 
  - Custom spans for database operations.
  - Attributes and events for tracing order lifecycle.
  - Error handling and status reporting in traces.
- **Observability**: Exports traces and logs to Signoz via OTLP.
- **Simulated Processing Delays**: Random delays introduced to mimic real-world DB latency and order failures.

## Endpoints

### `POST /createOrder`
Creates a new order with simulated processing delay.
- 90% chance of success, 10% chance of simulated failure.
- Persists order with status (`SUCCESS` or `FAILED`).
- Trace spans include operation details, random delays, and error reasons.

### `GET /checkInventory`
Checks the inventory by querying the database for `SUCCESS` and `FAILED` order counts.
- Introduces a random delay between 200ms and 800ms.
- Returns counts and a summary message.

## Observability with OpenTelemetry

This service is instrumented with OpenTelemetry both via the Java agent and custom manual spans in code. Key operations (like creating and saving orders, database queries, and error cases) are tracked with spans, events, and attributes. This allows:
- Deep visibility into service behavior.
- Correlation of failures and performance bottlenecks.
- Monitoring via Signoz.

## Getting Started

### Prerequisites

- Java 17+
- Maven
- [Signoz Cloud](https://signoz.io/teams/)

### Running the App

Start the service with OpenTelemetry instrumentation and export traces/logs to Signoz:

```bash
OTEL_LOGS_EXPORTER=otlp \
OTEL_EXPORTER_OTLP_ENDPOINT="<SIGNOZ_ENDPOINT>" \
OTEL_EXPORTER_OTLP_HEADERS="signoz-access-token=<SIGNOZ_INGESTION_KEY>" \
OTEL_RESOURCE_ATTRIBUTES="service.name=<APP_NAME>" \
java -javaagent:opentelemetry-javaagent.jar \
     -jar target/orderservice-1.0.0.jar
```

Replace:
- `<SIGNOZ_ENDPOINT>`: The OTLP endpoint for your Signoz instance 
- `<SIGNOZ_INGESTION_KEY>`: Your Signoz ingestion access token.
- `<APP_NAME>`: Service name for resource attributes (e.g., `otel-orderservice`).

## Configuration

- **OTEL_LOGS_EXPORTER**: Set to `otlp` to export logs.
- **OTEL_EXPORTER_OTLP_ENDPOINT**: URL for the OTLP collector (Signoz).
- **OTEL_EXPORTER_OTLP_HEADERS**: Include required authentication header.
- **OTEL_RESOURCE_ATTRIBUTES**: Set service name for identification.
- **opentelemetry-javaagent.jar**: Download from [OpenTelemetry releases](https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases).
