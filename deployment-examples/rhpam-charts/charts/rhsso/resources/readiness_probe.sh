#!/bin/bash
set -e

DATASOURCE_POOL_TYPE="data-source"
DATASOURCE_POOL_NAME="KeycloakDS"

PASSWORD_FILE="/tmp/management-password"
PASSWORD="not set"
USERNAME="admin"
AUTH_STRING=""

if [ -d "/opt/eap/bin" ]; then
    pushd /opt/eap/bin > /dev/null
    {{- if .Values.database.xa }}
    DATASOURCE_POOL_TYPE="xa-data-source"
    {{- else }}
    DATASOURCE_POOL_TYPE="data-source"
    {{- end }}
    DATASOURCE_POOL_NAME="keycloak_{{ .Values.database.driver }}-DB"
else
    pushd /opt/jboss/keycloak/bin > /dev/null
    if [ -f "$PASSWORD_FILE" ]; then
        PASSWORD=$(cat $PASSWORD_FILE)
    else
        PASSWORD=$(cat /dev/urandom | tr -dc 'a-zA-Z0-9' | fold -w 32 | head -n 1)
        ./add-user.sh -u $USERNAME -p $PASSWORD> /dev/null
        echo $PASSWORD > $PASSWORD_FILE
    fi
    AUTH_STRING="--digest -u $USERNAME:$PASSWORD"
fi

curl -s --max-time 10 --fail http://localhost:9990/management $AUTH_STRING --header "Content-Type: application/json" -d "{\"operation\":\"test-connection-in-pool\", \"address\":[\"subsystem\",\"datasources\",\"${DATASOURCE_POOL_TYPE}\",\"${DATASOURCE_POOL_NAME}\"], \"json.pretty\":1}"
curl -s --max-time 10 --fail http://$(hostname -i):8080/auth > /dev/null
