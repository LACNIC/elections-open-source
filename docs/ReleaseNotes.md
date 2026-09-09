---
layout: default
title: Release notes
permalink: /release-notes.html
---
# Release Notes

## v3.0

#### Destacados:

* Se consolida la versión actual del sistema sobre Java 17, WildFly 34 y despliegue Docker como mecanismo principal de instanciación.
* El proyecto queda documentado para adopción por cualquier organización: autenticación local `APP`, gestión manual y perfil Docker autocontenido no dependen de servicios de LACNIC; las integraciones externas son opcionales.
* La cadena de actualización posterior a `v2.4` queda resumida en `release-files/3.0/v3.0_script.sql`; para instalaciones históricas en `v2.3` o `v2.3.1` se mantiene la secuencia `v2.3.1 -> v2.4 -> v3.0`.
* Se normaliza el uso de `ORGID` como identidad operativa de organizaciones y padrón, con controles previos e índices únicos por elección.
* Se consolida la operación electoral actual: configuración por tipo de elección, calendario `N_1..N_23`, nominaciones y apoyos, auditoría previa y final, links públicos, votación y resultados.

#### Nuevas funcionalidades y correcciones:

* `release-files/3.0/v3.0_script.sql` consolida los cambios intermedios posteriores a `v2.4` en un único script de actualización.
* La migración completa `election.election_type` cuando falta y rellena `uservoter.orgid` desde `mail` si es recuperable.
* La actualización crea los índices únicos `uq_organization_election_orgid_norm` y `uq_uservoter_election_orgid_norm`, y aborta si detecta `ORGID` duplicados por elección o votantes sin `ORGID` recuperable.
* El modelo de elección incorpora `authorized_user_emails`, `authorized_support_emails`, `authorized_nominate_emails`, `callspanish`, `callenglish`, `callportuguese`, `callset` y `public_link_recovery_mode`.
* `public_link_recovery_mode` queda inicializado por tipo de elección (`ONLY_BR`, `ALL` o `NONE`) y gobierna la recuperación pública de links desde la landing.
* La gestión de elecciones usa presets por tipo para calendario, tareas, padrón y organizaciones, y mantiene switches manuales para acceso público, revisión y cierre irreversible.
* Padrón y organizaciones soportan gestión manual o automática, cargas XLSX, sincronización desde external sync dentro de `N_23` y reconciliación por `ORGID`; el padrón automático toma organizaciones locales elegibles y preserva links existentes cuando corresponde.
* El circuito de candidaturas soporta alta manual, nominación desde organización, aceptación o rechazo por token, apoyos por organización o contacto y publicación final de boleta sólo para candidatos `CONFIRMED_AND_PUBLISHED`.
* El flujo del candidato incorpora declaraciones, preguntas estatutarias y no estatutarias, preguntas de comunidad moderadas, progreso por tareas y validación posterior por auditores.
* La auditoría separa predecisión y decisión final, incorpora conformidad de resultados y habilitación coordinada de revisión interna de votos por auditores comisionados.
* Resultados y reportes incorporan dashboard interno, pre-reportes operativos HTML y códigos de verificación por voto, además de resultados públicos y auditoría final por ventana.
* La experiencia pública/tokenizada consolida landing, padrón público estatutario, recuperación de links por email, nominaciones, apoyo, tareas de candidatura, votación simple o conjunta, resultados públicos y rate limit por accesos inválidos.
* La autenticación administrativa y de WS sigue gobernada por `WS_AUTH_METHOD`, con modo local `APP` o autenticación centralizada externa, más CAPTCHA por IP y rate limits públicos configurables.
* `LOGIN_CAPTCHA_ENABLED`, `AI_TEXT_IMPROVEMENT_ENABLED` y `PUBLIC_NOMINATION_ENABLED` permiten controlar explícitamente CAPTCHA, asistencia de texto con IA y nominación pública (incluido el tipo `OTHER`).
* En autenticación local `APP`, Campus queda deshabilitado y no genera solicitudes ni errores de conexión visibles.
* Se amplía la configuración global con personalización visual, templates base y por elección, fallbacks de idioma y parámetros base para revisión asistida de texto con IA.
* Los servicios REST mantienen autenticación obligatoria, snapshots públicos `v2`, paginación por `WS_MAX_PAGE_SIZE` y endpoints de tablas/monitoreo alineados al estado actual del sistema.
* Docker es la forma vigente de instanciación; la instalación manual queda como alternativa avanzada y se retira el restore Ansible legado junto con sus dumps de test.
* El perfil `docker-fresh.sh` construye aplicación, PostgreSQL y Mailpit para evaluación o desarrollo. No es una configuración productiva lista para Internet; producción requiere servicios y controles administrados por cada organización.

