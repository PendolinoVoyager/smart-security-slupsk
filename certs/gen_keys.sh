#!/bin/bash

# Define directories and file names
CERT_DIR="./certs"
CONFIG_FILE="./openssl.cnf"
SERVER_KEY="$CERT_DIR/ass.key.pem"
SERVER_CERT="$CERT_DIR/ass.cert.pem"
CLIENT_KEY="$CERT_DIR/mqtt.key.pem"
CLIENT_CERT="$CERT_DIR/mqtt.cert.pem"
CA_CERT="$CERT_DIR/ca.cert.pem"
CA_KEY="$CERT_DIR/ca.key.pem"
DAYS_VALID=365  # Number of days the certificates will be valid
KEYSTORE="$CERT_DIR/keystore.jks"
TRUSTSTORE="$CERT_DIR/truststore.jks"
STOREPASS="changeit"

# Create certs directory if it doesn't exist
mkdir -p "$CERT_DIR"

# Function to generate CA (Certificate Authority) if it doesn't exist
generate_ca() {
  if [ ! -f "$CA_KEY" ]; then
    echo "Generating CA private key and self-signed certificate..."
    openssl genpkey -algorithm RSA -out "$CA_KEY"
    openssl req -x509 -new -nodes -key "$CA_KEY" -sha256 -days $DAYS_VALID -out "$CA_CERT" -subj "/C=US/ST=Development/L=Local/O=DevOrg/CN=Dev-CA"
  else
    echo "CA certificate and key already exist. Skipping CA generation."
  fi
}

# Function to generate a certificate and key pair
generate_certificate() {
  local keyfile=$1
  local certfile=$2
  local extensions=$3
  local subj=$4

  echo "Generating key: $keyfile"
  openssl genpkey -algorithm RSA -out "$keyfile"

  echo "Generating certificate: $certfile"
  openssl req -new -key "$keyfile" -out "$certfile.csr" -subj "$subj"

  openssl x509 -req -in "$certfile.csr" -CA "$CA_CERT" -CAkey "$CA_KEY" -CAcreateserial -out "$certfile" -days $DAYS_VALID -extfile "$CONFIG_FILE" -extensions "$extensions"

  rm "$certfile.csr"  # Remove CSR file
}

# Delete old certificates if they exist
echo "Cleaning up old certificates..."
rm -f "$SERVER_KEY" "$SERVER_CERT" "$CLIENT_KEY" "$CLIENT_CERT" "$CERT_DIR/*.csr" "$KEYSTORE" "$TRUSTSTORE"

# Generate CA certificate and key if not already present
generate_ca

# Generate server (broker) certificate
generate_certificate "$SERVER_KEY" "$SERVER_CERT" "v3_req_server" "/C=US/ST=Development/L=Local/O=DevOrg/CN=localhost"

# Generate client certificate
generate_certificate "$CLIENT_KEY" "$CLIENT_CERT" "v3_req_client" "/C=US/ST=Development/L=Local/O=DevOrg/CN=client"

# Create keystore and truststore
echo "Creating keystore and truststore..."
keytool -import -alias ca -file "$CA_CERT" -keystore "$TRUSTSTORE" -storepass "$STOREPASS" -noprompt
keytool -import -alias server -file "$SERVER_CERT" -keystore "$KEYSTORE" -storepass "$STOREPASS" -noprompt
keytool -import -alias client -file "$CLIENT_CERT" -keystore "$KEYSTORE" -storepass "$STOREPASS" -noprompt

echo "Certificates and keystores successfully generated!"
echo "Server certificate: $SERVER_CERT"
echo "Client certificate: $CLIENT_CERT"
echo "Keystore: $KEYSTORE"
echo "Truststore: $TRUSTSTORE"
