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

The map is refreshed from `[server-path-to-osm-source]/data.pbf` every Friday at 03:00 by a cron job of the **`[server-user]`** user
(must NOT be root, see below):

```bash
crontab -e -u [server-user]
0 3 * * 5 [server-path-to-graphhopper]/update-data.sh >> [server-path-to-graphhopper]/update.log 2>&1
```

The container runs as that same user (`user: "1000:1000"` in `docker-compose.yml`), so `data/` stays writable for the script.
If you ever run something in `data/` as root, fix it with `chown -R [server-user]:[server-user] data`.
`update-data.sh` stops on the first error (`set -e`) – check `update.log` for `ERROR`/`Permission denied`.

**Note:** `local-deploy.sh` does not clear `data/graph-cache`. After a GraphHopper upgrade or a change of
`graph.encoded_values` run `./update-data.sh` (or delete `data/graph-cache`) so the graph is rebuilt.

## 🔍 Monitoring
Check the logs of the running container:
```bash
docker compose -f docker-compose.yml logs -f
```
