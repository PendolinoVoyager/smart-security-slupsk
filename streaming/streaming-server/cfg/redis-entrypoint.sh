#!/bin/bash
set -m # Enable job control for proper foreground/background handling

# Start Redis server in the background
redis-server --save 20 1 --loglevel warning --protected-mode no \
    --loadmodule /opt/redis-stack/lib/redisearch.so \
    --loadmodule /opt/redis-stack/lib/rejson.so &

# Wait for Redis server to be ready
echo "Waiting for Redis server to start..."
while ! redis-cli ping | grep -q "PONG"; do
    sleep 1
done
echo "Redis server is ready."

# Run the Redis CLI command to create the index
echo "Creating index..."
redis-cli FT.CREATE idx_device ON JSON PREFIX 1 "device:" SCHEMA $.user_id AS user_id NUMERIC
echo "Index created successfully."

# Bring Redis server to the foreground
fg %1