#### Procedimiento de actualización:

###### Base de datos

Antes de ejecutar `v3.0_script.sql`, revise duplicados de `ORGID` y votantes sin `ORGID` recuperable, porque la migración crea índices únicos y corta ante inconsistencias. Las consultas previas y el procedimiento detallado están documentados en la [guía de actualización](update.html).

Si la instalación ya está en `v2.4`, se debe aplicar el siguiente script:
* [v3.0_script.sql](../release-files/3.0/v3.0_script.sql): consolida los cambios de base de datos posteriores a `v2.4`.

Si la instalación está en `v2.3.1`, se deben aplicar, en orden, los siguientes scripts:
* [v2.4_ddl_script.sql](../release-files/2.4/v2.4_ddl_script.sql)
* [v2.4_data_script.sql](../release-files/2.4/v2.4_data_script.sql)
* [v3.0_script.sql](../release-files/3.0/v3.0_script.sql)

Si la instalación está en `v2.3`, se deben aplicar, en orden, los siguientes scripts:
* [v2.3.1_script.sql](../release-files/2.3.1/v2.3.1_script.sql)
* [v2.4_ddl_script.sql](../release-files/2.4/v2.4_ddl_script.sql)
* [v2.4_data_script.sql](../release-files/2.4/v2.4_data_script.sql)
* [v3.0_script.sql](../release-files/3.0/v3.0_script.sql)

###### Aplicación y despliegue

* La versión actual compila con Java 17 y se despliega sobre WildFly 34.
* Para despliegue manual, se deben publicar los artefactos generados por `elections-ejb`, `elections-admin-web` y `elections-services`, junto con los módulos y configuración de WildFly correspondientes.
* Para despliegue Docker, se debe reconstruir o publicar la imagen vigente y redeployar con `docker-compose.yml`; las migraciones de base se toman de `release-files/`.
* Después de actualizar, se recomienda validar al menos login administrativo, listado de elecciones, acceso a una elección existente y disponibilidad del servicio web si está habilitado en la instalación.

## v2.4 (16/03/2026)

#### Nuevas funcionalidades y correcciones:

* Se incorpora el nuevo modelo de datos para elecciones, calendarios y candidatos.
* Se renombra el atributo `declarationsScope` a `electionType` en el modelo de elecciones.
* Se agregan las estructuras necesarias para soportar calendarios, tareas y configuración manual de padrón y organizaciones.
* Se actualizan los parámetros y templates base del sistema.

#### Procedimiento de actualización:

###### Base de datos

Se deben aplicar, en orden, los siguientes dos scripts sobre la base de datos:
* [v2.4_ddl_script.sql](../release-files/2.4/v2.4_ddl_script.sql): crea y ajusta la estructura necesaria para el nuevo modelo.
* [v2.4_data_script.sql](../release-files/2.4/v2.4_data_script.sql): migra los datos legacy, inicializa calendarios y tareas, y actualiza parámetros y templates.

## v2.3.1 (07/08/2023)

#### Nuevas funcionalidades y correcciones:

* Se amplio la información que se muestra en WS que muestra el reporte de partición por email.

#### Script de actualización de base de datos:

Es necesario ejecutar el siguiente script de base de datos:
[v2.3.1_script.sql](../release-files/2.3.1/v2.3.1_script.sql)

## v2.3 (29/09/2022)

#### Nuevas funcionalidades y correcciones:

* El sistema ahora permite tener más de un Votante con el mismo e-mail en la misma elección.
* El sistema ahora deja registro cuando un admin vé el link de un Auditor, así como ya sucedía con Link Votante.
* Se actualizó el informe de Auditoría en el Home y ahora lo toma de un parámetro.
* El sistema incluye ahora 2 mecanismos de Login Administrativo tanto para la aplicación como el WS, según el parámetro WS_AUTH_METHOD. Si está en APP, autentica contra la base de datos como siempre, pero si está en modo externo autentica contra un sistema centralizado.
* Se agrega una actividad de log específica para el nuevo Login Administrativo.
* Validación más sencilla de la dirección de e-mail.
* Se corrigió un problema de formato y orden de Fecha en la lista de Actividades.
* Mejoras y corrección de bugs menores.


