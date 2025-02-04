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

mermaid
Copy

graph TD
EdgeDevice -->|WebSocket| Server
Browser -->|HTTP/WS| Server
Server -->|Session Data| Redis[(Redis)]
Server -->|API| BackendServices
Server -->|Metrics| Monitoring
