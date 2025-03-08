#!/bin/bash

# This script can be used to quickly start up all backend and streaming services for development purposes.
# Requires Java, Maven, Cargo (for Rust), tmux, npm, python3, docker and Unix shell.

read -p "This script will launch a tmux dashboard with multiple services. Ensure you have a large enough terminal. Continue? (y/N): " confirm
if [[ "$confirm" != "y" && "$confirm" != "Y" ]]; then
    echo "Aborting."
    exit 1
fi

SESSION_NAME="backend_dashboard"
STREAMING_SERVER_ADDR="ws://127.0.0.1:9080"

# run all the containers beforehand to not complicate things
cd IoT_backend
docker compose up -d
cd ../streaming-server
docker compose up -d
cd ..


tmux new-session -d -s $SESSION_NAME

# splitting into 4 panes
tmux split-window -h
tmux select-pane -t 0
tmux split-window -v  
tmux select-pane -t 2
tmux split-window -v  

tmux select-pane -t 0
tmux send-keys "cd IoT_backend && mvn clean install && mvn spring-boot:run" C-m  

tmux select-pane -t 1

tmux send-keys "cd sss-frontend && npm run dev" C-m 

tmux select-pane -t 2

tmux send-keys "cd streaming-server && cargo run --release" C-m

tmux select-pane -t 3

tmux send-keys "cd firmware && python3 debug_stream_controller.py STREAMING_SERVER_ADDR --id 100" C-m 

tmux attach -t $SESSION_NAME