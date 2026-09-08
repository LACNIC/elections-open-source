# Compatibilidad historica y criterios de permanencia

Este documento deja por escrito que partes del codigo, de la configuracion y de la documentacion se conservan intencionalmente para no romper despliegues, migraciones ni datos ya cargados.

La idea es simple: **no todo lo que menciona a LACNIC debe borrarse**. Hay nombres tecnicos que funcionan como contrato estable con base de datos, scripts de carga, EJB, pantallas y pruebas. Esos nombres pueden parecer "antiguos", pero cambiarlos ahora implicaria una migracion amplia y riesgosa.

## Se conserva a proposito

### `net.lacnic.*` en paquetes y clases

Se conserva el namespace historico porque forma parte de:

- nombres completos de clases persistidas;
- referencias en `persistence.xml`;
- pruebas automatizadas;
- contratos internos entre modulos;
- serializacion y trazas historicas.

Renombrarlo no es solo un cambio cosmetico. Implicaria revisar entidades JPA, scripts de despliegue, dependencias entre modulos y posibles datos ya persistidos.

### Claves de parametro historicas

Se conservan claves como:

- `WS_LACNIC_AUTH_URL`
- `MILACNIC_SYNC_*`
- `QUESTION_RECEIVED_LACNIC`
- otros identificadores funcionales que ya viven en base o en scripts de referencia

Estas claves funcionan como interfaz estable entre la aplicacion, la configuracion de ambiente y los datos iniciales. Cambiarlas sin una migracion completa rompe compatibilidad con instalaciones existentes.

### Scripts y archivos de referencia

Se conservan los archivos de referencia y migracion en `release-files/` porque
documentan el recorrido de la aplicacion, permiten actualizar instalaciones
existentes y son los insumos canonicos del perfil Docker para una base nueva.

No deben incorporarse dumps de ambientes reales al repositorio. Los datos de
prueba deben ser ficticios y los secretos deben permanecer fuera de Git.

### Referencias documentales a LACNIC

Se mantienen algunas menciones a LACNIC en la documentacion tecnica cuando explican:

- compatibilidad hacia atras;
- nombres de clave que no conviene cambiar sin migracion;
- origen historico de un modulo o parametro.

Eso no significa que el proyecto siga siendo exclusivo de LACNIC. Significa que se respetan nombres que ya son parte del contrato tecnico.

## Lo que no debe quedar como valor real

Para que el proyecto sea reutilizable por cualquier organizacion, no deben quedar como valores reales:

- credenciales;
- tokens;
- contraseñas;
- rutas de conexion reales;
- URLs privadas de ambientes internos;
- nombres de hosts productivos;
- datos de cuentas de servicio.

Esos datos deben vivir fuera del repositorio, en variables de entorno, archivos locales no versionados o mecanismos de despliegue de cada organizacion.

## Resumen

Se queda todo lo que sea un contrato tecnico existente.

Se neutraliza todo lo que sea valor real, texto institucional o referencia que pueda hacer pensar que el proyecto depende de una sola organizacion.
