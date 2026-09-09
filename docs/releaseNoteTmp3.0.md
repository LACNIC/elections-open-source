# Bitácora de documentación y distribución v3.0

Este archivo conserva la trazabilidad del relevamiento y de las actualizaciones documentales de `v3.0`. No es una guía de instalación ni sustituye las [release notes consolidadas](ReleaseNotes.md). Se mantiene el nombre de archivo por compatibilidad con referencias existentes.

## 2026-09-09 - Unificación de rutas de documentación

- Se actualizan las referencias a la carpeta `docs/` en Docker, exclusiones, README, guías y ejemplos del reporte de auditoría.
- GitHub Pages usa `docs/_config.yml` y publica en `https://ghwww.labs.lacnic.net/elections-open-source/` desde `main` y `/docs`.
- Docker agrega `docs/_config.docker.yml` para servir la documentación en `/elections/docs/`. El menú de la aplicación, el filtro Wicket y la comprobación de arranque usan esa ruta.
- Los enlaces de código fuente y licencia apuntan al repositorio `LACNIC/elections-open-source`.
- Las referencias de archivos y rutas del registro histórico se normalizan a los nombres actuales. Se conservan los nombres de ramas históricas y las fechas de sus verificaciones.

## 2026-09-08 - Estado de la distribución y limpieza de Markdown

### Cambios de instalación ya verificados

- La rama de distribución es `develop-v3-snapshot` de `LACNIC/elections-open-source`.
- El commit `f982533` recuperó archivos necesarios para compilar y desplegar: POMs, descriptores web y de WildFly, módulos y otros recursos omitidos durante la importación.
- El flujo Docker fresh valida Compose, respeta la red y el puerto configurados, espera a PostgreSQL por TCP y comprueba la aplicación y la documentación. No requiere Python en el host.
- README y manual apuntan al repositorio de distribución e indican crear `.env` a partir del ejemplo, sustituir credenciales y configurar la organización. Mailpit captura correo de evaluación; la entrega externa requiere SMTP real.
- La prueba previa desde una exportación limpia completó la compilación Maven, inicializó una base vacía y permitió el login administrativo. La aplicación y `/elections/docs/` respondieron correctamente. Estas son verificaciones del cambio de instalación, no una nueva ejecución en esta edición documental.

### Limpieza documental actual

