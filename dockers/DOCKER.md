# Guia Docker del Sistema de Elecciones

La forma vigente de instanciar el Sistema de Elecciones es mediante Docker. Esta carpeta contiene la definicion de imagen, Compose, variables de ejemplo y scripts auxiliares para ejecucion local y despliegue remoto.

## Archivos

- `Dockerfile`: construye la imagen de la aplicacion sobre WildFly 34 con JDK 17.
- `docker-compose.yml`: define el contenedor productivo `elections`, variables, puerto publicado y volumen de logs.
- `docker-compose.local.yml`: conecta la aplicacion local a PostgreSQL y Mailpit mediante la red Docker compartida `elections-internal`.
- `.env.example`: ejemplo de variables de ambiente para test/desarrollo. Copielo a `.env` y complete los valores reales.
- `.env`: archivo local con variables reales. No debe versionarse.
- `docker-local.sh`: construye la imagen `dev` y recrea el contenedor para desarrollo local.
- `Dockerfile.fresh`: build multi-stage que compila Maven dentro de Docker y usa la imagen oficial de WildFly.
- `docker-compose.fresh.yml`: ambiente autocontenido con aplicacion, PostgreSQL y Mailpit.
- `docker-fresh.sh`: crea, opera y reinicia desde cero el ambiente autocontenido.
- `docker-jenkins.sh`: script de build y despliegue remoto usado por el flujo de Jenkins.
- `deployed-version.txt`: version/tag copiado dentro de la configuracion de WildFly durante el build.

## Imagen

El `Dockerfile` acepta `BASE_IMAGE` y, para los builds remotos, parte por defecto de:

```dockerfile
registry.example.org/wildfly/wildfly:34.0.0.Final-jdk17
```

El helper local usa la imagen WildFly disponible en el equipo con la etiqueta
`local-registry/wildfly:34.0.0.Final-jdk17`. Puede elegir otra imagen local
compatible mediante `LOCAL_BASE_IMAGE`.

La imagen copia:

- `elections-ejb/target/elections-ejb-1.0.jar`
- `elections-admin-web/target/elections.war`
- `elections-services/target/elections-ws.war`
- `wildfly/deployments/pai-auth-ws-client-1.5.1.jar`
- `wildfly/modules/`
- `wildfly/configuration/standalone.xml`
- `wildfly/configuration/pai.properties`
- `dockers/deployed-version.txt`

PAI es el servicio historico de autenticacion del portal de LACNIC. Al iniciar,
el contenedor reescribe `pai.properties` con `URL_PORTAL_WS` y `PORTAL_APIKEY`
tomados del ambiente y ejecuta WildFly con `standalone.sh -b=0.0.0.0`. Las
organizaciones que usan autenticacion local con `WS_AUTH_METHOD=APP` no necesitan
conectarse a PAI.

## Variables de ambiente

`docker-compose.yml` carga variables desde `.env`. Revise y reemplace todos los valores antes de ejecutar una instancia.

Variables usadas por la configuracion actual:

- `VERSION`: version asignada a la imagen de la aplicacion.
- `LOCAL_BASE_IMAGE`: imagen WildFly ya presente en Docker que usa el build local. Por defecto `local-registry/wildfly:34.0.0.Final-jdk17`.
- `JAVA_OPTS`: opciones de memoria/runtime para Java.
- `URL_PORTAL_WS`: URL del servicio de autenticacion PAI del portal de LACNIC.
- `PORTAL_APIKEY`: clave de acceso para esa integracion con PAI.
- `DB_ELECTIONS_HOST`: host PostgreSQL.
- `DB_ELECTIONS_PORT`: puerto PostgreSQL.
- `DB_ELECTIONS_NAME`: nombre de la base de datos.
- `DB_ELECTIONS_USER`: usuario de base.
- `DB_ELECTIONS_PASSWORD`: password de base.
- `DB_ELECTIONS_MIN_POOL_SIZE`: minimo del pool del datasource.
- `DB_ELECTIONS_MAX_POOL_SIZE`: maximo del pool del datasource.

Un punto de partida seguro esta en [`dockers/.env.example`](.env.example).

El datasource configurado en WildFly es `java:jboss/datasources/elections-ds`.

## Entorno de evaluacion autocontenido

El flujo `fresh` permite probar el sistema como una organizacion nueva sin una
base PostgreSQL, autenticacion PAI, CAPTCHA ni servidor SMTP preexistentes. Es
un ambiente de evaluacion y desarrollo que tambien sirve como referencia del
proceso de inicializacion. No debe exponerse directamente a Internet ni
reemplaza, sin endurecimiento, el Compose de despliegue: publica PostgreSQL,
usa Mailpit y trae credenciales de ejemplo.

