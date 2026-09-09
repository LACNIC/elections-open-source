# Sistema de Elecciones

El Sistema de Elecciones es un proyecto open source para implementar y operar procesos electorales de forma remota.

PAI es el portal/servicio externo de autenticación de LACNIC. La instalación
`fresh` usa autenticación local de la aplicación (`APP`) y no depende de PAI.

## Tecnologías

- Java 17
- Apache Wicket
- WildFly 34
- PostgreSQL
- Docker como forma vigente de instanciación

## Arranque rápido

Para evaluar el sistema desde una base vacía, sin depender de PAI (el servicio
histórico de autenticación del portal de LACNIC) ni de un servidor SMTP
externo:

Requisitos: Git (o descargar el ZIP de la rama), Docker en ejecución con
Docker Compose v2 y Bash. No es necesario instalar Java, Maven, Ruby ni Python
en el equipo; la primera compilación requiere acceso a Internet.

```bash
git clone --branch develop-v3-snapshot --single-branch https://github.com/LACNIC/elections-open-source.git
cd elections-open-source
cp dockers/.env.example dockers/.env
```

1. Preparar [`dockers/.env`](dockers/.env.example) con la copia del ejemplo anterior.
2. Cambiar las contraseñas, el usuario y correo del administrador inicial, el
   nombre de la organización y la URL pública.
3. Ejecutar `./dockers/docker-fresh.sh up`.
4. Abrir `http://localhost:8098/elections/login` e ingresar con
   `FRESH_ADMIN_USER` y `FRESH_ADMIN_PASSWORD`.
5. Consultar la documentación incluida en
   `http://localhost:8098/elections/docs/`.

Los puertos predeterminados son 8098 (aplicación), 8025 (Mailpit) y 54329
(PostgreSQL). Si están ocupados, cambie los puertos en `dockers/.env` y
ajuste `FRESH_PUBLIC_BASE_URL` al puerto o dominio elegido.
Mailpit captura los correos localmente; no los entrega a destinatarios externos.
El perfil fresh es para evaluación. Para producción configure TLS, SMTP real y
credenciales propias siguiendo la guía Docker.

`./dockers/docker-fresh.sh down` detiene la instancia y conserva los datos.
El administrador y los datos iniciales se crean solo cuando el volumen está vacío:
cambiar `FRESH_ADMIN_*` después no modifica una cuenta existente.

Este flujo crea PostgreSQL, carga el esquema y datos base, crea el primer
administrador local y captura los correos en Mailpit. Para un despliegue con
base externa, TLS, SMTP real y persistencia administrada, siga la
[`guía Docker completa`](dockers/DOCKER.md).

## Documentación

- [Documentación publicada en GitHub Pages](https://ghwww.labs.lacnic.net/elections-open-source/)
- [`docs/ReleaseNotes.md`](docs/ReleaseNotes.md)
- [`docs/compatibilidad-historica.md`](docs/compatibilidad-historica.md)
- [`postinst.md`](postinst.md)
- [`dockers/DOCKER.md`](dockers/DOCKER.md)
- [`docs/manual.html`](docs/manual.html)
- [`docs/security-access.html`](docs/security-access.html)
- [`CONTRIBUTING.md`](CONTRIBUTING.md)
- [`SECURITY.md`](SECURITY.md)

Los esquemas de referencia y las migraciones versionadas se mantienen en
[`release-files`](release-files/); el perfil Docker autocontenido consume esos
archivos para inicializar una base nueva.

## Licencia

Este repositorio se publica bajo [Licencia MIT](LICENSE).
