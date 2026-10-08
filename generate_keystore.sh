#!/bin/bash
# Script untuk membuat Keystore Rilis otomatis LacakPaket
set -e

KEYSTORE_NAME="my-upload-key.jks"
ALIAS="upload"
STORE_PASS="lacakpaket123"
KEY_PASS="lacakpaket123"
VALIDITY_DAYS=10000

echo "Membuat keystore rilis: $KEYSTORE_NAME..."

keytool -genkeypair \
  -v \
  -keystore "$KEYSTORE_NAME" \
  -alias "$ALIAS" \
  -keyalg RSA \
  -keysize 2048 \
  -validity "$VALIDITY_DAYS" \
  -storepass "$STORE_PASS" \
  -keypass "$KEY_PASS" \
  -dname "CN=LacakPaket, OU=Logistics, O=LacakPaket Studio, L=Jakarta, ST=DKI Jakarta, C=ID"

echo "Mengekspor base64 untuk GitHub Secrets..."
base64 -w 0 "$KEYSTORE_NAME" > "$KEYSTORE_NAME.base64"

echo ""
echo "=== KEYSTORE BERHASIL DIBUAT ==="
echo "File: $KEYSTORE_NAME"
echo "Alias: $ALIAS"
echo "Password: $STORE_PASS"
echo "Base64: $KEYSTORE_NAME.base64"
echo "================================"

keytool -list -v -keystore "$KEYSTORE_NAME" -storepass "$STORE_PASS"
