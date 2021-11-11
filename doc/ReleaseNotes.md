# Release Notes

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
* Nuevos servicios y autenticación contra servicio de LACNIC.
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