Requisitos:

- Docker en ejecucion con `docker compose` o `docker-compose`.
- Acceso de red para descargar las imagenes fijadas de PostgreSQL, Mailpit,
  Maven y WildFly.
- Bash y Docker Compose v2. El helper fresh no requiere Python, Java, Maven ni Ruby instalados en el host; la compilación y la generación de documentación se realizan dentro de Docker.
- `curl` es recomendable para que el helper espere y valide el despliegue.

Antes del primer arranque, prepare la configuracion local:

```bash
cp dockers/.env.example dockers/.env
```

Como minimo, reemplace estos valores en `dockers/.env`:

- `POSTGRES_SUPERUSER_PASSWORD`, `ELECTIONS_DB_USER` y
  `ELECTIONS_DB_PASSWORD`.
- `FRESH_ORGANIZATION_NAME` y `FRESH_PUBLIC_BASE_URL`.
- `FRESH_PUBLIC_NOMINATION_ENABLED`: use `true` para habilitar el formulario
  publico de nominacion para cualquier tipo de eleccion o `false` para
  deshabilitarlo.
- `FRESH_ADMIN_USER`, `FRESH_ADMIN_PASSWORD` y `FRESH_ADMIN_EMAIL`.
- `FRESH_DEFAULT_SENDER`.
- `FRESH_EMAIL_HOST`, `FRESH_EMAIL_USER` y `FRESH_EMAIL_PASSWORD` si se desea
  usar un SMTP distinto del Mailpit incluido.

El entorno `fresh` usa por defecto autenticacion local (`WS_AUTH_METHOD=APP`) y no
requiere configuracion PAI. Ademas crea `LOGIN_CAPTCHA_ENABLED=false` y
`AI_TEXT_IMPROVEMENT_ENABLED=false`, por lo que CAPTCHA e IA quedan
deshabilitados de forma explicita. El archivo `.env` contiene secretos locales
y no debe versionarse.

`FRESH_PUBLIC_BASE_URL` debe contener la URL base del servidor, sin agregar el
contexto `/elections`; la aplicacion incorpora ese contexto al generar links.

El bootstrap tambien configura `PUBLIC_ELECTION_LEGACY_MAX_ELECTION_ID=0` para
que las elecciones creadas desde cero no se oculten como elecciones historicas
en el listado publico, y copia `FRESH_PUBLIC_NOMINATION_ENABLED` al parametro de base
`PUBLIC_NOMINATION_ENABLED`. El bootstrap solo corre al crear el volumen de
PostgreSQL; en un entorno ya inicializado, cambie el parametro desde la
administracion o recree el entorno con `docker-fresh.sh reset`.

Desde la raiz del repositorio:

```bash
./dockers/docker-fresh.sh up
```

El helper crea la red Docker externa local `elections-internal`. PostgreSQL,
Mailpit y la aplicacion se unen a esa red y se resuelven por los nombres de
servicio `postgres` y `mailpit`. El puerto SMTP `1025` permanece interno; solo
la interfaz web de Mailpit se publica en el host.

En el primer arranque el script:

1. copia `.env.example` a `.env` si el archivo local no existe;
2. descarga las imagenes fijadas de PostgreSQL, Mailpit, Maven y WildFly;
3. compila los tres modulos Maven dentro de Docker;
4. construye la imagen local `registry.example.org/apps/elections:dev`;
5. crea una base vacia y carga el esquema, parametros y templates de
   `release-files/ref`;
6. crea la personalizacion de la organizacion y el administrador definidos en
   `.env`;
7. levanta la aplicacion y espera a que responda por HTTP.

Servicios publicados por defecto:

- aplicacion: `http://localhost:8098/elections`;
- bandeja de correo Mailpit: `http://localhost:8025`;
- PostgreSQL para diagnostico local: `localhost:54329`.

La autenticacion administrativa usa `WS_AUTH_METHOD` desde `elections.properties`.
Con `APP`, las credenciales
iniciales salen de `FRESH_ADMIN_USER` y `FRESH_ADMIN_PASSWORD`. Con los valores
SMTP predeterminados, los correos no salen a Internet: se entregan a Mailpit y
se inspeccionan en su UI. El bootstrap guarda `FRESH_EMAIL_HOST`,
`FRESH_EMAIL_USER` y `FRESH_EMAIL_PASSWORD` como los parametros `EMAIL_HOST`,
`EMAIL_USER` y `EMAIL_PASSWORD` de la aplicacion.

