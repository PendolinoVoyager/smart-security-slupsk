# Streaming Server

## Dev quick run

To run the server quickly, `docker compose -f compose.dev.yaml up` firt to get the dependencies going.

## Build

The whole thing will build with:

`docker compose up --build`
It will create a docker container running the server with provided configs and public key.
**Make sure to check if ports in compose.yaml and cfg/cfg.yaml match** 
## JWT Tokens

Make sure cfg/jwt_pub_key.pem is kept up to date as it's used to read JWT Tokens.

STRSRV_JWT_PUB_KEY_PATH will override default `cfg/jwt_pub_key.pem` path.

## Purpose

Acts as a central gateway for:

- Edge Devices - Receive real-time media streams and manage sessions

- Browser Clients - Distributes streams through WebSocket-backed web interfaces

- System Services - Provides methods for stream getting media stream from the devic

Serves as the coordination layer between physical devices and web-based consumers while maintaining session state across the ecosystem.

## Enviromental variables

STRSRV_APP_CONFIG environmental variable overrides default `cfg/config.yaml' path.

STRSRV_JWT_PUB_KEY_PATH will override default `cfg/jwt_pub_key.pem` path.

STRSRV_OPENAPI_YAML_PATH will override default `cfg/openapi.yaml` path.

Instead of using provided config.yaml file, enviromental variables will take precedence if `STRSVR_ENV_ON` enviromental variable is set to `1`.

List of variables that influence config:


- STRSRV_ENV - env (production / development)
- STRSRV_LOG - log
- STRSRV_DB_URI - db_uri
- STRSRV_REDIS_DB_URI - redis_db_uri
- STRSRV_LOKI_URL - loki url, default is http://127.0.0.1:3100
- STRSRV_TOKENS_ARE_IDS - tokens_are_ids

- STRSRV_HTTP_ADDR - http address to bind to
- STRSRV_HTTP_PORT - http port to bind to
- STRSRV_HTTP_CORS - cors-allow-origin

- STRSRV_WS_ADDR - websocket address to bind to
- STRSRV_WS_PORT - http port to bind to
