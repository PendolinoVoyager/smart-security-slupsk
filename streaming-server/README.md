# Streaming Server

## Purpose

Acts as a central gateway for:

- Edge Devices - Receive real-time media streams and manage sessions

- Browser Clients - Distributes streams through WebSocket-backed web interfaces

- System Services - Provides methods for stream getting media stream from the devic

Serves as the coordination layer between physical devices and web-based consumers while maintaining session state across the ecosystem.

## Key Features

- Dual-Mode Server - Unified HTTP/WebSocket engine using Rust's async capabilities

- Session Orchestration - Redis-backed device/client session tracking

- Stream Bridging - Protocol translation between stream ingest and distribution

- Auth Gateway - Centralized authentication for both devices and end-users

- Scalable Core - Tokio-based architecture for high-concurrency workloads

- Observability - Built-in metrics endpoints and logging integration (Loki and Graphana stack)

## Architecture Overview

## Enviromental variables

Instead of using provided .cfg files, enviromental variables will take precedence if `STRSVR_ENV_ON` enviromental variable is set to `1`.

List of variables that influence config:

- STRSRV_ENV - env (production / development)
- STRSRV_LOG - log
- STRSRV_DB_URI - db_uri
- STRSRV_REDIS_DB_URI - redis_db_uri
- STRSRV_TOKENS_ARE_IDS - tokens_are_ids

- STRSRV_HTTP_ADDR - http address to bind to
- STRSRV_HTTP_PORT - http port to bind to
- STRSRV_HTTP_CORS - cors-allow-origin

- STRSRV_WS_ADDR - websocket address to bind to
- STRSRV_WS_PORT - http port to bind to