- Se elimina el borrador Markdown de configuración inicial. Su preparación de capturas y publicación queda cancelada; se consultan [instalación](manual.html), [seguridad](security-access.html), [parámetros](parameters-templates-i18n.html) y [Docker](https://github.com/LACNIC/elections-open-source/blob/main/dockers/DOCKER.md).
- Se elimina la nota de convenciones de visibilidad de los ejemplos HTML que contenía rutas personales.
- Se retira también el PDF base v3.0 de configuración inicial, que era un documento de trabajo sin capturas. Se actualiza la [matriz documental](MatrizActualizacionDocumentacion.md) para reflejar el retiro del borrador y del PDF y remitir a las guías vigentes.
- La eliminación de archivos del árbol actual no elimina sus versiones anteriores ni los metadatos de autoría de Git. No se ha reescrito el historial.

### Lectura del registro histórico

Las entradas siguientes describen el estado en sus fechas originales. Las tareas antiguas de completar o publicar el borrador quedan sustituidas por su retiro. Las instrucciones históricas sobre Ansible no son operativas; Docker y `release-files/` son las referencias vigentes. Las release notes v3.0 ya están consolidadas, aunque las entradas iniciales describan su preparación.

## 2026-09-04 - Retiro del restore Ansible legado

- Se elimina `ansible/` y la guia `docs/ansible.html` de la distribucion publica.
- El contenido de `ansible/db/update.sql` fue contrastado con
  `release-files/3.0/v3.0_script.sql` y con las referencias `*_NEW.sql`; sus
  cambios de esquema, parametros y templates ya estan representados en la
  cadena canonica.
- Docker queda como mecanismo soportado de instanciacion y `release-files/`
  como fuente de esquema, datos base y migraciones.
- Las menciones posteriores de esta bitacora describen el estado historico
  previo a este retiro y no constituyen instrucciones operativas vigentes.

## 2026-04-20 - Relevamiento de guia de actualizacion

### Fuentes revisadas

- `docs/MatrizActualizacionDocumentacion.md`
- `docs/ReleaseNotes.md`
- `README.md`
- `.github/copilot-instructions.md`
- `docs/update.html`
- `release-files/2.3.1/v2.3.1_script.sql`
- `release-files/2.4/v2.4_ddl_script.sql`
- `release-files/2.4/v2.4_data_script.sql`
- `release-files/3.0/v3.0_script.sql`
- `release-files/validate_new_release_chain.py`
- `pom.xml`
- `elections-ejb/pom.xml`
- `elections-admin-web/pom.xml`
- `elections-services/pom.xml`
- `dockers/Dockerfile`
- `dockers/docker-compose.yml`
- `wildfly/configuration/standalone.xml`

### Hallazgos verificados

- La actualizacion de base hacia `v3.0` queda consolidada en `release-files/3.0/v3.0_script.sql` para instalaciones que ya esten en `v2.4`.
- Para una instalacion historica en `v2.3`, el orden documentado para llegar a `v3.0` es: `v2.3.1_script.sql`, `v2.4_ddl_script.sql`, `v2.4_data_script.sql` y `v3.0_script.sql`.
- El script `v3.0` rellena `election.election_type` segun `category` cuando esta nulo: `MODERATORS` queda como `MODERATORS`, `OTHER` queda como `OTHER` y el resto queda como `BOARD`.
- El script `v3.0` rellena `uservoter.orgid` con `mail` cuando `orgid` esta vacio y `mail` tiene valor.
- Antes de crear indices unicos, el script `v3.0` aborta si hay `organization.orgid` duplicados por eleccion usando `upper(btrim(orgid))`.
- Antes de crear indices unicos, el script `v3.0` aborta si quedan `uservoter.orgid` vacios o duplicados por eleccion usando `upper(btrim(orgid))`.
- El script `v3.0` crea los indices `uq_organization_election_orgid_norm` y `uq_uservoter_election_orgid_norm`.
- El script `v3.0` agrega `election.authorized_user_emails`, `election.authorized_support_emails`, `election.authorized_nominate_emails`, campos de convocatoria `callspanish`, `callenglish`, `callportuguese`, `callset` y `election.public_link_recovery_mode`.
- El valor inicial de `public_link_recovery_mode` queda en `ONLY_BR` para `BOARD`, `ELECTORAL_COMMISSION` y `FISCAL_COMMISSION`; queda en `ALL` para `IANA`, `MODERATORS`, `ASO` y `OTHER`; y queda en `NONE` para otros tipos.
- El script `v3.0` reemplaza el comentario unico de `auditorcandidatedecision` por campos separados de predecision y decision final.
- El script `v3.0` agrega parametros base para revision de texto con IA: `AI_TEXT_PROMPT_WRITING_STYLE_REVIEW`, `AI_TEXT_PROMPT_SPELLING_REVIEW` y `AI_TEXT_PROMPT_TRANSLATION`.
- El script `v3.0` agrega `sync_run.sync_type` y `sync_run.deleted_rows`, derivando valores iniciales desde `sync_run.message`.
- `release-files/validate_new_release_chain.py` valida en PostgreSQL temporal que `elections_schema_new`, `parameter_NEW` y `electionemailtemplate_NEW` cierran contra `elections_schema_old` mas `v2.4` y `v3.0`; tambien valida una fixture legacy con migracion de tipos de eleccion, ORGID de votantes, calendarios, tareas, candidatos, auditores y templates por eleccion.
- El runtime actual verificado en README, POM y Docker es Java 17 y WildFly 34.
- El `Dockerfile` usa `wildfly:34.0.0.Final-jdk17` y copia `elections-ejb/target/elections-ejb-1.0.jar`, `elections-admin-web/target/elections.war`, `elections-services/target/elections-ws.war`, `wildfly/modules` y `wildfly/configuration/standalone.xml`.
- El datasource actual usa `java:jboss/datasources/elections-ds` y variables `DB_ELECTIONS_HOST`, `DB_ELECTIONS_PORT`, `DB_ELECTIONS_NAME`, `DB_ELECTIONS_MIN_POOL_SIZE`, `DB_ELECTIONS_MAX_POOL_SIZE`, `DB_ELECTIONS_USER` y `DB_ELECTIONS_PASSWORD`.

### Pendiente para release notes finales

- Contrastar en codigo y UI el alcance funcional de la recuperacion de links publicos antes de redactarla como funcionalidad.
- Contrastar en codigo y UI el uso real de los prompts IA antes de presentarlo como funcionalidad de usuario.
- Revisar flujos de auditoria para describir correctamente la separacion entre predecision y decision final.
- Revisar flujos de sincronizacion para documentar `sync_type` y `deleted_rows` con comportamiento funcional, no solo estructura de base.

## 2026-04-20 - Relevamiento de instanciacion Docker y alcance de Ansible

### Fuentes revisadas

- Contexto operativo confirmado por el equipo: Docker es el mecanismo vigente para instanciar el sistema; Ansible se usa en test para reiniciar/restaurar la base y no en produccion.
- `docs/manual.html`
- `docs/index.html`
- `docs/ansible.html`
- `README.md`
- `pom.xml`
- `elections-ejb/pom.xml`
- `elections-admin-web/pom.xml`
- `elections-services/pom.xml`
- `dockers/Dockerfile`
- `dockers/docker-compose.yml`
- `dockers/.env`
- `dockers/docker-local.sh`
- `dockers/docker-jenkins.sh`
- `wildfly/configuration/standalone.xml`
- `wildfly/configuration/pai.properties`
- `ansible/restore-db.yaml`
- `ansible/hosts`
- `ansible/group_vars/dbdev`
- `ansible/db/actualizar_dump.sh`
- `release-files/ref/elections_schema_new.sql`
- `release-files/ref/parameter_NEW.sql`
- `release-files/ref/electionemailtemplate_NEW.sql`

### Hallazgos verificados

- La documentacion legacy de instalacion manual indicaba Java 8, WildFly 20, PostgreSQL 12 y rutas `ansible/roles/...` que ya no existen en la estructura actual.
- El runtime actual confirmado por POMs y Docker es Java 17 sobre WildFly 34.
- La imagen Docker copia artefactos Maven generados, `wildfly/modules`, `wildfly/configuration/standalone.xml`, `wildfly/configuration/pai.properties` y `dockers/deployed-version.txt`.
- `dockers/docker-compose.yml` publica `8098:8080`, usa una imagen de aplicación parametrizada por `VERSION`, carga variables desde `.env`, monta logs y ejecuta el contenedor como usuario `10000:10000`.
- `dockers/docker-jenkins.sh` compila con `maven:3.9.9-eclipse-temurin-17-focal`, construye y sube imagen, copia `docker-compose.yml` al servidor remoto y reinicia el contenedor con Docker Compose.
- La carpeta `ansible` actual contiene inventario `dbdev`, variables de base de test y `restore-db.yaml`, que borra, recrea y restaura la base desde `ansible/db/dump.zip` mas `ansible/db/update.sql`.
- Para una base nueva, los SQL versionados disponibles como referencia actual son `release-files/ref/elections_schema_new.sql`, `release-files/ref/parameter_NEW.sql` y `release-files/ref/electionemailtemplate_NEW.sql`.
- Al cargar `electionemailtemplate_NEW.sql` sobre una base nueva, la secuencia `electionemailtemplate_seq` debe realinearse contra el maximo `electionemailtemplate_id`, porque el esquema define la secuencia con inicio `500` y el archivo de templates incluye IDs mayores.

### Pendiente

- Revisar los parametros base cargados por `parameter_NEW.sql` para separar claramente valores de ejemplo/test de valores que cada ambiente debe configurar.
- Confirmar si `dockers/DOCKER.md` se mantiene como guia operativa o si debe reescribirse, porque parte del texto parece plantilla generica y no documentacion final del Sistema de Elecciones.

## 2026-04-20 - Reescritura de guia Ansible

### Fuentes revisadas

- `ansible/restore-db.yaml`
- `ansible/hosts`
- `ansible/group_vars/dbdev`
- `ansible/db/actualizar_dump.sh`
- `ansible/db/update.sql`
- `ansible/ansible.cfg`
- `docs/ansible.html`
- `docs/index.html`

### Hallazgos verificados

- `ansible/hosts` define el grupo `dbdev` con un host de test configurable por cada organización.
- `ansible/group_vars/dbdev` define variables de conexion y restore para una base de test.
- `ansible/restore-db.yaml` es destructivo: termina conexiones activas, borra la base configurada, la crea nuevamente, restaura `dump.sql`, aplica `update.sql` y otorga permisos al usuario configurado.
- El dump fuente del restore es `ansible/db/dump.zip`; el playbook lo descomprime en `/home/jenkins/dumps/` en el host remoto.
- `ansible/db/update.sql` existe como paso adicional posterior al dump, pero actualmente no contiene sentencias.
- `ansible/db/actualizar_dump.sh` genera un dump remoto desde el host configurado, lo descarga, reemplaza `dump.zip`, mueve el zip anterior a `dump-old.zip` y limpia archivos SQL temporales.

### Pendiente

- Validar el build de Jekyll cuando el entorno Ruby local tenga disponibles Bundler/Jekyll y herramientas para compilar extensiones nativas.

## 2026-04-20 - Reescritura de guia Docker operativa

### Fuentes revisadas

- `dockers/DOCKER.md`
- `dockers/Dockerfile`
- `dockers/docker-compose.yml`
- `dockers/.env`
- `dockers/docker-local.sh`
- `dockers/docker-jenkins.sh`
- `dockers/deployed-version.txt`
- `wildfly/configuration/standalone.xml`
- `wildfly/configuration/pai.properties`
- Salida de `docker compose -f dockers/docker-compose.yml config --no-interpolate`

### Hallazgos verificados

- La guia previa `dockers/DOCKER.md` era una plantilla generica y no describia correctamente el Sistema de Elecciones.
- La imagen vigente parte de una base WildFly parametrizada en `dockers/Dockerfile`.
- El `Dockerfile` copia los artefactos `elections-ejb-1.0.jar`, `elections.war`, `elections-ws.war`, el cliente `pai-auth-ws-client-1.5.1.jar`, `wildfly/modules`, `standalone.xml`, `pai.properties` y `dockers/deployed-version.txt`.
- El contenedor reescribe `pai.properties` al iniciar con `URL_PORTAL_WS` y `PORTAL_APIKEY` tomados del ambiente.
- `docker-compose.yml` usa la imagen de aplicación parametrizada por `VERSION`, publica `8098:8080`, define el contenedor `elections`, carga `./.env`, monta `./logs` en los logs de WildFly, ejecuta como usuario `10000:10000` y usa politica `restart: always`.
- `docker compose -f dockers/docker-compose.yml config --no-interpolate` confirma que `./.env` y `./logs` se resuelven bajo la carpeta `dockers`.
- `docker-local.sh` usa `VERSION=dev`, construye la imagen para `linux/arm64`, hace push al registry, elimina un contenedor local previo llamado `elections`, levanta Compose y abre `http://localhost:8098`.
- `docker-jenkins.sh` requiere `WORKSPACE` y los argumentos `<registry> <appname> <environment> <usuario@servidor>`; genera un tag timestamp, compila con `maven:3.9.9-eclipse-temurin-17-focal`, construye y publica tags Docker, copia `docker-compose.yml` al servidor remoto y reinicia con `docker compose pull/down/up -d`.
- El despliegue Docker no crea ni migra la base de datos; la base PostgreSQL debe existir y estar alineada con los scripts/versiones documentados.
- Ansible no forma parte del flujo productivo de despliegue; queda limitado al restore de base de test ya documentado.

### Pendiente

- Confirmar con el flujo Jenkins real si hay convenciones adicionales de servidores, nombres de ambiente o manejo de secretos que deban documentarse fuera de los scripts actuales.

## 2026-04-20 - Reescritura de guia de servicios REST

### Fuentes revisadas

- `docs/services.html`
- `elections-services/pom.xml`
- `elections-services/src/main/java/net/lacnic/elections/ws/app/ElectionsServicesApplication.java`
- `elections-services/src/main/java/net/lacnic/elections/ws/services/ElectionsService.java`
- `elections-services/src/main/java/net/lacnic/elections/ws/services/ElectionsTablesServices.java`
- `elections-services/src/main/java/net/lacnic/elections/ws/auth/WebServiceAuthentication.java`
- `elections-services/src/main/java/net/lacnic/elections/ws/services/util/PagingUtil.java`
- `elections-services/src/test/java/net/lacnic/elections/ws/auth/WebServiceAuthenticationTest.java`
- `elections-services/src/test/java/net/lacnic/elections/ws/services/ElectionsServiceTest.java`
- DTOs y reports en `elections-ejb/src/main/java/net/lacnic/elections/data`
- DTOs y reports en `elections-ejb/src/main/java/net/lacnic/elections/domain/services`
- `release-files/ref/parameter_NEW.sql`

### Hallazgos verificados

- El artefacto del módulo de servicios es `elections-ws.war`; el contexto esperado es `/elections-ws` y la aplicación JAX-RS usa `@ApplicationPath("/")`.
- Los recursos registrados son `ElectionsService`, `ElectionsTablesServices` y `JacksonConfigurationProvider`.
- Los servicios funcionales y de tablas usan `WebServiceAuthentication.authenticate`; en modo centralizado requieren rol `api-Elections`.
- Los endpoints de snapshot público `v2` usan `WebServiceAuthentication.authenticatePublicInformation`; en modo centralizado requieren rol `api-ElectionsPublicInformation`.
- En modo `APP`, la autenticación compara el header `Authorization` con `WS_AUTH_TOKEN` y luego valida la IP contra `WS_AUTHORIZED_IPS`.
- La IP cliente se toma de `X-FORWARDED-FOR` si existe; si no, se usa `request.getRemoteAddr()`.
- Los listados paginados requieren `/{pageSize}/{offset}`; `pageSize` debe ser mayor a cero, no superar `WS_MAX_PAGE_SIZE`, y `offset` representa número de página.
- La referencia nueva de parámetros carga `WS_MAX_PAGE_SIZE=50`.
- Siguen existiendo endpoints legacy `old/participations/{org}` y `old/participaciones/{org}`, además de los endpoints actuales `participations/{org}` y `participaciones/{org}`.
- Existen endpoints de snapshot público: `/v2/elections`, `/v2/elections/{id}/public-snapshot/core`, `/v2/elections/{id}/public-snapshot/roll` y `/v2/elections/{id}/public-snapshot/photos`.
- Los listados de tablas paginados actuales son `activities`, `auditors`, `candidates`, `commissioners`, `elections`, `electionemailtemplates`, `emails`, `emailshistory`, `ipaccesses`, `jointelections`, `useradmins`, `uservoters` y `votes`.
- Los listados de tablas no paginados actuales son `customizations` y `parameters`.
- `tables/parameter/{id}` devuelve la entidad `Parameter`, incluido su `value`; debe tratarse como endpoint sensible aunque esté protegido por autenticación.
- `UserAdminTableReport` tiene `password` anotado con `@JsonIgnore`, por lo que no se serializa en la respuesta del detalle de usuario admin.
- Métodos `HEAD`, `PUT`, `POST` y `DELETE` caen en un handler que responde `200` con `Not a valid operation.` y no ejecuta operaciones de negocio.

### Pendiente

- Generar ejemplos JSON reales o fixtures de respuesta sólo si se levantan datos controlados o tests dedicados; no inventar payloads.
- Revisar el comportamiento funcional completo de los snapshots públicos `v2` cuando se aborde la documentación de experiencia pública, candidatos, preguntas, padrón y resultados.

## 2026-04-20 - Ajuste de README

### Fuentes revisadas

- `README.md`
- `dockers/DOCKER.md`
- `docs/manual.html`
- `docs/ansible.html`
- `dockers/Dockerfile`
- `dockers/docker-compose.yml`
- Contexto operativo confirmado por el equipo sobre Docker y Ansible

### Hallazgos verificados

- El README ya declaraba Java 17 y WildFly 34 como runtime actual.
- El README no explicitaba que Docker es la forma vigente de instanciar el sistema.
- El README no aclaraba que la carpeta `ansible` no se usa para producción y queda limitada a restore/reinicio de base de test.

### Pendiente

- Mantener el README como entrada breve; el detalle operativo debe seguir en `docs/manual.html` y `dockers/DOCKER.md`.

## 2026-04-21 - Documentacion de organizaciones

### Fuentes revisadas

- `docs/MatrizActualizacionDocumentacion.md`
- `docs/elections.html`
- `docs/census.html`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/app/ElectionsManagerApp.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/admin/election/organizations/ElectionOrganizationsDashboard.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/admin/election/organizations/ElectionOrganizationsDashboard.html`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/admin/election/organizations/AddOrganizationPanel.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/admin/election/organizations/AddOrganizationPanel.html`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/admin/election/organizations/OrganizationsListPanel.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/admin/election/organizations/UploadOrganizationsDebtorsFilePanel.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/admin/election/organizations/UploadOrganizationsDebtorsFilePanel.html`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/admin/election/organizations/AutomaticOrganizationsSyncActionsPanel.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/admin/election/organizations/OrganizationsAdvancedActionsPanel.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/validators/OrganizationDeleteActionValidator.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/validators/OrganizationBulkDeleteActionValidator.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/validators/OrganizationExcelFileValidator.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/validators/OrganizationValidationRules.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/app/ElectionsManagerApp.properties.xml`
- `elections-ejb/src/main/java/net/lacnic/elections/domain/pre/Organization.java`
- `elections-ejb/src/main/java/net/lacnic/elections/utils/ExcelUtils.java`
- `elections-ejb/src/main/java/net/lacnic/elections/ejb/impl/ElectionsManagerEJBBean.java`

### Hallazgos verificados

- La pantalla administrativa de organizaciones esta montada en `/admin/election/organizations`.
- En modo manual, el alta individual no obliga `ORGID` en la UI; si llega vacio, el backend genera un UUID, lo normaliza y lo usa como identidad estable de la organizacion.
- La carga `alta/actualizacion` por Excel requiere `ORGID`, `NAME`, `VOTES`, `CATEGORY`, `COUNTRY`, `MEMBERSHIPCONTACTID`, `MEMBERSHIPCONTACTNAME`, `MEMBERSHIPCONTACTEMAIL` y `MEMBERSHIPCONTACTLANGUAGE`; `CNPJ` y `ASN` son opcionales.
- La carga `alta/actualizacion` no es snapshot por defecto: sin `overwriteAll` solo crea/actualiza. Marcando `overwriteAll`, elimina las organizaciones existentes que no vengan en el Excel.
- Esa carga conserva los tokens de nominacion existentes y no modifica el estado `deudor`.
- El archivo de deudores usa una sola columna `ORGID`. Sin `overwriteAll`, solo marca `deudor=true` para las organizaciones listadas; con `overwriteAll`, primero limpia todos los deudores actuales y luego aplica el listado nuevo.
- El borrado por Excel y la baja individual se bloquean si alguna organizacion tiene nominaciones o apoyos.
- El sync automatico completo desde external sync crea nuevas organizaciones, actualiza campos operativos y vuelve `member=true` a las que reaparecen en el payload, pero no elimina filas ausentes: para las ausentes baja `member=false`.
- El sync completo no eleva automaticamente `deudor=true` en organizaciones existentes cuando external sync las reporta como deudoras; para ese caso existe una accion avanzada separada de espejo de deudores.
- La accion avanzada `Actualizar estado de deudor desde external sync` solo actualiza `deudor`, no crea ni elimina organizaciones, y solo esta disponible en gestion automatica dentro de la ventana `N_23`.
- El padron automatico usa como fuente inmediata la lista local de organizaciones; solo genera votantes para organizaciones `member=true`, `deudor=false` y con datos de contacto/votos completos.

### Pendiente

- Relevar candidaturas, nominaciones y apoyos para documentar como consumen `member`, `deudor`, `doNominationToken` y support links.
- Relevar experiencia publica para documentar el uso final de los links de nominacion y apoyo fuera del admin.

## 2026-04-21 - Documentacion de resultados y reportes

### Fuentes revisadas

- `docs/MatrizActualizacionDocumentacion.md`
- `docs/voting-public-links.html`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/app/ElectionsManagerApp.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/commons/AdminNavBarPanel.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/admin/election/ElectionsListPanel.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/admin/election/view/ViewElectionPanel.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/admin/election/configuration/ElectionConfigurationDashboard.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/admin/election/charts/StatsDashboard.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/admin/election/charts/StatsDashboard.html`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/admin/election/charts/VotersGraphPanel.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/admin/election/charts/ChartLines.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/admin/election/prereports/PreElectionReportsDashboard.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/admin/election/prereports/PreElectionReportsPanel.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/admin/election/prereports/PreElectionReportsPanel.html`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/results/ElectionResultsPanel.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/results/ElectionResultsPanel.html`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/results/CandidateCodesPanel.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/results/CandidateCodesPanel.html`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/results/PublicResultsPanel.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/results/PublicResultsPanel.html`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/results/audit/MoreInformationForAuditPanel.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/results/audit/MoreInformationForAuditPanel.html`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/token/page/ResultPublicPage.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/token/page/AuditPublicResultsPage.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/token/page/AuditPublicDashboardPage.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/results/review/ReviewDashboard.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/app/ElectionsManagerApp.properties.xml`
- `elections-ejb/src/main/java/net/lacnic/elections/dao/VoteDao.java`
- `elections-ejb/src/main/java/net/lacnic/elections/dao/CandidateDao.java`
- `elections-ejb/src/main/java/net/lacnic/elections/data/ElectionsResultsData.java`
- `elections-ejb/src/main/java/net/lacnic/elections/data/ResultDetailData.java`
- `elections-ejb/src/main/java/net/lacnic/elections/domain/Election.java`
- `elections-ejb/src/main/java/net/lacnic/elections/utils/VotingPeriodResolver.java`
- `elections-ejb/src/main/java/net/lacnic/elections/ejb/impl/ElectionsManagerEJBBean.java`
- `elections-ejb/src/main/java/net/lacnic/elections/ejb/impl/ElectionsVoterEJBBean.java`
- `docs/auditReport-v2.3.pdf`

### Hallazgos verificados

- La administracion expone resultados internos en `/stats`, pre-reportes en `/admin/election/pre-reports`, resultados publicos en `/token/result` y auditoria final en `/token/audit/result`.
- El dashboard interno de resultados no usa snapshots persistidos: consulta candidatos y votos vivos al renderizar.
- La grafica de evolucion se construye con `UserVoter` que ya votaron, por fecha, por lo que mide participacion de votantes y acumulado de participantes; no total de filas `Vote`.
- La grafica depende de `https://www.gstatic.com/charts/loader.js`.
- La tabla de resultados resume `totalVoters` como cantidad de votantes que emitieron voto y `totalVotes` como cantidad de filas `Vote`, por lo que pueden diferir cuando hay voto ponderado.
- La tabla de codigos lista una fila por voto y ordena por `v.code`.
- El pre-reporte es una vista HTML operativa con acciones y recordatorios; no es un export ni un documento cerrado.
- El pre-reporte consolida disponibilidad de links publicos, nominaciones aceptadas, nominaciones pendientes, apoyos, progreso por tareas y matriz de decisiones de auditores.
- `/token/result` exige `resultLinkAvailable=true` y calendario `N_17_SINGLE_PROVISIONAL_RESULTS_PUBLISHED`, pero no tiene fecha de cierre publica propia una vez abierto.
- `/token/audit/result` exige `auditorLinkAvailable=true` y ventana `N_18_PERIODO_CE_AUDIT` con inicio y fin.
- El bloque adicional de auditoria agrupa por `voteAmount`, pero la columna visual `Votos (1 - max)` hoy queda en `?` fijo en el template.
- `docs/auditReport-v2.3.pdf` sigue siendo solo el documento historico de auditoria de la version 2.3; no corresponde describirlo como reporte funcional nuevo de v3.0.

### Pendiente

- Relevar el bloque transversal de parametros, templates e i18n que afecta textos, recordatorios, switches y pantallas publicas de resultados.
- Dejar para release notes finales solo cambios funcionales verificados; no convertir el PDF de auditoria historica en item funcional de `v3.0`.

## 2026-04-21 - Documentacion de candidaturas, nominaciones y apoyos

### Fuentes revisadas

- `docs/MatrizActualizacionDocumentacion.md`
- `docs/elections.html`
- `docs/calendar-tasks.html`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/app/ElectionsManagerApp.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/admin/election/candidates/ElectionCandidatesDashboard.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/admin/election/candidates/CandidatesListPanel.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/admin/election/candidates/CandidatesHeaderActionsPanel.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/admin/election/candidates/AddCandidatePanel.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/admin/election/candidates/EditCandidateDashboard.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/admin/election/candidates/ManageCandidateStatusDashboard.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/admin/election/candidates/ManageCandidateTrainingDashboard.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/admin/election/candidates/ManageCandidateTranslationsDashboard.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/admin/election/candidates/ManageCandidateBiographyDashboard.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/admin/election/candidates/ViewCandidateAnswersDashboard.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/token/page/DoNominationPage.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/token/NominationFormPanel.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/token/page/AcceptNominationConditionsPage.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/token/page/GenericAcceptNominationTasksPage.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/token/page/SupportNominationPage.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/token/taskpanels/GenericNominationOrgSupportsManagementPanel.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/token/taskpanels/GenericNominationUserSupportsManagementPanel.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/validators/AuthorizedSupportContactEmailValidator.java`
- `elections-ejb/src/main/java/net/lacnic/elections/domain/Candidate.java`
- `elections-ejb/src/main/java/net/lacnic/elections/domain/CandidateType.java`
- `elections-ejb/src/main/java/net/lacnic/elections/domain/pre/CandidateStatus.java`
- `elections-ejb/src/main/java/net/lacnic/elections/domain/pre/Nomination.java`
- `elections-ejb/src/main/java/net/lacnic/elections/domain/pre/NominationStatus.java`
- `elections-ejb/src/main/java/net/lacnic/elections/domain/pre/SupportNomination.java`
- `elections-ejb/src/main/java/net/lacnic/elections/domain/pre/SupportStatus.java`
- `elections-ejb/src/main/java/net/lacnic/elections/dao/CandidateDao.java`
- `elections-ejb/src/main/java/net/lacnic/elections/ejb/impl/ElectionsManagerEJBBean.java`
- `elections-ejb/src/main/java/net/lacnic/elections/ejb/impl/ElectionsPreNominationEJBBean.java`

### Hallazgos verificados

- El modulo administrativo de candidatos esta montado en `/admin/election/candidates` y en el wizard queda entre `Tareas` y `Auditores`.
- El alta manual de candidato crea tipo `NORMAL`, orden no fijo siguiente y filas de progreso `NOT_STARTED` para todas las tareas actuales de la eleccion.
- El formulario inline de alta/edicion exige `name`, `mail`, `reminderFrequency` y `bioSpanish` para candidatos normales; `linkSpanish` y `linkedinUrl` son opcionales. Si no se sube foto al crear, se aplica la foto default.
- La UI inline de alta fuerza `onlySp=true` y al guardar ejecuta `copyBioToOtherLanguages()`, sobrescribiendo bio/link EN/PT y respuestas abiertas traducidas con el contenido en espanol.
- La opcion de abstencion no nace desde nominacion: se agrega manualmente desde cabecera, con estado `CONFIRMED_AND_PUBLISHED`, `ReminderFrequency.DISABLED`, foto default propia, texto default propio y `candidateOrder = MAX_ORDER`.
- La pantalla de estado de candidato expone todos los enums de `CandidateStatus` y el backend `updateCandidateStatus()` no valida transiciones de negocio entre estados. La advertencia por falta de aprobacion total de auditores es solo informativa.
- Solo los candidatos `CONFIRMED_AND_PUBLISHED` entran en la boleta publica.
- La pagina `/token/organization/do-nomination` bloquea nuevas nominaciones cuando la organizacion ya tiene alguna en `PROPOSED`, `ACCEPTED_BY_CANDIDATE` o `APPROVED`.
- La nominacion desde organizacion requiere `doNominationLinkAvailable=true` y ventana abierta `N_2_PERIODO_CALL_FOR_CANDIDATES`.
- Aceptar una nominacion crea un candidato nuevo con nombre/email de la nominacion, foto default, `onlySp=true`, estado `INCOMPLETE`, estados de capacitacion/evaluacion en `PENDING` y progreso inicial `NOT_STARTED` para todas las tareas de la eleccion.
- El backend bloquea la aceptacion si ya existe otra nominacion aceptada con el mismo email normalizado dentro de la misma eleccion.
- Rechazar una nominacion mueve la nominacion a `REJECTED_BY_CANDIDATE` y transforma apoyos pendientes `PROPOSED` en `REJECTED`.
- Los apoyos de organizacion se limitan a dos activos por nominacion; una organizacion no puede apoyarse a si misma, no puede ser pedida dos veces para la misma nominacion y no puede haber otorgado ya apoyo a otra nominacion de esa misma eleccion.
- Los apoyos por contacto manual solo aplican a `USER_SUPPORTS_2` y `USER_SUPPORTS_5`; se bloquean emails duplicados dentro de la nominacion y el email del propio candidato.
- Si la eleccion tiene `authorizedSupportEmails`, la UI del flujo de apoyo manual solo acepta emails incluidos en esa lista.
- Al alcanzar los umbrales de apoyos aceptados/aprobados, el backend intenta autocompletar `ORG_SUPPORTS`, `USER_SUPPORTS_2` y `USER_SUPPORTS_5`.
- El flujo publico de apoyo solo permite `ACCEPTED` o `REJECTED`, aunque el modelo tambien contempla `APPROVED` e `INVALID` para etapas posteriores.
- La pagina administrativa de capacitacion puede marcar tareas `COURSE` y `EVALUATION` como completadas cuando deja sus estados en `COMPLETED` o `NOT_APPLICABLE`.
- La vista administrativa de respuestas consolida datos de candidato, nominacion, apoyos, progreso de tareas, paises, organizaciones y decisiones de auditoria.

### Pendiente

- Relevar en bloque separado el detalle funcional de preguntas estatutarias/no estatutarias, declaraciones y preguntas de comunidad.
- Relevar experiencia publica completa para documentar como se presentan candidaturas, apoyos aceptados y estados de nominacion fuera del admin.

## 2026-04-20 - Inventario de documentacion preexistente pendiente

### Fuentes revisadas

- `find docs -maxdepth 3 -type f`
- `find . -maxdepth 3 -type f \( -iname '*.md' -o -iname '*.html' -o -iname '*.pdf' \)`
- `find .github -maxdepth 3 -type f`
- `postinst.md`
- `.github/copilot-instructions.md`
- `README.md`
- `docs/MatrizActualizacionDocumentacion.md`

### Hallazgos verificados

- El arbol de documentacion contiene soporte Jekyll y assets que no son contenido funcional directo: `docs/Gemfile`, `docs/Gemfile.lock`, `_layouts`, `_includes`, `_sass`, CSS, JS e imagenes de marca.
- `postinst.md` es documentacion preexistente y esta linkeado desde `README.md`, pero no estaba en la matriz.
- `postinst.md` documenta un reverse proxy nginx hacia `localhost:8080` y redirige a `/elecciones/`; esto no coincide con la documentacion Docker vigente, que publica `8098:8080` y usa el contexto `/elections`.
- `.github/copilot-instructions.md` no es documentacion de usuario final, pero contiene reglas operativas que deben considerarse en code review, arquitectura, i18n, validaciones Wicket, manejo de parametros y versionado de assets.
- La matriz ya tenía pendientes los PDFs históricos de auditoría y configuración inicial, además de `docs/404.html` y soporte Jekyll. El PDF de auditoría fue versionado posteriormente como `docs/auditReport-v2.3.pdf`. El manual inicial de 2021 fue retirado; consulte [la guía vigente](manual.html).

### Pendiente

- Actualizar `postinst.md` antes de crear documentacion funcional nueva, porque es un documento preexistente enlazado desde README y puede guiar mal una instalacion actual.
- Revisar los PDFs preexistentes para decidir si se reemplazan por documentos editables o se marcan como legado.

## 2026-04-20 - Actualizacion de postinst

### Fuentes revisadas

- `postinst.md`
- `dockers/docker-compose.yml`
- `dockers/DOCKER.md`
- `docs/manual.html`
- `elections-admin-web/pom.xml`
- `elections-services/pom.xml`
- `elections-services/src/main/java/net/lacnic/elections/ws/app/ElectionsServicesApplication.java`

### Hallazgos verificados

- La guia anterior asumía que la aplicacion escuchaba directo en `localhost:8080` y redirigia a `/elecciones/`.
- El Compose vigente publica el contenedor `elections` como `8098:8080`.
- El WAR web se publica como `elections.war`, por lo que el contexto de administracion y flujos web es `/elections`.
- El WAR de servicios se publica como `elections-ws.war`; la aplicacion JAX-RS usa `@ApplicationPath("/")`, por lo que los servicios quedan bajo `/elections-ws`.
- `WebServiceAuthentication` usa `X-FORWARDED-FOR` si esta presente; la configuracion de reverse proxy debe preservar ese header para que la validacion por IP use la IP real del cliente.
- La carpeta `ansible` no participa de la post-instalacion productiva.

### Pendiente

- Validar la configuracion final de Nginx contra un servidor real si se definen convenciones productivas especificas de la organizacion para dominios, logs o hardening TLS.

## 2026-04-20 - Relevamiento de PDF de auditoria y configuracion inicial

### Fuentes revisadas

- Manual histórico de configuración de 2021, revisado en esa fecha y posteriormente retirado. Para instalar la versión actual, consulte [el manual vigente](manual.html) y [la guía Docker](https://github.com/LACNIC/elections-open-source/blob/main/dockers/DOCKER.md).
- `docs/auditReport-v2.3.pdf`
- `release-files/2.3/v2.3_script.sql`
- `release-files/ref/parameter_NEW.sql`
- `release-files/validate_old_release_chain.py`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/app/ElectionsManagerApp.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/app/ElectionsWebAdminSession.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/login/LoginPanel.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/commons/AdminNavBarPanel.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/admin/parameter/*`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/admin/customization/*`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/home/PublicHomeDashboard.java`
- `elections-ejb/src/main/java/net/lacnic/elections/utils/Constants.java`
- `elections-ejb/src/main/java/net/lacnic/elections/utils/LinksUtils.java`
- `elections-ejb/src/main/java/net/lacnic/elections/ejb/commons/impl/ElectionsParametersEJBBean.java`
- `elections-ejb/src/main/java/net/lacnic/elections/dao/CustomizationDao.java`

### Hallazgos verificados

- El manual histórico revisado correspondía a septiembre de 2021. Sus instrucciones fueron reemplazadas por las guías actuales de [instalación](manual.html), [seguridad](security-access.html) y [parámetros](parameters-templates-i18n.html).
- El PDF legacy indicaba acceso por `/elections/login`; la ruta sigue vigente porque `ElectionsManagerApp` monta `/login` dentro del WAR `elections.war`.
- En Docker local, el Compose vigente publica `8098:8080`, por lo que el acceso directo local esperado es `http://localhost:8098/elections/login`.
- El PDF legacy indicaba credenciales `admin/admin`; en `release-files/ref` no se encontro seed de usuario `useradmin`, por lo que el borrador no las documenta como vigentes salvo que la base entregada las incluya.
- `WS_AUTH_METHOD` controla tanto el login administrativo como la autenticacion de servicios: `APP` usa usuarios locales y el modo centralizado usa autenticacion externa.
- El menu `Administradores` solo se muestra cuando el login fue local (`WS_AUTH_METHOD=APP`).
- El menu de parametros actual esta agrupado como `Avanzadas > Sistema > Parametros`.
- El menu de personalizacion actual esta agrupado como `Avanzadas > Elecciones > Personalizacion`.
- `URL` se usa como base para componer links y el codigo agrega el contexto `elections`; no debe cargarse con `/elections` duplicado salvo despliegue especifico que lo requiera.
- El comportamiento actual de CAPTCHA no depende de `APP`: requiere `LOGIN_CAPTCHA_ENABLED=true`, claves de reCAPTCHA y superar el umbral de intentos fallidos por IP cuando corresponde al login.
- La personalizacion actual requiere el registro `customization_id=1`, permite editar tres imagenes, titulos y HTML del home, y alterna entre home personalizado y home por defecto mediante `show_home`.
- El reporte de auditoria se renombro a `docs/auditReport-v2.3.pdf` y las referencias `AUDIT_REPORT_LINK` pasan a indicar explicitamente que corresponde a la version auditada `v2.3`.

### Pendiente

- Pendiente histórico cancelado el 2026-09-08: completar las capturas del borrador de configuración inicial, posteriormente retirado.
- Confirmar con la base de entrega si existe usuario bootstrap y cual es el procedimiento oficial para rotarlo.
- Decisión actualizada el 2026-09-08: retirar el borrador Markdown y consultar las guías vigentes; no publicarlo como manual final.

### Verificacion

- `git diff --check` OK.
- `python3 release-files/validate_new_release_chain.py` OK.

## 2026-04-21 - Enriquecimiento adicional de ReleaseNotes desde la rama doc

### Fuentes revisadas

- `git log --oneline -- docs README.md postinst.md dockers/DOCKER.md .github/copilot-instructions.md`
- `docs/organizations.html`
- `docs/census.html`
- `docs/questions-declarations.html`
- `docs/auditors.html`
- `docs/voting-public-links.html`
- `docs/results-reports.html`
- `docs/ReleaseNotes.md`

### Hallazgos verificados

- La rama `doc` ya tenía documentados detalles que valía la pena subir a `ReleaseNotes.md` sin convertirlo en una guía operativa: sincronización manual/automática de organizaciones y padrón con external sync, padrón público estatutario, recuperación pública de links por email, pre-reportes operativos HTML, códigos de verificación por voto y revisión coordinada por auditores comisionados.
- Esos puntos ya estaban contrastados en las guías funcionales y eran consistentes con la entrada `v3.0` existente, por lo que se usaron para enriquecer el resumen sin inventar comportamiento nuevo.

### Verificacion

- `git diff --check` OK.

## 2026-04-21 - Actualizacion de 404 del sitio de documentacion

### Fuentes revisadas

- `docs/404.html`
- `docs/_layouts/default.html`
- `docs/_includes/header.html`
- `docs/_includes/footer.html`
- `docs/index.html`
- `docs/manual.html`

### Hallazgos verificados

- La pagina `404` anterior no usaba `layout: default`; duplicaba manualmente el HTML base del sitio.
- El documento anterior dejaba el `<title>` vacio, incluia texto en ingles (`Page not found`) y tenia enlaces del header sin destino valido.
- La documentacion vigente del sitio ya no presenta Ansible como mecanismo de despliegue productivo y debe derivar a las guias activas del sitio.
- La `404` puede resolverse sin tocar navegacion ni estructura global del sitio; alcanza con reutilizar el layout comun y ofrecer enlaces utiles a la documentacion actual.

### Verificacion

- `git diff --check` OK.
- No aplica build Jekyll en este bloque porque no se modifico navegacion ni estructura del sitio.

## 2026-04-21 - Saneamiento del soporte Jekyll del sitio

### Fuentes revisadas

- `docs/_config.yml`
- `docs/_includes/header.html`
- `docs/_includes/footer.html`
- `docs/index.html`
- `docs/manual.html`
- `docs/update.html`
- `docs/services.html`
- `docs/ansible.html`
- `docs/404.html`
- `docs/Gemfile`
- `docs/Gemfile.lock`
- `docs/.ruby-version`
- `README.md`
- `https://pages.github.com/versions/`
- `https://docs.github.com/en/pages/setting-up-a-github-pages-site-with-jekyll/creating-a-github-pages-site-with-jekyll`

### Hallazgos verificados

- La dirección vigente de la documentación es `https://ghwww.labs.lacnic.net/elections-open-source/`, con `baseurl: "/elections-open-source"` y publicación desde `main` y `/docs`.
- Las páginas principales del sitio seguían arrastrando `link_logo`, `link_title` y `link_repo` hardcodeados hacia una URL de demostración, aunque la documentación vigente ya no debe depender de ese demo.
- `docs/_config.yml` ahora centraliza la URL del repositorio en `site.repository_url`, evitando repetir el link GitHub en cada página.
- `docs/_includes/header.html` ahora deriva siempre al home de la documentación con `site.baseurl`, en vez de depender de variables por página.
- `docs/_includes/footer.html` ya no deja el año fijo en `2020`; usa `site.time`.
- `docs/index.html`, `docs/manual.html`, `docs/update.html`, `docs/services.html`, `docs/ansible.html` y `docs/404.html` quedaron sin front matter repetido de links obsoletos.
- Los enlaces al repositorio en `index.html`, `update.html`, `services.html` y `404.html` quedaron unificados sobre `site.repository_url`.
- El sitio quedo alineado al bundle soportado por GitHub Pages usando `github-pages ~> 232` en `docs/Gemfile`, en lugar de fijar `jekyll` manualmente.
- El lockfile se regenero limpio y ahora refleja la cadena soportada por GitHub Pages, incluyendo `github-pages 232`, `jekyll 3.10.0`, `jekyll-seo-tag 2.8.0` y `jekyll-sitemap 1.4.0`.
- Se agrego `docs/.ruby-version` con `3.3.4` para dejar explicito el runtime Ruby publicado por GitHub Pages.

### Verificacion

- `git diff --check` OK.
- `PATH="/opt/homebrew/opt/ruby@3.3/bin:$PATH" bundle install` OK.
- `PATH="/opt/homebrew/opt/ruby@3.3/bin:$PATH" bundle exec jekyll build --source . --destination /tmp/elections-doc-site` OK.
- El comando historico con `--disable-disk-cache` ya no aplica, porque `jekyll 3.10.0` no soporta ese flag.
- El build deja warnings no bloqueantes de `GitHub Metadata` sin autenticacion API y de `faraday-retry`, pero genera el sitio correctamente.

### Pendiente

- Evaluar luego si conviene documentar en una guia operativa el `PATH` local de `ruby@3.3` para quienes editen el sitio fuera de Docker.

## 2026-04-21 - Revisión de `.github/copilot-instructions.md`

### Fuentes revisadas

- `.github/copilot-instructions.md`
- `elections-services/src/main/java/net/lacnic/elections/ws/services/ElectionsService.java`
- `elections-services/src/main/java/net/lacnic/elections/ws/auth/LacnicAuthResponse.java`
- `elections-ejb/src/main/java/net/lacnic/elections/cron/ElectionScheduler.java`
- búsquedas de patrones de comentarios y versionado de assets en el repositorio

### Hallazgos verificados

- `.github/copilot-instructions.md` no es documentación funcional ni operativa del producto; actúa como guía interna para generación asistida de código y code review.
- Las reglas de arquitectura e i18n siguen siendo consistentes con el sistema actual: separación DAO/EJB, uso de `wicket:message`, validaciones Wicket, versionado de assets y parámetros con fallback.
- El repositorio sí contiene comentarios puntuales y útiles en zonas técnicas concretas, por ejemplo en `ElectionScheduler`, `ElectionsService` y `LacnicAuthResponse`, por lo que una prohibición absoluta de comentarios no reflejaba el uso real del código.
- Se ajustó la regla para desalentar comentarios redundantes, pero permitir comentarios breves cuando agregan contexto no obvio.
- También se dejó explícito en el documento que su alcance es de contribución/review y que no reemplaza la documentación funcional ni operativa del sistema.

### Verificacion

- `git diff --check` OK.

## 2026-04-21 - Guia funcional de alta y configuracion base de elecciones

### Fuentes revisadas

- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/admin/election/create/ElectionCreateDashboard.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/admin/election/create/ElectionCreatePanel.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/admin/election/create/ElectionCreatePanel.html`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/admin/election/detail/ElectionDetailDashboard.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/admin/election/detail/ElectionDetailPanel.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/admin/election/detail/ElectionDetailPanel.html`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/admin/election/configuration/ElectionConfigurationDashboard.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/admin/election/configuration/ElectionConfigurationDashboard.html`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/admin/election/ManageElectionTabsPanel.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/admin/election/ElectionsListPanel.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/admin/election/view/ViewElectionPanel.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/app/SecurityUtils.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/validators/AuthorizedUserEmailsValidator.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/validators/AuthorizedSupportContactEmailValidator.java`
- `elections-ejb/src/main/java/net/lacnic/elections/domain/Election.java`
- `elections-ejb/src/main/java/net/lacnic/elections/domain/ElectionCategory.java`
- `elections-ejb/src/main/java/net/lacnic/elections/domain/ElectionType.java`
- `elections-ejb/src/main/java/net/lacnic/elections/domain/ElectionLinkRecoveryMode.java`
- `elections-ejb/src/main/java/net/lacnic/elections/domain/pre/ElectionPresetConfigurations.java`
- `elections-ejb/src/main/java/net/lacnic/elections/ejb/impl/ElectionsManagerEJBBean.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/app/ElectionsManagerApp.properties.xml`

### Hallazgos verificados

- Una eleccion nueva se crea desde `ElectionCreateDashboard`, pero el alta real se canaliza por `updateElection`; cuando `election_id == 0`, el EJB persiste la eleccion, crea templates de email, agrega commissioners como auditores y aplica el preset del tipo de eleccion.
- La entidad `Election` arranca por defecto con `onlySp=true`, `electionType=BOARD`, `publicLinkRecoveryMode` consistente con ese tipo, tokens de resultado/publico generados, `diffUTC=3` y todos los switches de links en estado habilitado.
- En el alta, cambiar `electionType` recalcula automaticamente `publicLinkRecoveryMode`; en la edicion de detalle ese recalculo no ocurre.
- El preset de tipo se aplica solo al crear y no se vuelve a ejecutar si la eleccion ya tiene calendario o tareas. Cambiar el tipo en detalle no regenera calendario ni tareas.
- Todos los presets actuales fijan `manageOrganizationsManual=true` y `manageVotersManual=true`: las elecciones nuevas comienzan con organizaciones, deudores y padrón en gestión manual. La gestión automática de organizaciones se habilita únicamente en el despliegue que dispone de la integración MiLACNIC.
- `authorizedUserEmails` permite acceso a la eleccion aunque el usuario no tenga rol por categoria; `SecurityUtils.isElectionListedUser` compara por email actual normalizado.
- `authorizedSupportEmails` se usa en el flujo de apoyos a nominacion para restringir los correos de contacto admitidos.
- Los switches de `ElectionConfigurationDashboard` funcionan hoy como corte manual: los textos de ayuda y la implementacion asumen que la disponibilidad real la define el calendario electoral y que estos toggles se usan para bloquear acceso cuando hace falta.
- La solicitud de revision y el cierre irreversible de la eleccion siguen vigentes en configuracion; el cierre conserva resultados agregados y registro de votantes, pero elimina el vinculo votante-candidato.

### Pendiente

- Documentar por separado los modulos de calendario y tareas con sus dependencias operativas completas.
- Profundizar en las pantallas de padrón, organizaciones, candidaturas y auditoria para completar la administracion integral de elecciones.

### Verificacion

- `git diff --check` OK.
- `PATH="/opt/homebrew/opt/ruby@3.3/bin:$PATH" bundle exec jekyll build --source . --destination /tmp/elections-doc-site` OK.

## 2026-04-21 - Pasada final de cierre de documentos en revision

### Fuentes revisadas

- `docs/MatrizActualizacionDocumentacion.md`
- `docs/ReleaseNotes.md`
- guias funcionales y operativas ya cerradas en la rama `doc`

### Hallazgos verificados

- La mayoria de las entradas en `En revision` ya no tenian un pendiente de contenido: solo conservaban ese estado por cierre de navegacion, validacion administrativa o espera del cierre integral del relevamiento.
- En esta pasada se movieron a `Actualizado` los documentos y bloques funcionales cuyo contenido ya habia sido contrastado y enlazado, sin reabrir el relevamiento tecnico.
- El unico bloque que permanece fuera de `Actualizado` por una decision pendiente del usuario es `DOC-008`, asociado al draft textual de configuracion inicial y sus capturas.

### Verificacion

- `git diff --check` OK.

## 2026-04-21 - Guia funcional de seguridad y accesos

### Fuentes revisadas

- `docs/security-access.html`
- `docs/index.html`
- `docs/parameters-templates-i18n.html`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/app/ElectionsWebAdminSession.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/app/SecurityUtils.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/login/LoginDashboard.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/login/LoginPanel.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/bases/DashboardManagerBasePage.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/bases/DashboardElectionBasePage.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/bases/DashboardPublicBasePage.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/bases/PublicTokenBasePage.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/commons/AdminNavBarPanel.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/admin/ipaccess/IpAccessDashboard.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/admin/ipaccess/IpAccessListPanel.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/token/page/PublicAccessDeniedPage.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/token/page/PublicElectionPage.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/token/page/AskCandidateQuestionPage.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/admin/photo/PublicPhotoResizeDashboard.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/publicelections/PublicElectionRecoverLinkPanel.java`
- `elections-ejb/src/main/java/net/lacnic/elections/ejb/impl/ElectionsManagerEJBBean.java`
- `elections-ejb/src/main/java/net/lacnic/elections/ejb/impl/ElectionsVoterEJBBean.java`
- `elections-ejb/src/main/java/net/lacnic/elections/ejb/impl/ElectionsMonitorEJBBean.java`
- `elections-ejb/src/main/java/net/lacnic/elections/dao/IpAccessDao.java`
- `elections-ejb/src/main/java/net/lacnic/elections/scheduler/ElectionScheduler.java`
- `elections-ejb/src/main/java/net/lacnic/elections/utils/Constants.java`
- `elections-services/src/main/java/net/lacnic/elections/ws/auth/WebServiceAuthentication.java`
- `elections-services/src/main/java/net/lacnic/elections/ws/services/ElectionsService.java`
- `elections-services/src/main/java/net/lacnic/elections/ws/services/ElectionsTablesServices.java`
- `elections-services/src/test/java/net/lacnic/elections/ws/auth/WebServiceAuthenticationTest.java`

### Hallazgos verificados

- El login administrativo depende de `WS_AUTH_METHOD`: con `APP` autentica contra `UserAdmin`; con modo centralizado delega en `UtilsLogin.login()` y filtra roles soportados por Elecciones.
- En autenticacion externa, `elections-deleter` solo no habilita ingreso. Para entrar debe existir al menos uno entre `elections-manager`, `elections-statutary-only` o `elections-non-statutary-only`.
- En autenticacion local la sesion agrega automaticamente `elections-manager` y `elections-deleter`.
- Las pantallas globales exigen `elections-manager`. Las pantallas por eleccion aplican un segundo filtro con `SecurityUtils.canAccessElection()` sobre categoria y `authorizedUserEmails`.
- El CAPTCHA de login se decide por IP, no por usuario. Aparece desde `LOGIN_CAPTCHA_MAX_ATTEMPTS` (default `5`) y no se limpia en el mismo login exitoso; solo se reinicia por scheduler segun `LOGIN_CAPTCHA_RATE_LIMIT_CACHE_RESET_HOURS` (default `6`).
- El helper web de IP usa el primer hop de `X-Forwarded-For`, luego `X-Real-IP` y despues `remoteAddr`.
- El rate limit publico solo cuenta accesos con token o identificador inexistente. Con `PUBLIC_FAILED_ACCESS_MAX_ATTEMPTS=50`, el `429` empieza recien en el intento invalido `51` porque la condicion es `>` y no `>=`.
- La tabla `IpAccess` guarda historial persistente de intentos fallidos, pero el bloqueo efectivo vive en cache. El scheduler limpia la cache cada `PUBLIC_FAILED_ACCESS_RATE_LIMIT_CACHE_RESET_HOURS` (default `1`); no borra las filas historicas.
- `IpAccessDao.getAllDisabledIPs()` devuelve todas las filas de `IpAccess`, por lo que `/admin/ipaccess` funciona como visor historico y no como blacklist administrable.
- Las paginas tokenizadas distinguen entre token invalido (`404`, suma al rate limit) y token valido fuera de ventana (`PublicAccessDeniedPage`, sin sumar al rate limit).
- Los formularios publicos de recovery, preguntas y resize de foto reutilizan el mismo backend de CAPTCHA que el login, sujeto a `LOGIN_CAPTCHA_ENABLED`, `DataSiteKeyReCaptcha` y `SkGoogleApiReCaptcha`.
- No hay endpoints REST anonimos en este bloque: `/hc`, endpoints internos, tablas y snapshots `/v2` pasan por autenticacion.
- En modo `APP`, los WS requieren `Authorization=WS_AUTH_TOKEN` mas IP incluida en `WS_AUTHORIZED_IPS`.
- En modo centralizado, los WS internos requieren rol remoto `api-Elections` y los snapshots `/v2` requieren `api-ElectionsPublicInformation`; ademas se valida la IP contra `ipAllowed` devuelto por el servicio remoto.
- La capa REST toma `X-FORWARDED-FOR` completo si existe y no consulta `X-Real-IP`, por lo que su extraccion de IP no coincide exactamente con la UI web.
- Las tablas de monitoreo via WS solo enmascaran `EMAIL_HOST`, `EMAIL_USER`, `EMAIL_PASSWORD`, `WS_AUTH_TOKEN` y `WS_AUTHORIZED_IPS`. Claves sensibles consumidas por el sistema, como `OPENAI_API_KEY`, quedan visibles para clientes autenticados del monitor.

### Pendiente

- Volcar estos hallazgos al bloque final de `docs/ReleaseNotes.md` cuando cierre el relevamiento integral.
- Evaluar si la mascara de parametros sensibles del monitor debe ampliarse en una iteracion funcional futura; hoy queda solo documentado como comportamiento real.

### Verificacion

- `git diff --check` OK.
- No se reintento build Jekyll por instruccion operativa del usuario.

## 2026-04-21 - Cierre de ReleaseNotes v3.0

### Fuentes revisadas

- `docs/ReleaseNotes.md`
- `docs/MatrizActualizacionDocumentacion.md`
- `docs/releaseNoteTmp3.0.md`
- `docs/update.html`

### Hallazgos verificados

- La entrada `v3.0` de release notes quedo consolidada con foco en cambios funcionales, tecnicos y operativos efectivamente contrastados durante el relevamiento.
- La cadena de actualizacion historica desde `v2.3` hacia `v3.0` quedo alineada entre `ReleaseNotes.md` y `update.html`.
- Se mantuvo fuera de release notes el detalle en bruto de la bitacora y los pendientes que no forman parte del cambio de producto, como capturas del draft de configuracion inicial o decisiones futuras de publicacion.
- El PDF `docs/auditReport-v2.3.pdf` se mantiene como referencia historica de la version auditada `v2.3` y no se incluyo como funcionalidad nueva de `v3.0`.

### Pendiente

- Revisar en un cierre final del sitio los documentos que siguen en estado `En revision` dentro de la matriz, sin reabrir el contenido ya relevado salvo correccion factual.

### Verificacion

- `git diff --check` OK.

## 2026-04-21 - Guia transversal de parametros, templates e i18n

### Fuentes revisadas

- `docs/parameters-templates-i18n.html`
- `docs/index.html`
- `docs/results-reports.html`
- `.github/copilot-instructions.md`
- `release-files/ref/parameter_NEW.sql`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/app/ElectionsManagerApp.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/app/ElectionsWebAdminSession.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/app/SecurityUtils.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/admin/parameter/ParametersDashboard.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/admin/parameter/AddParameterPanel.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/admin/parameter/ParametersListPanel.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/admin/parameter/EditParameterDashboard.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/admin/emailtemplate/EmailTemplatesDashboard.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/admin/emailtemplate/AddEmailTemplatePanel.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/admin/emailtemplate/EditEmailTemplatePanel.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/admin/emailtemplate/EmailTemplatesListPanel.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/admin/emailtemplate/EmailTemplateFilterCatalog.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/admin/customization/CustomizationPanel.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/admin/customization/CustomizationPanel.html`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/home/PublicHomeDashboard.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/commons/AdminTopBarPanel.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/bases/PublicTokenTopHeaderPanel.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/bases/PublicTokenBasePage.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/admin/election/create/ElectionCreateDashboard.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/admin/election/detail/ElectionDetailDashboard.java`
- `elections-ejb/src/main/java/net/lacnic/elections/ejb/commons/impl/ElectionsParametersEJBBean.java`
- `elections-ejb/src/main/java/net/lacnic/elections/ejb/commons/impl/MailsSendingEJBBean.java`
- `elections-ejb/src/main/java/net/lacnic/elections/ejb/impl/ElectionsManagerEJBBean.java`
- `elections-ejb/src/main/java/net/lacnic/elections/domain/Election.java`
- `elections-ejb/src/main/java/net/lacnic/elections/domain/ElectionEmailTemplate.java`
- `elections-ejb/src/main/java/net/lacnic/elections/domain/LanguageCode.java`
- `elections-ejb/src/main/java/net/lacnic/elections/utils/Constants.java`

### Hallazgos verificados

- La pantalla de parametros es global y plana: no existe segmentacion por entorno ni por modulo, solo altas/ediciones/bajas de pares `key/value`.
- El backend devuelve string vacio para parametros faltantes y limpia la cache cada vez que se agrega, edita o elimina un parametro.
- `APP` tiene semantica especial: si falta, la instancia se considera productiva; el modo no productivo depende de que el valor exista y no sea `PROD`.
- `release-files/ref/parameter_NEW.sql` siembra claves reales de runtime, mail, WS, external sync, Campus, OpenAI, CAPTCHA y condiciones de aceptacion de nominacion.
- La personalizacion visual es global, no multidioma ni por eleccion: logos, simbolo, `siteTitle`, `loginTitle`, `showHome` y `homeHtml`.
- `homeHtml` se renderiza sin escape en el home publico, por lo que opera como HTML confiado almacenado en base.
- Los templates base se crean solo con `electionId=0`; al crear una eleccion se copian automaticamente a la eleccion las plantillas faltantes.
- El motor de envio resuelve primero template de eleccion y luego base; editar el base no pisa automaticamente elecciones ya abiertas.
- La accion `forceBaseTemplateToOpenElections` reemplaza la copia del template en todas las elecciones no cerradas.
- La UI obliga asunto y cuerpo en los tres idiomas para crear/editar templates, pero el motor igual tiene fallback runtime de `EN/PT` hacia `SP`.
- Algunos tipos de template disparan `STANDARD_DISPATCH_NOTICE` adicional y la firma se inyecta solo si el texto contiene `$signature`.
- Si falta `LINK_RECOVERY`, existe un fallback interno trilingue para no cortar la recuperacion de enlaces.
- La UI fija del sistema usa bundles Wicket; la guia interna de `.github/copilot-instructions.md` sigue alineada con el uso actual del repo: texto visible fijo via `wicket:message`, no hardcoded.
- En el modelo `Election` no hay fallback runtime de titulos/descripciones/links entre idiomas; cuando `onlySp=true`, create/detail copian SP hacia EN/PT al guardar.
- El selector de idioma existe tanto en admin como en publico/token, y las paginas tokenizadas preservan `locale` en recargas y redirecciones.

### Pendiente

- Relevar `FUN-017` para cerrar autenticacion admin, WS, accesos por IP, rate limits y compuertas de seguridad.
- Dejar `docs/ReleaseNotes.md` para el cierre del relevamiento completo.
- Pendiente histórico cancelado el 2026-09-08: el borrador Markdown se retira y no queda programada su publicación.

### Verificacion

- `git diff --check` OK.
- No se reintento build Jekyll por instruccion operativa del usuario.

## 2026-04-21 - PDF base sin capturas para configuracion inicial

### Fuentes revisadas

- Borrador textual de configuración inicial disponible en esa fecha, retirado el 2026-09-08.
- Manual histórico de 2021 (retirado; consulte [el manual vigente](manual.html)).

### Hallazgos verificados

- Se genero `docs/EleccionesConfiguracionInicialv3.0.pdf` a partir del borrador textual actual.
- Para evitar mezclar contenido transitorio con la version de trabajo, se filtraron los marcadores `[incluir captura de pantalla: ...]`.
- El PDF queda versionado como documento de trabajo interno y no como guia final enlazada en el sitio.

### Verificacion

- `file docs/EleccionesConfiguracionInicialv3.0.pdf` OK (`PDF document, version 1.3, 7 pages`).

## 2026-04-21 - Guia funcional de auditoria previa y final

### Fuentes revisadas

- `docs/auditors.html`
- `docs/index.html`
- `docs/questions-declarations.html`
- `docs/MatrizActualizacionDocumentacion.md`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/admin/election/auditors/ElectionAuditorsDashboard.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/admin/election/auditors/AddAuditorPanel.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/admin/election/auditors/AuditorsListPanel.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/admin/election/auditors/EditAuditorDashboard.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/admin/election/prereports/PreElectionReportsPanel.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/token/page/AuditPublicDashboardPage.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/token/page/AuditPublicCandidateDetailPage.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/token/page/AuditPublicResultsPage.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/results/audit/AuditorRevisionPanel.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/results/audit/AuditorConformityPanel.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/results/audit/MoreInformationForAuditPanel.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/results/review/ReviewDashboard.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/results/review/ReviewAuditorsListPanel.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/results/review/ReviewVotesListPanel.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/validators/AuditorValidator.java`
- `elections-ejb/src/main/java/net/lacnic/elections/domain/Auditor.java`
- `elections-ejb/src/main/java/net/lacnic/elections/domain/pre/AuditorCandidateDecision.java`
- `elections-ejb/src/main/java/net/lacnic/elections/domain/pre/AuditorCandidateDecisionStage.java`
- `elections-ejb/src/main/java/net/lacnic/elections/domain/pre/AuditorCandidateDecisionStatus.java`
- `elections-ejb/src/main/java/net/lacnic/elections/ejb/impl/ElectionsManagerEJBBean.java`
- `elections-ejb/src/main/java/net/lacnic/elections/ejb/impl/ElectionsVoterEJBBean.java`
- `elections-ejb/src/main/java/net/lacnic/elections/ejb/commons/impl/MailsSendingEJBBean.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/app/ElectionsManagerApp.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/app/ElectionsManagerApp.properties.xml`

### Hallazgos verificados

- La administracion de auditores vive en `/admin/election/auditors` y solo trabaja sobre elecciones abiertas.
- Cada auditor se crea con nombre, mail, frecuencia de recordatorio y flag de comisionado; la validacion remota bloquea duplicados para la eleccion.
- El seguimiento interno previo ya separa por auditor `1ra verificacion` y `Final`, y reconstruye compatibilidad hacia atras cuando una decision vieja solo tiene columnas legacy.
- El acceso tokenizado de auditoria usa `Auditor.resultToken` y se reparte entre `/token/audit`, `/token/audit/candidate` y `/token/audit/result`.
- La auditoria de candidatos queda disponible desde `N_1_SINGLE_CALL_FOR_CANDIDATES_PUBLISHED`; la auditoria final de resultados usa exclusivamente la ventana `N_18_PERIODO_CE_AUDIT`.
- Solo los auditores comisionados pueden cargar decisiones desde la pagina tokenizada del candidato.
- La etapa previa solo admite `PREAPPROVED` o `REJECTED` sobre candidatos `PRECOMPLETE`; la etapa final solo admite `APPROVED` o `REJECTED` sobre candidatos `COMPLETE`.
- Los comentarios de auditoria son opcionales, se sanean en backend y tienen limite efectivo de `4000` caracteres.
- La carga tokenizada crea la fila de decision si no existia. La edicion administrativa del auditor solo corrige filas ya existentes y no dispara correo al remitente de la eleccion.
- `REJECTED` y `APPROVED` por flujo tokenizado notifican al remitente de la eleccion; `PREAPPROVED` no dispara correo de aprobacion.
- La vista de auditoria del candidato ya consume tarjetas de preguntas estatutarias/no estatutarias y declaraciones, por lo que `FUN-011` impacta directamente en `FUN-012`.
- La auditoria final de resultados muestra resultados, codigos de candidatos e informacion agregada de porcentajes/habilitados/participantes/pesos cuando no hay solicitud de revision.
- Confirmar conformidad final marca `agreedConformity=true` y encola `AUDITOR_AGREEMENT`.
- Si la eleccion tiene `revisionRequest=true`, el comisionado puede habilitar revision; eso marca `revisionAvailable=true`, encola `AUDITOR_REVISION` y registra actividad.
- El dashboard interno `/review` aparece por switch de eleccion, pero el detalle de votos solo queda visible cuando todos los auditores comisionados habilitaron revision.

### Pendiente

- Continuar con `FUN-013` para documentar votacion, links publicos y recuperacion de accesos.
- Profundizar luego `FUN-014` para cerrar resultados, reportes y consumo final de revision/auditoria en la documentacion integral.

### Verificacion

- `git diff --check` OK.
- No se reintento build de Jekyll por instruccion del usuario.

## 2026-04-21 - Guia funcional de votacion y links publicos

### Fuentes revisadas

- `docs/voting-public-links.html`
- `docs/index.html`
- `docs/auditors.html`
- `docs/MatrizActualizacionDocumentacion.md`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/app/ElectionsManagerApp.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/bases/PublicTokenBasePage.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/token/page/PublicAccessDeniedPage.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/token/page/PublicElectionPage.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/token/page/PublicElectionPageV2.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/token/page/PublicElectionPageParameters.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/token/panel/PublicElectionRecoverLinkPanel.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/token/page/DoNominationPage.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/token/page/SupportNominationPage.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/token/page/AcceptNominationConditionsPage.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/token/page/GenericAcceptNominationTasksPage.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/token/page/VotePublicPage.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/token/page/ResultPublicPage.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/admin/election/prereports/PreElectionReportsPanel.java`
- `elections-ejb/src/main/java/net/lacnic/elections/publicelection/PublicElectionVisibilityResolver.java`
- `elections-ejb/src/main/java/net/lacnic/elections/publicelection/PublicElectionSnapshotBuilder.java`
- `elections-ejb/src/main/java/net/lacnic/elections/domain/ElectionLinkRecoveryMode.java`
- `elections-ejb/src/main/java/net/lacnic/elections/utils/PublicLinkRecoveryType.java`
- `elections-ejb/src/main/java/net/lacnic/elections/utils/LinksUtils.java`
- `elections-ejb/src/main/java/net/lacnic/elections/utils/Constants.java`
- `elections-ejb/src/main/java/net/lacnic/elections/dao/UserVoterDao.java`
- `elections-ejb/src/main/java/net/lacnic/elections/dao/OrganizationDao.java`
- `elections-ejb/src/main/java/net/lacnic/elections/dao/SupportNominationDao.java`
- `elections-ejb/src/main/java/net/lacnic/elections/ejb/impl/ElectionsManagerEJBBean.java`
- `elections-ejb/src/main/java/net/lacnic/elections/ejb/impl/ElectionsVoterEJBBean.java`
- `elections-ejb/src/main/java/net/lacnic/elections/ejb/commons/impl/MailsSendingEJBBean.java`
- `elections-ejb/src/main/java/net/lacnic/elections/cron/ElectionScheduler.java`

### Hallazgos verificados

- Las rutas montadas hoy para experiencia publica/tokenizada son `/token/public-election`, `/token/public-election-v2`, `/token/organization/do-nomination`, `/token/nomination/support`, `/token/nomination/tasks`, `/token/vote`, `/token/result` y `/token/access-denied`.
- El link generado por dominio para landing publica sigue apuntando a `/token/public-election`; `public-election-v2` existe como variante montada en paralelo, no como target principal del generador de links.
- La landing publica valida `Election.publicElectionToken`, requiere `publicElectionLinkAvailable=true` y abre en `N_1_SINGLE_CALL_FOR_CANDIDATES_PUBLISHED`.
- La misma landing resuelve modos `landing`, `candidate` y `roll`, con aliases `electoral-roll` y `padron` para el modo de padron publico.
- El resumen de padron publico solo aparece para elecciones estatutarias y desde `N_4_SINGLE_PADRON_PUBLISHED`.
- La tarjeta de resultados en landing y `/token/result` dependen de `resultLinkAvailable=true` y de haber alcanzado `N_17_SINGLE_PROVISIONAL_RESULTS_PUBLISHED`; `/token/result` no tiene cierre temporal adicional.
- La recuperacion publica de links no tiene ruta separada ni calendario propio: aparece dentro de la landing cuando la eleccion sigue abierta y `publicLinkRecoveryMode` no es `NONE`.
- `publicLinkRecoveryMode` soporta `NONE`, `ONLY_BR` y `ALL`; el default vigente por tipo de eleccion sale de `ElectionLinkRecoveryMode.defaultForElectionType`.
- El recovery por email busca coincidencias separadas en votantes, organizaciones y apoyos, con tope de `10` coincidencias por tipo.
- En `ONLY_BR` el filtro por pais se aplica sobre `UserVoter.country`, `Organization.country` y el pais de la organizacion nominadora para apoyos.
- El formulario de recovery devuelve mensaje generico de aceptacion aunque no haya coincidencias, y solo expone error si hubo falla operativa al encolar correos.
- Si falta template `LINK_RECOVERY`, el sistema usa un fallback trilingue armado en codigo.
- `/token/organization/do-nomination` y `/token/nomination/support` dependen de sus switches manuales y de la ventana `N_2_PERIODO_CALL_FOR_CANDIDATES`.
- El link de aceptacion de candidatura se construye sobre `/token/nomination/tasks`; esa misma ruta redirige internamente a condiciones si la nominacion todavia no fue aceptada.
- La ventana de tareas de candidatura no depende de un solo calendario: usa el inicio mas temprano y el cierre mas tardio entre las tareas configuradas para el candidato.
- `/token/vote` depende de `votingLinkAvailable=true` y `N_16_PERIODO_VOTING`; si el votante ya voto, la pagina queda en modo informativo.
- En elecciones conjuntas, `/token/vote` arma una vista doble buscando el segundo votante por `ORGID`; los subtokens `token1` y `token2` permiten entrar directo a cada boleta.
- Todas las paginas tokenizadas publicas comparten rate limit por intentos invalidos de token: el default actual es `50` intentos fallidos por IP antes de responder `429`.

### Pendiente

- Continuar con `FUN-014` para documentar resultados y reportes administrativos/publicos.
- Dejar para `FUN-016` y `FUN-017` el detalle fino de parametros operativos y seguridad general mas alla del rate limit publico observado en este bloque.

### Verificacion

- `git diff --check` OK.
- No se reintento build de Jekyll por instruccion del usuario.

## 2026-04-21 - Guia funcional de preguntas, declaraciones y comunidad

### Fuentes revisadas

- `docs/questions-declarations.html`
- `docs/candidates.html`
- `docs/index.html`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/admin/election/questions/ElectionQuestionsDashboard.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/admin/election/questions/QuestionsListPanel.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/admin/election/questions/QuestionEditPanel.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/token/taskpanels/GenericNominationDeclarationsManagementPanel.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/token/taskpanels/GenericNominationNonStatutoryDeclarationsManagementPanel.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/token/taskpanels/GenericNominationOtherStatutoryQuestionsManagementPanel.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/token/taskpanels/GenericNominationOtherNonStatutoryQuestionsManagementPanel.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/token/CandidateCommunityQuestionsPanel.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/token/page/GenericAcceptNominationTasksPage.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/token/page/PublicElectionPage.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/token/page/AskCandidateQuestionPage.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/token/page/AuditPublicCandidateDetailPage.java`
- `elections-ejb/src/main/java/net/lacnic/elections/domain/Candidate.java`
- `elections-ejb/src/main/java/net/lacnic/elections/domain/pre/CandidateQuestion.java`
- `elections-ejb/src/main/java/net/lacnic/elections/domain/pre/CandidateQuestionStatus.java`
- `elections-ejb/src/main/java/net/lacnic/elections/domain/pre/ElectionTaskKey.java`
- `elections-ejb/src/main/java/net/lacnic/elections/dao/CandidateQuestionDao.java`
- `elections-ejb/src/main/java/net/lacnic/elections/ejb/impl/ElectionsPreNominationEJBBean.java`
- `elections-ejb/src/main/java/net/lacnic/elections/ejb/impl/ElectionsManagerEJBBean.java`
- `elections-ejb/src/main/java/net/lacnic/elections/publicelection/PublicElectionVisibilityResolver.java`
- `elections-ejb/src/main/java/net/lacnic/elections/publicelection/PublicElectionSnapshotBuilder.java`

### Hallazgos verificados

- Las preguntas estatutarias del checklist del candidato son exactamente cuatro y sus labels salen de parametros `OTHER_STATUTORY_Q1_LABEL..Q4_LABEL`, con fallback local si no existen.
- La pregunta no estatutaria del checklist del candidato es una sola y sale de `OTHER_NON_STATUTORY_Q1_LABEL`, tambien con fallback local.
- Las respuestas abiertas del candidato se guardan inicialmente en espanol y EN/PT quedan replicados con el mismo texto hasta que un admin las traduzca.
- La tarea `DECLARATIONS` exige siempre D1, D2, D3, D5, D6 y D7; D4 solo aparece para elecciones `BOARD`.
- La tarea `DECLARATIONS_NON_STATUTORY` agrega D8 solo para `IANA`, D9 solo para `ASO`, y siempre vuelve a incluir D6 y D7.
- Las declaraciones no tienen guardado parcial; las preguntas abiertas si permiten borrador (`STARTED`) antes del cierre (`COMPLETED`).
- El dashboard administrativo de preguntas permite crear/editar preguntas de comunidad con candidato, idioma, status, pregunta SP/EN/PT y respuesta SP/EN/PT, y guardar con o sin correo.
- Las notificaciones por cambio de estado hoy salen en tres casos: `QUESTION_READY_FOR_CANDIDATE` al candidato, `ANSWER_SUBMITTED_BY_CANDIDATE` al destinatario estandar de la eleccion, y `PUBLISHED` a quien hizo la pregunta.
- La carga publica de preguntas permite una sola pregunta para un candidato o replicarla a todos los candidatos publicos de la eleccion.
- La UI publica permite hasta 1000 caracteres para la pregunta, pero el backend la normaliza a 500 caracteres maximos al persistir.
- Las preguntas publicas nacen en `QUESTION_RECEIVED_LACNIC`, owner `LACNIC`, con el mismo texto replicado en SP/EN/PT por compatibilidad historica.
- En el link de candidatura, el panel lateral de preguntas de comunidad solo se muestra mientras `N_12_PERIODO_CANDIDATE_QUESTIONS` sigue abierto y solo deja responder preguntas en `QUESTION_READY_FOR_CANDIDATE`.
- La respuesta del candidato a una pregunta de comunidad se normaliza a 1000 caracteres maximos y queda inicialmente igual en SP/EN/PT.
- La pagina publica del candidato muestra preguntas de comunidad publicadas y tambien pendientes, con mascaras o mensajes de espera segun el estado; las rechazadas no se muestran.
- El resumen de auditoria del candidato ya consume tarjetas para preguntas estatutarias/no estatutarias y declaraciones, pero no tiene una tarjeta separada para preguntas de comunidad.

### Pendiente

- Relevar `FUN-012` para documentar como usan estas respuestas y declaraciones las pantallas de auditoria previa y final.
- Relevar `FUN-013` para cerrar la experiencia publica completa de candidatos, preguntas, votacion y recuperacion de links.

### Verificacion

- `git diff --check` OK.
- `PATH="/opt/homebrew/opt/ruby@3.3/bin:$PATH" BUNDLE_PATH=/tmp/elections-bundle BUNDLE_APP_CONFIG=/tmp/elections-bundle-config bundle install` OK.
- `PATH="/opt/homebrew/opt/ruby@3.3/bin:$PATH" BUNDLE_PATH=/tmp/elections-bundle BUNDLE_APP_CONFIG=/tmp/elections-bundle-config bundle exec jekyll build --source . --destination /tmp/elections-doc-site` reintentado; quedo en `Generating...` tras warning no bloqueante de `GitHub Metadata` y se dejo de perseguir por decision operativa.

## 2026-04-21 - Guia funcional de padron y censo

### Fuentes revisadas

- `docs/census.html`
- `docs/elections.html`
- `docs/index.html`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/app/ElectionsManagerApp.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/app/ElectionsManagerApp.properties.xml`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/admin/election/census/ElectionCensusDashboard.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/admin/election/census/ElectionCensusDashboard.html`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/admin/election/census/UploadCensusFilePanel.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/admin/election/census/UploadCensusFilePanel.html`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/admin/election/census/AddUserVoterPanel.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/admin/election/census/AddUserVoterPanel.html`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/admin/election/census/EditUserVoterDashboard.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/admin/election/census/EditUserVoterDashboard.html`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/admin/election/census/CensusListPanel.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/admin/election/census/CensusListPanel.html`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/admin/election/census/AutomaticCensusSyncActionsPanel.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/admin/election/census/CensusAdvancedActionsPanel.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/validators/CensusExcelFileValidator.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/validators/CensusOrgIdIdentityValidator.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/validators/CensusDeleteActionValidator.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/validators/CensusVotesValidator.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/validators/OrganizationValidationRules.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/validators/OptionalOrganizationOrgIdValidator.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/validators/OptionalOrganizationCountryValidator.java`
- `elections-ejb/src/main/java/net/lacnic/elections/utils/ExcelUtils.java`
- `elections-ejb/src/main/java/net/lacnic/elections/utils/FilesUtils.java`
- `elections-ejb/src/main/java/net/lacnic/elections/ejb/impl/ElectionsManagerEJBBean.java`

### Hallazgos verificados

- El modulo de padron esta montado en `ElectionCensusDashboard` bajo `/admin/election/voters`, entre organizaciones y calendario.
- El comportamiento real depende de `manageVotersManual`: en modo manual aparecen upload, alta, edicion y baja; en modo automatico el padron se recalcula desde organizaciones locales.
- La identidad operativa del padron en `v3.0` es `ORGID`. La reconciliacion no se hace por email; el backend normaliza `ORGID` a mayusculas y exige unicidad por eleccion.
- La carga Excel usa la primera hoja del `.xlsx`, requiere `IDIOMA`, `NOMBRE`, `MAIL`, `CANTVOTOS` y `ORGID`, acepta `ORGNAME` y `PAIS`, y procesa el archivo como snapshot completo: altas, actualizaciones y bajas por ausencia en la planilla.
- Si una carga o sincronizacion implicaria borrar votantes que ya votaron, el backend rechaza toda la operacion.
- El check de upload no define si se sobreescribe el padron; siempre se reconcilia completo. Solo define si se regeneran los tokens de votacion de registros existentes.
- El alta manual genera siempre un `voteToken` nuevo y deja `electorsSet=true`. La edicion manual no regenera token. La baja manual esta bloqueada si el votante ya voto y, si borra el ultimo registro, deja `electorsSet=false`.
- `Marcar como terminada y seguir` solo persiste `electorsSet=true`; no valida que haya votantes cargados ni que una sincronizacion automatica se haya ejecutado.
- El listado operativo permite ver link, regenerar token individual y reenviar mail aun en modo automatico; editar y eliminar solo aparecen en modo manual.
- La sincronizacion automatica del padron no consume external sync directamente desde esta pantalla: toma la lista local de organizaciones de la eleccion y solo incluye organizaciones miembro, no deudoras, con `orgId`, contacto de membresia y votos.
- El boton `Forzar sincronizacion del padron` solo se habilita dentro de `N_23_PERIODO_PADRON_MILACNIC_SYNC` por compatibilidad historica y mantiene links existentes; solo genera token cuando falta.
- El cron combinado primero sincroniza organizaciones y luego padron dentro de `N_23`, usando el mismo criterio por `ORGID`.
- Persisten artefactos legacy como `CensusEmailIdentityValidator` y `exampleCensusEmail.xlsx`, pero no estan conectados al flujo activo: la UI y el backend vigentes operan por `ORGID`.

### Pendiente

- Profundizar mas adelante el impacto del modulo de organizaciones ya documentado sobre candidaturas, apoyos y experiencia publica.
- Profundizar mas adelante el impacto del padron sobre votacion publica, recuperacion de links, resultados y auditoria.

### Verificacion

- `git diff --check` OK.
- `PATH="/opt/homebrew/opt/ruby@3.3/bin:$PATH" bundle exec jekyll build --source . --destination /tmp/elections-doc-site` OK.

## 2026-04-21 - Guia funcional de calendario y tareas electorales

### Fuentes revisadas

- `docs/calendar-tasks.html`
- `docs/elections.html`
- `docs/index.html`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/admin/election/calendarmodule/ElectionCalendarModuleDashboard.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/admin/election/calendarmodule/ElectionCalendarModuleDashboard.html`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/admin/election/tasks/ElectionTasksDashboard.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/admin/election/tasks/ElectionTasksDashboard.html`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/admin/election/tasks/ElectionTasksPanel.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/admin/election/tasks/ElectionTasksPanel.html`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/admin/election/ManageElectionTabsPanel.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/bases/DashboardElectionBasePage.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/app/ElectionsManagerApp.properties.xml`
- `elections-ejb/src/main/java/net/lacnic/elections/domain/pre/ElectionCalendarKey.java`
- `elections-ejb/src/main/java/net/lacnic/elections/domain/pre/ElectionTaskKey.java`
- `elections-ejb/src/main/java/net/lacnic/elections/domain/pre/TaskDependencyLevel.java`
- `elections-ejb/src/main/java/net/lacnic/elections/domain/pre/ElectionPresetConfigurations.java`
- `elections-ejb/src/main/java/net/lacnic/elections/ejb/impl/ElectionsManagerEJBBean.java`
- `elections-ejb/src/main/java/net/lacnic/elections/publicelection/PublicElectionSnapshotBuilder.java`
- `elections-admin-web/src/main/java/net/lacnic/elections/adminweb/ui/token/page/PublicElectionPage.java`

### Hallazgos verificados

- El modulo vigente de calendario es `calendar-module`; ya no corresponde documentar el flujo legacy basado en la UI anterior.
- El sistema administra una etapa por cada `ElectionCalendarKey` (`N_1` a `N_23`), distinguiendo hitos `SINGLE` y ventanas `PERIODO`.
- `N_16_PERIODO_VOTING` es obligatoria y no admite `undefinedDate`; el resto de las etapas puede quedar sin fecha y, en ese caso, la UI obliga `publicable=false`.
- La UI presenta fechas ajustadas con `diffUTC`, pero el backend persiste nuevamente en UTC al guardar.
- El timeline marca solapamientos entre etapas, pero esa condicion es solo una advertencia visual: el guardado solo bloquea fechas vacias y rangos con `fin < inicio`.
- `Guardar` en calendario/tareas no marca completitud; `Marcar como terminada y seguir` persiste y ademas deja `calendarSet=true` o `tasksSet=true`.
- El flag `publicable` del calendario y de las tareas tiene consumo real en snapshots y experiencia publica; no es solo metadato administrativo.
- Los presets crean registros de calendario para todas las etapas y aplican defaults de fecha/publicacion segun el tipo de eleccion.
- Las tareas se definen por `ElectionTaskKey`, calendario asociado, `TaskDependencyLevel`, `displayOrder` y `publicable`.
- No se pueden repetir claves de tarea en una eleccion; el formulario filtra las ya usadas y el backend vuelve a validarlo.
- Agregar o editar una tarea crea progreso `NOT_STARTED` para candidatos existentes sin progreso en esa tarea, excepto candidatos de abstencion.
- Una tarea no puede eliminarse si ya tiene progreso asociado, por lo que en elecciones con candidatos cargados la eliminacion puede quedar bloqueada.
- Los movimientos subir/bajar renumeran todas las tareas y la experiencia publica usa la visibilidad por clave de tarea resuelta en snapshot.

### Pendiente

- Profundizar el consumo funcional de calendarios y tareas dentro de nominaciones, apoyos, auditoria y experiencia publica antes de cerrar la documentacion integral de elecciones.

### Verificacion

- `git diff --check` OK.
- `PATH="/opt/homebrew/opt/ruby@3.3/bin:$PATH" bundle exec jekyll build --source . --destination /tmp/elections-doc-site` OK.
