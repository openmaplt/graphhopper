#!/bin/bash

# Change directory to the script location
cd "$(dirname "$0")"

echo "--- Starting data update: $(date) ---"

# Settings
URL="https://download.geofabrik.de/europe/lithuania-latest.osm.pbf"
DEST_FILE="data/lithuania-latest.osm.pbf"
TEMP_FILE="${DEST_FILE}.tmp"

echo "Downloading new map..."
if curl -L -o "$TEMP_FILE" "$URL"; then
    echo "Download successful."

    # 1. Stop the container
    echo "Stopping GraphHopper..."
    docker compose -f docker-compose.yml down

    # 2. Clear old cache (REQUIRED: otherwise GH uses old graph with new data)
    echo "Clearing old cache..."
    rm -rf data/graph-cache

    # 3. Replace the old PBF file with the new one
    mv "$TEMP_FILE" "$DEST_FILE"

    # 4. Restart the container (re-import starts automatically)
    echo "Restarting GraphHopper..."
    docker compose -f docker-compose.yml up -d

    echo "Update completed successfully."
else
    echo "ERROR: Failed to download the map."
    rm -f "$TEMP_FILE"
    exit 1
fi
