#!/bin/bash

# Change directory to the script location
cd "$(dirname "$0")"

echo "--- Starting data update from local source: $(date) ---"

# Settings
SOURCE_FILE="/home/openmap/openmap/db/data.pbf"
DEST_FILE="data/lithuania-latest.osm.pbf"

# Check if the source file exists
if [ -f "$SOURCE_FILE" ]; then
    echo "Source file found at $SOURCE_FILE."

    # 1. Stop the container
    echo "Stopping GraphHopper..."
    docker compose -f docker-compose.yml down

    # 2. Clear old cache (REQUIRED: otherwise GH uses old graph with new data)
    echo "Clearing old cache..."
    rm -rf data/graph-cache

    # 3. Copy the local PBF file to our project directory
    echo "Copying PBF file..."
    cp "$SOURCE_FILE" "$DEST_FILE"

    # 4. Restart the container (re-import starts automatically)
    echo "Restarting GraphHopper..."
    docker compose -f docker-compose.yml up -d

    echo "Update completed successfully."
else
    echo "ERROR: Source file NOT FOUND at $SOURCE_FILE. Update aborted."
    exit 1
fi
