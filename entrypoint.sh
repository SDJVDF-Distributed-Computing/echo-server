#!/bin/sh
set -e

KEYSTORE="${SMP_KEYSTORE_PATH:-certs/smp_keystore.jks}"

if [ ! -f "$KEYSTORE" ]; then
    echo "[Entrypoint] ERROR: Keystore not found at $KEYSTORE"
    echo "[Entrypoint] Generate it first with: ./scripts/generate_keystore.sh"
    exit 1
fi

exec java -jar /app/smp-server.jar
