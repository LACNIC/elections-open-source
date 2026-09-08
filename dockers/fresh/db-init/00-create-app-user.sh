#!/usr/bin/env sh

set -eu

: "${ELECTIONS_DB_USER:?ELECTIONS_DB_USER es requerido}"
: "${ELECTIONS_DB_PASSWORD:?ELECTIONS_DB_PASSWORD es requerido}"

psql -v ON_ERROR_STOP=1 \
  --username "$POSTGRES_USER" \
  --dbname "$POSTGRES_DB" \
  --set=app_user="$ELECTIONS_DB_USER" \
  --set=app_password="$ELECTIONS_DB_PASSWORD" <<'SQL'
CREATE ROLE :"app_user" LOGIN PASSWORD :'app_password';
SQL
