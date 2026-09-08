# Post-instalacion del Sistema de Elecciones en Ubuntu

Esta guia cubre la publicacion de una instancia ya levantada con Docker detras de un firewall, un reverse proxy Nginx y TLS con Let's Encrypt.

La forma vigente de instanciar el sistema esta documentada en la carpeta [`dockers`](dockers/) y en la guia de instalacion del sitio. El `docker-compose.yml` actual publica WildFly en el host como `8098:8080`; por eso los ejemplos de Nginx usan `127.0.0.1:8098` como upstream.

Rutas publicadas por la aplicacion:

- Administracion y flujos web: `/elections`
- Servicios REST: `/elections-ws`

## Prerrequisitos

Antes de aplicar esta guia:

- El contenedor `elections` debe estar corriendo.
- La aplicacion debe responder localmente en `http://127.0.0.1:8098/elections`.
- Si se exponen servicios REST, deben responder localmente bajo `http://127.0.0.1:8098/elections-ws`.
- El DNS del dominio publico debe apuntar al servidor donde corre Nginx.
- Los puertos `80` y `443` deben estar disponibles para Nginx.

Ejemplo de DNS usando direcciones reservadas para documentacion:

```text
elecciones.example.net.  86400  IN  A     203.0.113.10
elecciones.example.net.  86400  IN  AAAA  2001:db8::10
```

## Firewall

Instale y configure `ufw` permitiendo SSH desde redes de gestion y trafico web publico hacia Nginx.

```bash
sudo apt update
sudo apt install -y ufw

sudo ufw disable
sudo ufw reset

sudo ufw allow from IP-GESTION-1 to any port 22 proto tcp
sudo ufw allow from IP-GESTION-2 to any port 22 proto tcp
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp

sudo ufw enable
sudo ufw status verbose
```

El puerto `8098` no necesita estar publicado a Internet si Nginx corre en el mismo host. Si el Compose queda escuchando en todas las interfaces, bloquee el acceso externo a ese puerto con firewall o restrinjalo a la interfaz local en la configuracion operativa del ambiente.

## Nginx y certificado TLS

Instale Nginx y Certbot:

```bash
sudo apt update
sudo apt install -y nginx certbot python3-certbot-nginx
```

Defina el dominio publico y el correo operativo:

```bash
MIDOMINIO=elecciones.example.net
EMAIL_ADMIN=admin@example.net
```

Obtenga el certificado:

```bash
sudo certbot certonly --nginx -n --agree-tos -m "$EMAIL_ADMIN" -d "$MIDOMINIO"
sudo ls -la "/etc/letsencrypt/live/$MIDOMINIO/"
```

## Configuracion de Nginx

Cree una configuracion dedicada para la aplicacion:

```bash
sudo tee /etc/nginx/sites-available/elections.conf > /dev/null <<'EOF'
upstream elections_app {
    server 127.0.0.1:8098;
}

server {
    listen 80;
    server_name elecciones.example.net;

    location /.well-known/acme-challenge/ {
        root /var/www/html;
    }

    location / {
        return 301 https://$host$request_uri;
    }
}

server {
    listen 443 ssl http2;
    server_name elecciones.example.net;

    access_log /var/log/nginx/elections.access.log;
    error_log /var/log/nginx/elections.error.log;

    ssl_certificate /etc/letsencrypt/live/elecciones.example.net/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/elecciones.example.net/privkey.pem;
    include /etc/letsencrypt/options-ssl-nginx.conf;
    ssl_dhparam /etc/letsencrypt/ssl-dhparams.pem;

    client_max_body_size 16M;

    location = / {
        return 302 /elections/;
    }

    location = /elections {
        return 301 /elections/;
    }

    location /elections/ {
        proxy_http_version 1.1;
        proxy_redirect off;
        proxy_set_header Host $http_host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_set_header X-NginX-Proxy true;
        proxy_pass http://elections_app;
        gzip on;
        gzip_types text/css application/json application/javascript;
    }

    location /elections-ws/ {
        proxy_http_version 1.1;
        proxy_redirect off;
        proxy_set_header Host $http_host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_pass http://elections_app;
    }
}
EOF
```

Reemplace `elecciones.example.net` por el dominio real:

```bash
sudo sed -i "s/elecciones.example.net/$MIDOMINIO/g" /etc/nginx/sites-available/elections.conf
```

Habilite el sitio y recargue Nginx:

```bash
sudo ln -sf /etc/nginx/sites-available/elections.conf /etc/nginx/sites-enabled/elections.conf
sudo nginx -t
sudo systemctl reload nginx
```

## Renovacion de certificado

Verifique que Certbot tenga renovacion automatica disponible y pruebe el proceso:

```bash
sudo systemctl list-timers | grep certbot
sudo certbot renew --dry-run
```

## Validacion

Despues de aplicar la configuracion:

```bash
curl -I "https://$MIDOMINIO/elections/"
curl -I "https://$MIDOMINIO/elections-ws/hc"
docker logs elections --tail 100
```

Validaciones esperadas:

- Nginx responde en `https://$MIDOMINIO/`.
- La raiz redirige a `/elections/`.
- La aplicacion web carga sin errores de proxy.
- Si los servicios REST estan habilitados, `/elections-ws` llega al WAR de servicios.
- Los logs de WildFly no muestran errores de despliegue ni datasource.
- El header `X-Forwarded-For` llega al backend para que la autenticacion por IP de servicios pueda usar la IP real del cliente.

## Notas operativas

- Mantenga secretos y archivos `.env` fuera del repositorio.
- No use este documento para migrar o crear la base de datos; siga la guia de instalacion o actualizacion segun corresponda.
- Si el reverse proxy esta en otro host, ajuste el upstream y las reglas de firewall para permitir solo el trafico necesario hacia `8098`.
- Si cambia el puerto publicado por Compose, actualice el upstream de Nginx.
