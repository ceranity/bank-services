#!/bin/bash

# Check if the script is running as root or with sudo
if [[ $EUID -ne 0 ]]; then
   echo "This script must be run as root. Please run it with 'sudo' or as a superuser (su)." 
   exit 1
fi

# Function to kill all child processes on exit
cleanup() {
    echo "Stopping all services..."
    kill 0
}

# Trap Ctrl+C (SIGINT) to run the cleanup function
trap cleanup SIGINT

# Open a new terminal with multiple tabs for each service
gnome-terminal \
--tab --title="service-registry" -- bash -c "docker compose -f service-registry/docker-compose.yml build && docker compose -f service-registry/docker-compose.yml up & exec bash" \
--tab --title="config-server" -- bash -c "docker compose -f config-server/docker-compose.yml build && docker compose -f config-server/docker-compose.yml up & exec bash" \
--tab --title="user-service" -- bash -c "docker compose -f user-service/docker-compose.yml build && docker compose -f user-service/docker-compose.yml up & exec bash" \
--tab --title="accounts-service" -- bash -c "docker compose -f accounts-service/docker-compose.yml build && docker compose -f accounts-service/docker-compose.yml up & exec bash" \
--tab --title="agentname-service" -- bash -c "docker compose -f agentname-service/docker-compose.yml build && docker compose -f agentname-service/docker-compose.yml up & exec bash" \
--tab --title="loan-service" -- bash -c "docker compose -f loan-service/docker-compose.yml build && docker compose -f loan-service/docker-compose.yml up & exec bash" \
--tab --title="transaction-service" -- bash -c "docker compose -f transaction-service/docker-compose.yml build && docker compose -f transaction-service/docker-compose.yml up & exec bash" \
--tab --title="api-gateway" -- bash -c "docker compose -f api-gateway/docker-compose.yml build && docker compose -f api-gateway/docker-compose.yml up & exec bash" &

# Wait for all background processes to complete
wait
