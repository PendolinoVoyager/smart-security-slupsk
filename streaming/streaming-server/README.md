# HTTP and WebSocket Server

## Overview

This project is a hybrid HTTP and WebSocket server designed to coordinate end devices with WebSocket clients for real-time video streaming. Built in Tokio runtime, aiming for highest performance. 

## Features TODO
Major:

- **Unified Runtime**: Combines HTTP and WebSocket servers in a single Tokio runtime, sharing the same `AppContext`.
- **Real-time Coordination**: Facilitates communication between WebSocket clients and end devices to establish video streams.
- **Authentication**: Add client authentication for secure access.

Minor:
- **Stream Playback**: Saves the last video stream for retrospective playback.
- **Metrics and Monitoring**: Include metrics for server performance and usage statistics.
- **Scalability**: containerization ,ainly.


## Architecture

- **AppContext**: Centralized context shared across the HTTP and WebSocket servers to maintain state and configurations, database connections.
- **HTTP Server**: Handles REST API requests for client interactions and administrative tasks (example: get list of devices you are authorized to view).
- **WebSocket Server**: Manages bi-directional communication with clients for real-time streaming.


### HTTP Endpoints

| Endpoint         | Method | Description                      |
|------------------|--------|----------------------------------|
| `/hello`         | GET    | Get some metrics                 |

