# Post instalación del sistema de elecciones en Ubuntu

El sistema de elecciones quedará escuchando en el puerto 8080 de la IP del servidor.

Vamos a instalar y configurar algunos componentes extra para poder segurizar el sistema:

* Firewall
* Proxy reverso
* SSL / [Letsencrypt](https://letsencrypt.org/)

Todo ha sido probado en Ubuntu18+ sin paquetes ni servicios extras y con el sistema de elecciones correctamente instalado.

## Configuración del firewall

```bash
# Instalar dependencias
sudo apt update && sudo apt install -y ufw

# Desactivar y resetear el firewall
ufw disable

ufw reset

# Permitir el acceso SSH desde nuestras IPs de gestion
ufw allow from IP-GESTION-1 to any port 22 proto tcp
ufw allow from IP-GESTION-2 to any port 22 proto tcp
ufw allow from IP-GESTION-N to any port 22 proto tcp

# Permitir el acceso web para el proxy reverso
ufw allow 80
ufw allow 443

# Activar las reglas de firewall
ufw enable
```

## Instalación del proxy reverso y dependencias LetsEncrypt

Para desplegar el proxy reverso y el certificado SSL con este instructivo debemos contar con al menos una IP pública en el server y el puerto 80 y 443 abierto hacia internet.

De igual manera, el DNS del dominio seleccionado para conectarse al sitio debe apuntar hacia la dirección IP de este servidor del sistema de elecciones.

Es decir, si yo quiero instalar el sistema de elecciones en la url `elecciones.midominio.com` en el servidor con IPv4 e IPv6  `198.18.8.8`, `2002:8:8::80` entonces debo tener cargados los siguientes registros en mi DNS.

```c
elecciones.midominio.com 86400 IN A 198.18.8.8
elecciones.midominio.com 86400 IN AAAA 2002:8:8::8080
elecciones.midominio.com 86400 IN TXT "v=spf1 ip4:198.18.8.8 ip6:2002:8:8::80 -all"
```

### Proceso de configuración

```bash
# Instalar dependencias
sudo apt update && apt install -y nginx certbot python3-certbot python3-certbot-nginx

# Ponemos el dominio seleccionado en una variable
MIDOMINIO=elecciones.midominio.com

# Obtener certificado SSL
sudo certbot certonly --nginx -n --agree-tos -m CUENTA-DE-MAIL -d $MIDOMINIO

# Chequear que se obtuvo correctamente el certificado...

ls -la /etc/letsencrypt/live/$MIDOMINIO/ || clear; echo "ERROR: Hubo un problema al obtener el certificado... no se puede seguir con el instructivo hasta arreglar esto."

# Luego procedemos a modificar la config de nginx para usarlo

cp /etc/nginx/sites-available/default /etc/nginx/sites-available/default.backup

cat << EOF > /etc/nginx/sites-available/default
upstream app.local {
    server localhost:8080;
}

server {
    server_name $MIDOMINIO;
    keepalive_timeout 0;
    access_log /var/log/nginx/$MIDOMINIO.access.log;

    location / {
        return 302 /elecciones/;
    }

    location /elecciones/ {
	client_max_body_size 8M;
        proxy_http_version 1.1;
        proxy_redirect off;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header Host \$http_host;
        proxy_set_header X-NginX-Proxy true;
        proxy_pass http://app.local;
        gzip on;
        gzip_types text/css application/json application/javascript;
    }

    listen 443 ssl http2; # managed by Certbot
    ssl_certificate /etc/letsencrypt/live/$MIDOMINIO/fullchain.pem; # managed by Certbot
    ssl_certificate_key /etc/letsencrypt/live/$MIDOMINIO/privkey.pem; # managed by Certbot
    include /etc/letsencrypt/options-ssl-nginx.conf; # managed by Certbot
    ssl_dhparam /etc/letsencrypt/ssl-dhparams.pem; # managed by Certbot
}

server {
    if (\$host = $MIDOMINIO) {
        return 301 https://\$host\$request_uri;
    } # managed by Certbot

    listen 80;
    server_name $MIDOMINIO;
    return 404; # managed by Certbot
}
EOF

nginx -t && systemctl restart nginx
```

Una vez finalizado este procedimiento se puede acceder al sitio de elecciones desde: https://elecciones.midominio.com.