## Release Notes

### v1.1 (22/03/2021)

#### Destacados

* Nueva versión disponible!

#### Nuevas funcionalidades y correcciones:

* Nueva funcionalidad de Personalización permite customizar el contenido de la home.
* Correcciones menores.

#### Script de actualización de base de datos

Para la nueva funcionalidad de Personalización, es necesario aplicar los siguientes cambios en la base de datos:

```sql
alter table public.personalizacion add titulo_sitio character varying(255) COLLATE pg_catalog."default" NULL;
alter table public.personalizacion add titulo_login character varying(255) COLLATE pg_catalog."default" NULL;
alter table public.personalizacion add show_home boolean;
alter table public.personalizacion add home_html text COLLATE pg_catalog."default";
update public.personalizacion set titulo_sitio = 'Sistema de Elecciones - Lacnic', titulo_login = 'Elecciones LACNIC', show_home = false where id_personalizacion = 1;
```

### v1.0 (21/01/2021)

#### Destacados

* Sistema de Elecciones liberado!

#### Funcionalidades:

* Creación y gestión de elecciones.
* Permite auditar y validar el proceso.
* Notificaciones programables.
* Carga y descarga de padrones.
* Elecciones Conjuntas.
