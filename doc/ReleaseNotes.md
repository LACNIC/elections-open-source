# Release Notes

## v2.0 (14/07/2021)

#### Destacados:

* Nueva versión disponible!
* Refactor general de código.

#### Recomendación:

Aplicar esta actualización mientras no tenga elecciones activas, ya que, por ejemplo, enlaces enviados vía email ya no serán válidos (contexto de la aplicación cambia de `/elecciones` a `/elections`).

#### Nuevas funcionalidades y correcciones:

* Refactor general del código, que fue traducido a inglés, así como toda la estructura de base de datos.
* Ajuste de funcionalidad para configuración de captcha en modo PROD.
* Corrección de bugs menores.

#### Script de actualización de base de datos:

Se deben aplicar, en orden, los siguientes dos scripts sobre la base de datos:
* [v2.0_ddl_script.sql](../release-files/2.0/v2.0_ddl_script.sql): traducción a inglés, DROP de recursos no usados, nueva secuencia para Comisionados.
* [v2.0_data_script.sql](../release-files/2.0/v2.0_data_script.sql): ajusta nombres de parámetros, templates, etc, a la nueva configuración en inglés.

#### Tareas posactualización:

Si generó sus propios templates de email, debe regenerarlos ya que las referencias a objectos cambian de nombre. Por ejemplo, si tiene una referencia `$eleccion.tituloIngles`, cambia a `$election.titleEnglish`.


## v1.2 (22/04/2021)

#### Destacados:

* Nueva versión disponible!

#### Nuevas funcionalidades y correcciones:

* Corrección de dependencia en módulo de servicios.
* Actualización de documentación.

  
## v1.1 (22/03/2021)

#### Destacados:

* Nueva versión disponible!

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
