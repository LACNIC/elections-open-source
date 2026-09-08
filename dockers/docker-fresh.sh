#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
ENV_FILE="$ROOT_DIR/dockers/.env"
ENV_EXAMPLE="$ROOT_DIR/dockers/.env.example"
COMPOSE_FILE="$ROOT_DIR/dockers/docker-compose.fresh.yml"
ACTION="${1:-up}"

if command -v docker-compose >/dev/null 2>&1; then
  COMPOSE=(docker-compose)
else
  COMPOSE=(docker compose)
fi

if ! docker info >/dev/null 2>&1; then
  echo "Docker no esta disponible. Inicia el daemon de Docker y vuelve a intentar." >&2
  exit 1
fi

if [ ! -f "$ENV_FILE" ]; then
  cp "$ENV_EXAMPLE" "$ENV_FILE"
  echo "Se creo $ENV_FILE con credenciales exclusivas de desarrollo."
fi

compose() {
  "${COMPOSE[@]}" --env-file "$ENV_FILE" -f "$COMPOSE_FILE" "$@"
}

ensure_internal_network() {
  local INTERNAL_NETWORK="${ELECTIONS_INTERNAL_NETWORK:-$(env_value ELECTIONS_INTERNAL_NETWORK)}"
  INTERNAL_NETWORK="${INTERNAL_NETWORK:-elections-internal}"
  if ! docker network inspect "$INTERNAL_NETWORK" >/dev/null 2>&1; then
    echo "Creando red Docker interna $INTERNAL_NETWORK..."
    docker network create "$INTERNAL_NETWORK" >/dev/null
  fi
}

env_value() {
  local key="$1"
  sed -n "s/^${key}=//p" "$ENV_FILE" | tail -n 1
}

case "$ACTION" in
  up)
    compose config --quiet
    mkdir -p "$ROOT_DIR/dockers/logs-fresh"
    ensure_internal_network

    echo "Descargando las imagenes de infraestructura..."
    compose pull postgres mailpit

    app_image="$(compose config --images app | awk '/^elections:/ {print; exit}')"
    if [ -z "$app_image" ]; then
      echo "No se pudo resolver la imagen elections del Compose fresh." >&2
      exit 1
    fi
    base_image="$(env_value FRESH_BASE_IMAGE)"
    maven_image="$(env_value FRESH_MAVEN_IMAGE)"

    echo "Compilando y construyendo la aplicacion dentro de Docker..."
    docker build \
      --build-arg "BASE_IMAGE=${base_image:-quay.io/wildfly/wildfly:34.0.0.Final-jdk17}" \
      --build-arg "MAVEN_IMAGE=${maven_image:-maven:3.9.9-eclipse-temurin-17}" \
      -f "$ROOT_DIR/dockers/Dockerfile.fresh" \
      -t "$app_image" \
      "$ROOT_DIR"

    compose up -d --no-build --pull never

    app_port="${APP_HOST_PORT:-$(env_value APP_HOST_PORT)}"
    app_port="${app_port:-8098}"
    if command -v curl >/dev/null 2>&1; then
      echo "Esperando a que WildFly complete el despliegue..."
      ready=0
      for _ in $(seq 1 60); do
        http_code="$(curl --max-time 5 -s -o /dev/null -w '%{http_code}' "http://localhost:${app_port}/elections/" || true)"
        docs_code="$(curl --max-time 5 -s -o /dev/null -w '%{http_code}' "http://localhost:${app_port}/elections/documentacion/" || true)"
        if { [ "$http_code" = "200" ] || [ "$http_code" = "302" ]; } && [ "$docs_code" = "200" ]; then
          ready=1
          break
        fi
        sleep 2
      done
      if [ "$ready" != "1" ]; then
        echo "La aplicacion no quedo disponible. Ultimos logs:" >&2
        compose logs --no-color --tail 120 app >&2
        exit 1
      fi
    fi

    echo
    echo "Entorno fresh disponible:"
    echo "  Aplicacion: http://localhost:${app_port}/elections"
    echo "  Documentacion: http://localhost:${app_port}/elections/documentacion/"
    echo "  Correo:     http://localhost:$(env_value MAILPIT_WEB_PORT)"
    echo "  Login:      revisar FRESH_ADMIN_USER y FRESH_ADMIN_PASSWORD en dockers/.env"
    ;;
  reset)
    echo "Eliminando contenedores y la base de datos del entorno elections-fresh..."
    compose down -v --remove-orphans
    echo "Entorno eliminado. Ejecuta '$0 up' para crear una organizacion nueva."
    ;;
  down)
    compose down
    ;;
  logs)
    compose logs -f app postgres mailpit
    ;;
  status)
    compose ps
    ;;
  *)
    echo "Uso: $0 {up|down|reset|logs|status}" >&2
    exit 2
    ;;
esac
