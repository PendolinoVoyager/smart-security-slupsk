#!/bin/bash

DB_CONTAINER_NAME="ass_dev_db"
DB_USER="master"
DB_PASSWORD="12345678"
DB_NAME="ass_dev"
DB_PORT=5432
POSTGRES_IMAGE="postgres:latest"

# Check if Docker is installed
if ! command -v docker &> /dev/null
then
    echo "Docker not found. Please install Docker."
    exit 1
fi

# Pull the PostgreSQL image if not available
echo "Checking if the PostgreSQL Docker image is available..."
docker image inspect $POSTGRES_IMAGE > /dev/null 2>&1 || {
    echo "Pulling PostgreSQL Docker image..."
    docker pull $POSTGRES_IMAGE
}

# Run PostgreSQL container
echo "Starting PostgreSQL container..."
docker run --name $DB_CONTAINER_NAME -e POSTGRES_USER=$DB_USER \
           -e POSTGRES_PASSWORD=$DB_PASSWORD -e POSTGRES_DB=$DB_NAME \
           -p $DB_PORT:5432 -d $POSTGRES_IMAGE

# Wait a few seconds for the container to initialize
echo "Waiting for PostgreSQL to initialize..."
sleep 5

# Check if the container is running
if [ "$(docker inspect -f '{{.State.Running}}' $DB_CONTAINER_NAME)" != "true" ]; then
    echo "Failed to start PostgreSQL container."
    exit 1
fi

# Connect to the database using psql
echo "Connecting to PostgreSQL database..."
docker exec -it $DB_CONTAINER_NAME psql -U $DB_USER -d $DB_NAME

# Usage instructions
echo "PostgreSQL database '$DB_NAME' is running on port $DB_PORT."
echo "Access it with psql using: docker exec -it $DB_CONTAINER_NAME psql -U $DB_USER -d $DB_NAME"

