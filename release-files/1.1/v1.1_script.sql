alter table public.personalizacion add titulo_sitio character varying(255) COLLATE pg_catalog."default" NULL;
alter table public.personalizacion add titulo_login character varying(255) COLLATE pg_catalog."default" NULL;
alter table public.personalizacion add show_home boolean;
alter table public.personalizacion add home_html text COLLATE pg_catalog."default";
update public.personalizacion set titulo_sitio = 'Sistema de Elecciones - Lacnic', titulo_login = 'Elecciones LACNIC', show_home = false where id_personalizacion = 1;
ALTER TABLE public.personalizacion ALTER COLUMN titulo_sitio SET NOT NULL;
ALTER TABLE public.personalizacion ALTER COLUMN titulo_login SET NOT NULL;
ALTER TABLE public.personalizacion ALTER COLUMN show_home SET NOT NULL;