#!/usr/bin/env bash

set -e

VERSION="${VERSION:-dev}"
DOCKERFILE_PATH="${DOCKERFILE_PATH:-dockers/Dockerfile}"
DOCKER_COMPOSE_FILE="${DOCKER_COMPOSE_FILE:-dockers/docker-compose.local.yml}"
BUILD_CONTEXT="${BUILD_CONTEXT:-.}"
COMPOSE_SERVICE="${COMPOSE_SERVICE:-app}"
APP_PATH="${APP_PATH:-/elections}"
OPEN_BROWSER="${OPEN_BROWSER:-1}"
INTERNAL_NETWORK="${ELECTIONS_INTERNAL_NETWORK:-elections-internal}"

if command -v docker-compose >/dev/null 2>&1; then
  COMPOSE=(docker-compose)
else
  COMPOSE=(docker compose)
fi

compose_config() {
  VERSION="$VERSION" "${COMPOSE[@]}" -f "$DOCKER_COMPOSE_FILE" config "$@"
}

if [ ! -f "$DOCKER_COMPOSE_FILE" ]; then
  echo "No se encontro el archivo $DOCKER_COMPOSE_FILE. Abortando."
  exit 1
fi

if [ ! -f "$DOCKERFILE_PATH" ]; then
  echo "No se encontro el archivo $DOCKERFILE_PATH. Abortando."
  exit 1
fi

if [ -z "${LOCAL_BASE_IMAGE:-}" ]; then
  BASE_IMAGE_TAG="$(awk -F'wildfly:' '/^ARG BASE_IMAGE=.*wildfly:/ { print $NF; exit }' "$DOCKERFILE_PATH")"
  if [ -z "$BASE_IMAGE_TAG" ]; then
    echo "No se pudo deducir la imagen base local desde ARG BASE_IMAGE en $DOCKERFILE_PATH."
    exit 1
  fi
  LOCAL_BASE_IMAGE="lacnic-local/wildfly:$BASE_IMAGE_TAG"
fi

IMAGE_NAME="$(compose_config --format json | python3 -c '
import json, sys
service = sys.argv[1]
data = json.load(sys.stdin)
try:
    print(data["services"][service]["image"])
except KeyError:
    sys.exit(f"No se encontro image para el servicio {service}.")
' "$COMPOSE_SERVICE")"

CONTAINER_NAME="$(compose_config --format json | python3 -c '
import json, sys
service = sys.argv[1]
data = json.load(sys.stdin)
svc = data.get("services", {}).get(service, {})
project = data.get("name", "app")
print(svc.get("container_name") or f"{project}-{service}-1")
' "$COMPOSE_SERVICE")"

HOST_PORT="$(compose_config --format json | python3 -c '
import json, sys
service = sys.argv[1]
data = json.load(sys.stdin)
ports = data.get("services", {}).get(service, {}).get("ports", [])
for port in ports:
    published = port.get("published")
    if published:
        print(published)
        break
' "$COMPOSE_SERVICE")"

if [ -n "$HOST_PORT" ]; then
  URL="http://localhost:${HOST_PORT}${APP_PATH}"
else
  URL=""
fi

mkdir -p "$(dirname "$DOCKER_COMPOSE_FILE")/logs"

if ! docker network inspect "$INTERNAL_NETWORK" >/dev/null 2>&1; then
  echo "Creando red Docker interna $INTERNAL_NETWORK..."
  docker network create "$INTERNAL_NETWORK" >/dev/null
fi

if ! docker image inspect "$LOCAL_BASE_IMAGE" >/dev/null 2>&1; then
  cat >&2 <<EOF
No existe la imagen base local requerida: $LOCAL_BASE_IMAGE
El arranque local no consulta registries. Provisiona esta etiqueta una unica vez
siguiendo dockers/DOCKER.md o define LOCAL_BASE_IMAGE con otra imagen ya local.
EOF
  exit 1
fi

if [ "${SKIP_MAVEN_BUILD:-0}" != "1" ]; then
  echo "Compilando artefactos Maven..."
  mvn clean package -DskipTests
else
  echo "Build Maven omitido por SKIP_MAVEN_BUILD=1."
fi

if [ "${SKIP_DOCKER_BUILD:-0}" != "1" ]; then
  echo "Construyendo imagen local $IMAGE_NAME..."
  # BuildKit intenta resolver incluso las etiquetas locales de FROM contra un
  # registry. El builder del daemon respeta estrictamente LOCAL_BASE_IMAGE y
  # permite que este flujo permanezca offline.
  DOCKER_BUILDKIT=0 docker build \
    --pull=false \
    --build-arg "BASE_IMAGE=$LOCAL_BASE_IMAGE" \
    -f "$DOCKERFILE_PATH" \
    -t "$IMAGE_NAME" \
    "$BUILD_CONTEXT"
else
  echo "Build Docker omitido por SKIP_DOCKER_BUILD=1."
fi

echo "Eliminando contenedor anterior si existe..."
docker rm -f "$CONTAINER_NAME" 2>/dev/null || true

echo "Levantando contenedor con Docker Compose sin pull del registry..."
VERSION="$VERSION" "${COMPOSE[@]}" -f "$DOCKER_COMPOSE_FILE" up -d --no-build --pull never "$COMPOSE_SERVICE"

echo "Contenedor '$CONTAINER_NAME' corriendo."

if [ -n "$URL" ] && command -v curl >/dev/null 2>&1; then
  echo "Esperando readiness en $URL..."
  READY=0
  for _ in $(seq 1 60); do
    HTTP_CODE="$(curl -s -o /dev/null -w '%{http_code}' "$URL" || true)"
    if [ "$HTTP_CODE" = "200" ] || [ "$HTTP_CODE" = "302" ]; then
      READY=1
      break
    fi
    sleep 2
  done
  if [ "$READY" != "1" ]; then
    echo "La aplicacion no quedo disponible. Ultimos logs:" >&2
    docker logs --tail 120 "$CONTAINER_NAME" >&2
    exit 1
  fi
fi

if [ "$OPEN_BROWSER" = "1" ] && [ -n "$URL" ]; then
  echo "Abriendo navegador en $URL"
  if which open >/dev/null; then
    open "$URL"
  elif which xdg-open >/dev/null; then
    xdg-open "$URL"
  else
    echo "Abrir manualmente: $URL"
  fi
elif [ -n "$URL" ]; then
  echo "URL local: $URL"
fi
