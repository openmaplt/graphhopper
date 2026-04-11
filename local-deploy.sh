#!/bin/bash

# --- CONFIGURATION (Change these) ---
SSH_USER="your-user"
SSH_HOST="your-server-ip"
REMOTE_PATH="server-path-to-graphhopper"
# -----------------------------------

echo "--- 1. Building GraphHopper locally ---"
mvn clean install -DskipTests

if [ $? -ne 0 ]; then
    echo "ERROR: Maven build failed!"
    exit 1
fi

echo "--- 2. Preparing remote directory ---"
ssh ${SSH_USER}@${SSH_HOST} "mkdir -p ${REMOTE_PATH}/data"

echo "--- 3. Uploading JAR file ---"
# Find the built JAR (web/target/graphhopper-web-*.jar)
JAR_FILE=$(ls web/target/graphhopper-web-*.jar | head -n 1)
scp "$JAR_FILE" ${SSH_USER}@${SSH_HOST}:${REMOTE_PATH}/graphhopper.jar

echo "--- 4. Uploading configuration and Docker files ---"
scp Dockerfile docker-compose.yml config-openmap.yml update-data.sh ${SSH_USER}@${SSH_HOST}:${REMOTE_PATH}/

echo "--- 5. Restarting GraphHopper on server ---"
ssh ${SSH_USER}@${SSH_HOST} "chmod +x ${REMOTE_PATH}/update-data.sh && cd ${REMOTE_PATH} && docker compose -f docker-compose.yml up -d --build"

echo "--- DONE! GraphHopper is updating on the server ---"
