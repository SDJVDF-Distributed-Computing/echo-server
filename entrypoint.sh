#!/bin/sh
set -e

KEYSTORE="${SMP_KEYSTORE_PATH:-certs/smp_keystore.jks}"
PASSWORD="${SMP_KEYSTORE_PASSWORD:-changeit}"
ALIAS="${SMP_KEYSTORE_ALIAS:-smpserver}"
CERT_FILE="${KEYSTORE%.jks}.cer"

if [ ! -f "$KEYSTORE" ]; then
    echo "[Entrypoint] Keystore not found — generating self-signed TLS certificate..."
    mkdir -p "$(dirname "$KEYSTORE")"

    keytool -genkeypair \
        -alias "$ALIAS" \
        -keyalg RSA \
        -keysize 2048 \
        -validity 365 \
        -keystore "$KEYSTORE" \
        -storepass "$PASSWORD" \
        -keypass  "$PASSWORD" \
        -dname "CN=localhost, OU=SMP, O=University, L=City, S=State, C=UG" \
        -noprompt

    keytool -export \
        -alias "$ALIAS" \
        -keystore "$KEYSTORE" \
        -storepass "$PASSWORD" \
        -file "$CERT_FILE" \
        -rfc

    echo "[Entrypoint] Certificate written to $CERT_FILE"
fi

exec java -jar /app/smp-server.jar