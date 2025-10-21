# ---------- Build stage ----------
FROM rust:1.90 AS builder

# Create a new empty shell project
WORKDIR /app

COPY Cargo.toml Cargo.lock ./
RUN cargo fetch
COPY src ./src


RUN cargo build --release
FROM debian:bookworm-slim AS runtime

# Install OpenSSL runtime dependencies
RUN apt-get update && apt-get install -y \
    libssl3 \
    ca-certificates \
 && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY --from=builder /app/target/release/streaming-server /usr/local/bin/streaming-server

COPY cfg/cfg.yaml /etc/streaming-server/cfg.yaml
COPY cfg/jwt_pub_key.pem /etc/streaming-server/jwt_pub_key.pem
COPY cfg/openapi.yaml /etc/streaming-server/openapi.yaml
 
ENV RUST_LOG=info \
    STRSRV_APP_CONFIG=/etc/streaming-server/cfg.yaml \
    STRSRV_JWT_PUB_KEY_PATH=/etc/streaming-server/jwt_pub_key.pem \
    STRSRV_OPENAPI_YAML_PATH=/etc/streaming-server/openapi.yaml


ENTRYPOINT ["streaming-server"]