### Primer login y administradores locales

El bootstrap de `fresh` configura `WS_AUTH_METHOD=APP` y conserva una fila en la
tabla `useradmin`. La contrasena configurada en `FRESH_ADMIN_PASSWORD` se
almacena como un hash SHA-256 en mayusculas; el texto original no se guarda en
la base.

Abra:

```text
http://localhost:8098/elections/login
```

Ingrese con `FRESH_ADMIN_USER` y `FRESH_ADMIN_PASSWORD`. El campo TOTP puede
quedar vacio en modo `APP`; actualmente solo interviene en el login externo.
Tras ingresar, el menu `Administradores` permite:

- crear otros usuarios locales;
- cambiar la contrasena de cualquier administrador;
- actualizar sus correos;
- retirar cuentas que ya no correspondan.

Todos los usuarios locales autenticados reciben actualmente los permisos
`elections-manager` y `elections-deleter`, es decir, acceso administrativo
completo. No existe recuperacion autoservicio de contrasena ni integracion
generica OIDC, SAML o LDAP. Debe conservarse al menos una segunda cuenta
administrativa: si se pierde la unica cuenta, otro operador con acceso a la
base debe aprovisionar o restablecer una cuenta siguiendo el procedimiento de
base externa documentado mas abajo.

Comandos operativos:

```bash
./dockers/docker-fresh.sh status
./dockers/docker-fresh.sh logs
./dockers/docker-fresh.sh down
```

Para eliminar la base, los contenedores y recrear otra organizacion desde
cero:

```bash
./dockers/docker-fresh.sh reset
./dockers/docker-fresh.sh up
```

`reset` elimina solamente los volúmenes pertenecientes al proyecto Compose
`elections-fresh`. Antes del siguiente `up` se pueden cambiar en `.env`
el nombre institucional, URL publica, puertos, credenciales y administrador.
Los scripts de `/docker-entrypoint-initdb.d` solo vuelven a ejecutarse cuando el
volumen PostgreSQL esta vacio.

## Base de datos

El contenedor no crea ni migra la base. Antes de levantarlo, la base PostgreSQL debe existir, tener el esquema esperado y ser accesible desde el contenedor con las variables `DB_ELECTIONS_*`.

Los archivos `release-files/ref/elections_schema_new.sql`,
`release-files/ref/parameter_NEW.sql` y
`release-files/ref/electionemailtemplate_NEW.sql` preparan una base nueva, pero
no crean un administrador. Para instalaciones independientes, verifique que el
variable `WS_AUTH_METHOD` del `.env` valga `APP` y cree el primer administrador
antes de intentar entrar. El procedimiento completo y parametrizado esta en
`docs/manual.html`, seccion **Preparar base de datos**.

El Compose normal conserva su comportamiento: el modo de login se lee de la
tabla `parameter` de su propia base y no depende de la configuracion de
`fresh`.

Para bases nuevas o migraciones desde versiones anteriores, use la documentacion versionada del sitio:

- `docs/manual.html`: instanciacion con Docker y preparacion de base nueva.
- `docs/update.html`: actualizacion desde versiones anteriores.

## Ejecucion con Compose

Para ejecutar una imagen ya construida:

```bash
cd dockers
mkdir -p logs
VERSION=dev docker compose up -d --no-build --pull never
```

Para desarrollo local use `./dockers/docker-local.sh`, que selecciona
`docker-compose.local.yml`, monta `fresh/email.properties` y ejecuta WildFly con
el UID compatible con la imagen. El archivo `docker-compose.yml` se conserva
como entrada productiva y no incorpora dependencias locales.

Para validar solamente la recreacion y readiness con una imagen ya construida,
pueden definirse `SKIP_MAVEN_BUILD=1` y `SKIP_DOCKER_BUILD=1`.

La configuracion actual:

- Usa la imagen `registry.example.org/apps/elections:${VERSION}`.
- Publica `8098:8080`.
- Monta logs en `./logs:/opt/jboss/wildfly/standalone/log`.
- Ejecuta el contenedor con usuario `10000:10000`.
- Usa `TZ=America/Montevideo`.
- Reinicia el contenedor con politica `always`.

La aplicacion queda disponible en:

```text
http://localhost:8098/elections
```

La documentación correspondiente a la misma versión queda publicada sin autenticación en:

```text
http://localhost:8098/elections/docs/
```

Los Dockerfiles compilan `docs/` con `_config.yml` y `_config.docker.yml`,
que fija la ruta interna `/elections/docs`. GitHub Pages usa solamente
`docs/_config.yml` y publica en
`https://ghwww.labs.lacnic.net/elections-open-source/`, desde `main` y `/docs`.

