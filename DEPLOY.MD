# 🚢 GraphHopper Openmap Deployment Guide

This guide describes how to deploy your local GraphHopper changes to the production server.

## 🛠 Prerequisites

1.  **Maven** installed on your local machine.
2.  **SSH** access to the server with **Docker** and **Docker Compose** installed.

## 🚀 Fast Deployment (Local Script)

Use the provided `local-deploy.sh` script to build, upload, and restart GraphHopper in one command:

1.  Open `local-deploy.sh` and set your server details:
    ```bash
    SSH_USER="your-user"
    SSH_HOST="your-server-ip"
    REMOTE_PATH="server-path-to-graphhopper"
    ```
2.  Run the script from your local machine:
    ```bash
    chmod +x local-deploy.sh
    ./local-deploy.sh
    ```

---

## 🗺 Data Update (OSM Map)

To update the Lithuania map on the server, log in to the server and run:

```bash
cd [server-path-to-graphhopper]
./update-data.sh
```

### ⏰ Automation (Cron)

To update map data automatically every night at 04:00, add a cron job on the server:

```bash
crontab -e
# Add this line (update the path if necessary):
0 4 * * * [server-path-to-graphhopper]/update-data.sh >> [server-path-to-graphhopper]/update.log 2>&1
```

## 🔍 Monitoring
Check the logs of the running container:
```bash
docker compose -f docker-compose.yml logs -f
```
