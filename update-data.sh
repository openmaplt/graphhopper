#!/bin/bash
# Refreshes the OSM data and rebuilds the GraphHopper graph.
# Run as the same user that owns the data/ directory (the container runs as that user too, see docker-compose.yml).
set -euo pipefail

# Change directory to the script location
cd "$(dirname "$0")"

echo "--- Starting data update from local source: $(date) ---"

# Settings
SOURCE_FILE="/home/openmap/openmap/db/data.pbf"
DEST_FILE="data/lithuania-latest.osm.pbf"
TMP_FILE="data/lithuania-latest.osm.pbf.new"

# Check if the source file exists
if [ ! -f "$SOURCE_FILE" ]; then
    echo "ERROR: Source file NOT FOUND at $SOURCE_FILE. Update aborted."
    exit 1
fi
echo "Source file found at $SOURCE_FILE."

# 1. Copy first, while GraphHopper is still running: if this fails, nothing has been touched.
echo "Copying PBF file..."
cp "$SOURCE_FILE" "$TMP_FILE"

# 2. Stop the container
echo "Stopping GraphHopper..."
docker compose -f docker-compose.yml down

# 3. Clear old cache (REQUIRED: otherwise GH uses old graph with new data)
echo "Clearing old cache..."
rm -rf data/graph-cache

# 4. Put the new PBF in place
mv -f "$TMP_FILE" "$DEST_FILE"

# 5. Restart the container (re-import starts automatically)
echo "Restarting GraphHopper..."
docker compose -f docker-compose.yml up -d

echo "Update completed successfully: $(date)"
