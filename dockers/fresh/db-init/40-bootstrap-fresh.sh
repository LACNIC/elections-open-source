#!/usr/bin/env sh

set -eu

: "${ELECTIONS_DB_USER:?ELECTIONS_DB_USER es requerido}"
: "${FRESH_ADMIN_USER:?FRESH_ADMIN_USER es requerido}"
: "${FRESH_ADMIN_PASSWORD:?FRESH_ADMIN_PASSWORD es requerido}"
: "${FRESH_ADMIN_EMAIL:?FRESH_ADMIN_EMAIL es requerido}"
: "${FRESH_DEFAULT_SENDER:?FRESH_DEFAULT_SENDER es requerido}"
: "${FRESH_EMAIL_HOST:?FRESH_EMAIL_HOST es requerido}"
: "${FRESH_EMAIL_USER:?FRESH_EMAIL_USER es requerido}"
: "${FRESH_EMAIL_PASSWORD:?FRESH_EMAIL_PASSWORD es requerido}"
: "${FRESH_ORGANIZATION_NAME:?FRESH_ORGANIZATION_NAME es requerido}"
: "${FRESH_PUBLIC_BASE_URL:?FRESH_PUBLIC_BASE_URL es requerido}"
: "${FRESH_PUBLIC_NOMINATION_ENABLED:?FRESH_PUBLIC_NOMINATION_ENABLED es requerido}"

case "$FRESH_PUBLIC_NOMINATION_ENABLED" in
  true|false) ;;
  *)
    echo "FRESH_PUBLIC_NOMINATION_ENABLED debe ser true o false" >&2
    exit 1
    ;;
esac

admin_password_hash="$(printf '%s' "$FRESH_ADMIN_PASSWORD" | sha256sum | awk '{print toupper($1)}')"

psql -v ON_ERROR_STOP=1 \
  --username "$POSTGRES_USER" \
  --dbname "$POSTGRES_DB" \
  --set=db_name="$POSTGRES_DB" \
  --set=app_user="$ELECTIONS_DB_USER" \
  --set=admin_user="$FRESH_ADMIN_USER" \
  --set=admin_password_hash="$admin_password_hash" \
  --set=admin_email="$FRESH_ADMIN_EMAIL" \
  --set=default_sender="$FRESH_DEFAULT_SENDER" \
  --set=email_host="$FRESH_EMAIL_HOST" \
  --set=email_user="$FRESH_EMAIL_USER" \
  --set=email_password="$FRESH_EMAIL_PASSWORD" \
  --set=organization_name="$FRESH_ORGANIZATION_NAME" \
  --set=public_base_url="$FRESH_PUBLIC_BASE_URL" \
  --set=public_nomination_enabled="$FRESH_PUBLIC_NOMINATION_ENABLED" <<'SQL'
INSERT INTO public.customization (
    customization_id,
    pic_small_logo,
    pic_big_logo,
    pic_symbol,
    site_title,
    login_title,
    show_home,
    home_html
) VALUES (
    1,
    'logo.png',
    'logo.png',
    'favicon.png',
    :'organization_name',
    :'organization_name',
    true,
    '<h1>Bienvenido al sistema de elecciones</h1><p>Esta instalación usa autenticación local y correo de prueba. Configure una elección para comenzar.</p>'
);

SELECT setval('public.customization_seq', 1, true);

INSERT INTO public.useradmin (useradmin_id, email, password)
VALUES (lower(:'admin_user'), lower(:'admin_email'), :'admin_password_hash');

INSERT INTO public.parameter (key, value) VALUES
	('WS_AUTH_METHOD', 'APP'),
	('AI_TEXT_IMPROVEMENT_ENABLED', 'false'),
	('LOGIN_CAPTCHA_ENABLED', 'false'),
	('PUBLIC_NOMINATION_ENABLED', :'public_nomination_enabled'),
	('DataSiteKeyReCaptcha', ''),
    ('SkGoogleApiReCaptcha', ''),
    ('EMAIL_HOST', :'email_host'),
    ('EMAIL_USER', :'email_user'),
    ('EMAIL_PASSWORD', :'email_password'),
    ('DEFAULT_SENDER', :'default_sender'),
    ('URL', :'public_base_url'),
    ('WEBSITE_DEFAULT', :'public_base_url'),
    ('PUBLIC_ELECTION_LEGACY_MAX_ELECTION_ID', '0'),
    ('WS_AUTH_TOKEN', 'fresh-local-token'),
    ('WS_AUTHORIZED_IPS', '0.0.0.0/0')
ON CONFLICT (key) DO UPDATE SET value = EXCLUDED.value;

DELETE FROM public.parameter
WHERE key = 'WS_LACNIC_AUTH_URL';

GRANT CONNECT ON DATABASE :"db_name" TO :"app_user";
GRANT USAGE ON SCHEMA public TO :"app_user";
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO :"app_user";
GRANT USAGE, SELECT, UPDATE ON ALL SEQUENCES IN SCHEMA public TO :"app_user";
ALTER DEFAULT PRIVILEGES IN SCHEMA public
    GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO :"app_user";
ALTER DEFAULT PRIVILEGES IN SCHEMA public
    GRANT USAGE, SELECT, UPDATE ON SEQUENCES TO :"app_user";

ALTER SCHEMA public OWNER TO :"app_user";
SELECT format('ALTER TABLE %I.%I OWNER TO %I', schemaname, tablename, :'app_user')
FROM pg_tables
WHERE schemaname = 'public'
\gexec
SELECT format('ALTER SEQUENCE %I.%I OWNER TO %I', sequence_schema, sequence_name, :'app_user')
FROM information_schema.sequences
WHERE sequence_schema = 'public'
\gexec
SQL
