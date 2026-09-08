# Contributing

Gracias por ayudar a mejorar el proyecto.

## Antes de abrir un cambio

- Trabaja en una rama propia.
- Revisa que no se agreguen secretos, credenciales, dumps ni archivos locales.
- Mantén los cambios enfocados y con un mensaje claro.

## Recomendaciones

- Para cambios de backend, valida que `mvn clean package -DskipTests` siga funcionando.
- Para cambios de Docker o despliegue, prueba el flujo de `dockers/docker-local.sh` o al menos revisa `dockers/DOCKER.md`.
- Si el cambio toca documentación, actualiza la guía correspondiente en `doc/` o `dockers/`.

## Estilo

- Prefiere cambios pequeños y fáciles de revisar.
- No versiones artefactos generados ni configuración real de ambiente.
- Si una dependencia nueva trae binarios o licencias especiales, documéntalo en la PR.