## Build manual

Desde la raiz del repositorio:

```bash
docker build -f dockers/Dockerfile -t registry.example.org/apps/elections:<tag> .
docker push registry.example.org/apps/elections:<tag>
```

Luego configure `VERSION=<tag>` en el `.env` usado por Compose.

## Helper local

### Provision unica de la base local

El arranque normal nunca consulta registries. Antes del primer uso en una
maquina nueva, un operador con acceso al registry debe provisionar la base una
sola vez:

```bash
docker pull registry.example.org/wildfly/wildfly:34.0.0.Final-jdk17
docker tag \
  registry.example.org/wildfly/wildfly:34.0.0.Final-jdk17 \
  local-registry/wildfly:34.0.0.Final-jdk17
```

Esta provision es una operacion explicita y separada. No forma parte de
`docker-local.sh`. Si la etiqueta local no existe, el script falla antes de
compilar y explica como corregirlo; nunca intenta descargarla automaticamente.

Desde la raiz del repositorio:

```bash
./dockers/docker-local.sh
```

El script:

- Usa `VERSION=dev`.
- exige la base `lacnic-local/wildfly:34.0.0.Final-jdk17` o la indicada por `LOCAL_BASE_IMAGE`;
- compila los artefactos Maven y construye localmente `registry.example.org/apps/elections:dev`;
- usa temporalmente el builder del daemon con `DOCKER_BUILDKIT=0`,
  `--pull=false` y pasa la base local al Dockerfile;
- no hace login, pull ni push;
- Elimina el contenedor local anterior llamado `elections`, si existe.
- ejecuta Compose con `--no-build --pull never`;
- abre `http://localhost:8098` salvo que se use `OPEN_BROWSER=0`.

El nombre completo de la imagen de aplicacion se conserva por compatibilidad
con Compose y Jenkins, pero etiquetarla localmente no produce trafico de red.
Antes de usar el helper, confirme que `dockers/.env` contiene los valores
correctos del ambiente.

El builder clasico esta deprecado por Docker, pero actualmente es necesario
para garantizar que `FROM` consuma la etiqueta del daemon sin intentar resolver
su nombre contra Docker Hub. La migracion futura debe usar una base en layout
OCI local compatible con BuildKit y conservar la misma garantia offline.

## Despliegue remoto

`docker-jenkins.sh` espera cuatro argumentos:

```bash
./dockers/docker-jenkins.sh <registry> <appname> <environment> <usuario@servidor>
```

Ejemplo:

```bash
export WORKSPACE=/ruta/al/repositorio
./dockers/docker-jenkins.sh registry.example.org/apps elections test usuario@servidor
```

El script:

- Entra al directorio definido por `WORKSPACE`.
- Genera un tag con formato `<environment>-YYYYMMDD-HHMMSS`.
- Escribe ese tag en `dockers/deployed-version.txt`.
- Compila con Maven dentro del contenedor `maven:3.9.9-eclipse-temurin-17-focal`.
- Construye la imagen Docker.
- Publica dos tags: el tag timestamp y el alias del ambiente.
- Copia `dockers/docker-compose.yml` a `/usr/local/properties/<appname>/docker-compose.yml` en el servidor remoto.
- Ejecuta `docker compose pull`, `down` y `up -d` en el servidor remoto.

Antes del primer despliegue remoto, prepare en el servidor:

```bash
mkdir -p /usr/local/properties/elections/logs
```

Tambien debe existir un `.env` del ambiente junto al `docker-compose.yml` remoto, con las variables necesarias y secretos reales fuera del repositorio.

## Verificaciones posteriores

Despues de levantar o desplegar:

- Verifique que el contenedor `elections` este corriendo.
- Revise los logs de WildFly.
- Confirme que no haya errores de datasource `elections-ds`.
- Acceda a `/elections`.
- Acceda a `/elections/login` con la cuenta inicial.
- Si usa `APP`, confirme que aparece el menu `Administradores`, cree una segunda
  cuenta y pruebe su acceso antes de retirar cualquier usuario de bootstrap.
- Verifique el listado de elecciones contra la base configurada.

## Notas operativas

- No use los valores de `.env` como configuracion productiva sin revisarlos.
- No versionar secretos reales.
- El flujo Docker no reemplaza los scripts de migracion de base; las migraciones
  versionadas se mantienen en `release-files/` y se ejecutan aparte cuando se
  actualiza una instalacion existente.
