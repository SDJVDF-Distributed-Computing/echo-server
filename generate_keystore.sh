#!/bin/bash

if [ -f .env ]; then
    set -a
    . ./.env
    set +a
fi

KEYSTORE="${SMP_KEYSTORE_PATH:-certs/smp_keystore.jks}"
PASSWORD="${SMP_KEYSTORE_PASSWORD:-changeit}"
ALIAS="${SMP_KEYSTORE_ALIAS:-smpserver}"
CERT_FILE="${KEYSTORE%.jks}.cer"

mkdir -p "$(dirname "$KEYSTORE")"

echo "Generating self-signed TLS keystore: $KEYSTORE"

keytool -genkeypair \
    -alias "$ALIAS" \
    -keyalg RSA \
    -keysize 2048 \
    -validity 365 \
    -keystore "$KEYSTORE" \
    -storepass "$PASSWORD" \
    -keypass  "$PASSWORD" \
    -dname "CN=localhost, OU=SMP, O=University, L=City, S=State, C=UG"

echo "Exporting public cert: $CERT_FILE"

keytool -export \
    -alias "$ALIAS" \
    -keystore "$KEYSTORE" \
    -storepass "$PASSWORD" \
    -file "$CERT_FILE" \
    -rfc
