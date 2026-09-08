#!/bin/bash
set -e

# ╭──────────────────────────────────────────────╮
# │ Lo que viene es común a todos los sistemas   │
# ╰──────────────────────────────────────────────╯

cd "${WORKSPACE}"
echo "Entrando al workspace desde script: ${WORKSPACE}"

REGISTRY="$1"
APPNAME="$2"
ENVIRONMENT="$3"
CONTAINER="$4"
TAG="${ENVIRONMENT}-$(date +%Y%m%d-%H%M%S)"  

echo "Registry: $REGISTRY"
echo "Aplicación: $APPNAME"
echo "Entorno: $ENVIRONMENT"
echo "Servidor remoto: $CONTAINER"
echo "Tag generado: $TAG"

echo "Copiando el tag en el version file"
echo "$TAG" > dockers/deployed-version.txt

echo "Compilando la aplicación con Maven (sin ejecutar tests)..."
docker volume create maven-repo
docker run --rm --name my-maven-elections \
  -v maven-repo:/root/.m2 \
  -v "$(pwd)":/app \
  -w /app \
  maven:3.9.9-eclipse-temurin-17-focal \
  mvn clean package -DskipTests
echo "Compilación finalizada"


echo "Haciendo login en Docker Registry"
REGISTRY_HOST="${REGISTRY%%/*}"
docker login "https://${REGISTRY_HOST}/"

echo "Construyendo imagen Docker con tag: ${TAG}"
docker build -f dockers/Dockerfile --tag "${REGISTRY}/${APPNAME}:${TAG}" .

echo "Etiquetando imagen adicional con alias de entorno: ${ENVIRONMENT}"
docker tag "${REGISTRY}/${APPNAME}:${TAG}" "${REGISTRY}/${APPNAME}:${ENVIRONMENT}"

echo "Subiendo imágenes al Registry"
docker push "${REGISTRY}/${APPNAME}:${TAG}"
docker push "${REGISTRY}/${APPNAME}:${ENVIRONMENT}"
echo "Imágenes subidas correctamente"

echo "Verificando que exista el directorio remoto para la app en el servidor"
ssh "${CONTAINER}" "mkdir -p /usr/local/properties/${APPNAME}/"

echo "Copiando archivo docker-compose.yml al servidor remoto"
scp dockers/docker-compose.yml "${CONTAINER}:/usr/local/properties/${APPNAME}/docker-compose.yml"

echo "Actualizando contenedor remoto"
ssh "${CONTAINER}" "
  set -e
  docker compose -f /usr/local/properties/${APPNAME}/docker-compose.yml pull
  docker compose -f /usr/local/properties/${APPNAME}/docker-compose.yml down
  docker compose -f /usr/local/properties/${APPNAME}/docker-compose.yml up -d
"
echo "Contenedor remoto actualizado correctamente"

# (Opcional) Forzar limpieza de contenedor previo
# echo "Eliminando contenedor anterior manualmente y levantando de nuevo"
# docker rm --force ${APPNAME}; docker compose up -d

echo "Despliegue finalizado"