#### Script de actualización de base de datos:

Es necesario ejecutar el siguiente script de base de datos:
[v2.3_script.sql](../release-files/2.3/v2.3_script.sql)

## v2.2 (27/04/2022)

#### Nuevas funcionalidades y correcciones:

* Nueva funcionalidad Cierre de elección permite eliminar el registro de candidato(s) elegido(s) por cada votante.

#### Script de actualización de base de datos:

Es necesario ejecutar el siguiente script de base de datos:
[v2.2_script.sql](../release-files/2.2/v2.2_script.sql)


## v2.1 (11/11/2021)

#### Nuevas funcionalidades y correcciones:

* Fix menor en servicio de tabla de Emails.


## v2.0 (14/10/2021)

#### Destacados:

* Refactor general de código.
* Nuevos servicios.
* Soporte archivos .xlsx.

#### Recomendación:

Se recomienda aplicar esta actualización mientras no tenga elecciones activas. Los enlaces que hayan sido enviados vía email ya no serán válidos (contexto de la aplicación cambia de `/elecciones` a `/elections`).

#### Nuevas funcionalidades y correcciones:

* Traducción de todo el código y la estructura de base de datos a inglés.
* Reorganización del código en módulo web.
* Ajuste de funcionalidad para configuración de captcha en modo PROD.
* Se agregó email al Candidato.
* Soporte de archivos .xslx en importación y exportación de padrón electoral.
* Nuevos servicios y autenticación contra servicio centralizado externo.
* Mejoras y corrección de bugs menores.

#### Procedimiento de actualización:

###### Base de datos

Se deben aplicar, en orden, los siguientes dos scripts sobre la base de datos:
* [v2.0_ddl_script.sql](../release-files/2.0/v2.0_ddl_script.sql): traducción a inglés, DROP de recursos no usados, nueva secuencia para Comisionados, mail en Candidato.
* [v2.0_data_script.sql](../release-files/2.0/v2.0_data_script.sql): ajusta nombres de parámetros, templates, etc, a la nueva configuración en inglés.

###### Servidor de aplicaciones

* Copiar nuevos módulos a Wildfly para el soporte de archivos .xlsx. Para esto, copiar el contenido del directorio `ansible/roles/elections/files/modules` del repositorio al directorio `<Wildfly>/modules/system/layers/base` de su instalación Wildfly. El módulo jxl ya no es necesario, se puede eliminar borrando el directorio `<Wildfly>/modules/system/layers/base/jxl` de su instalación Wildfly.
* En el archivo de configuración `<Wildfly>/standalone/configuration/standalone.xml`, se debe actualizar el datasource para reflejar el nuevo nombre de la base. Cambiar `elecciones` por `elections` en `connection-url`, `jndi-name` y `pool-name` del datasource.

###### Aplicaciones

* Publicar los nuevos artefactos (puede encontrarlos en `ansible/roles/elections/files/deployments`) y borrar los viejos (`elecciones-ejb.jar`, `elecciones.war`, `elecciones-ws.war`).

#### Tareas posactualización:

Si generó sus propios templates de email, debe regenerarlos ya que las referencias a objectos cambian de nombre. Por ejemplo, si tiene una referencia `$eleccion.tituloIngles`, cambia a `$election.titleEnglish`.


## v1.2 (22/04/2021)

#### Nuevas funcionalidades y correcciones:

* Corrección de dependencia en módulo de servicios.
* Actualización de documentación.


## v1.1 (22/03/2021)

#### Nuevas funcionalidades y correcciones:

* Nueva funcionalidad de Personalización permite customizar el contenido de la home.
* Correcciones menores.

#### Script de actualización de base de datos:

Para la nueva funcionalidad de Personalización, es necesario ejecutar el siguiente script de base de datos:
[v1.1_script.sql](../release-files/1.1/v1.1_script.sql)


## v1.0 (21/01/2021)

#### Destacados:

* Sistema de Elecciones liberado!

#### Funcionalidades:

* Creación y gestión de elecciones.
* Permite auditar y validar el proceso.
* Notificaciones programables.
* Carga y descarga de padrones.
* Elecciones Conjuntas.
