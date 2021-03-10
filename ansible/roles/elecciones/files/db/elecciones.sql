--
-- PostgreSQL database dump
--

-- Dumped from database version 12.4
-- Dumped by pg_dump version 12.4

-- Started on 2020-12-01 16:40:17

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- TOC entry 202 (class 1259 OID 16394)
-- Name: accesosips; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.accesosips (
    id bigint NOT NULL,
    fechaprimerintento timestamp without time zone NOT NULL,
    fechaultimointento timestamp without time zone NOT NULL,
    intentos integer NOT NULL,
    ip character varying(255) NOT NULL
);


ALTER TABLE public.accesosips OWNER TO postgres;

--
-- TOC entry 203 (class 1259 OID 16397)
-- Name: actividad; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.actividad (
    id_actividad bigint NOT NULL,
    descripcion character varying(20000) NOT NULL,
    ip character varying(255) NOT NULL,
    nomuser character varying(255) NOT NULL,
    tiempo timestamp without time zone NOT NULL,
    tipoactividad character varying(255) NOT NULL,
    ideleccion bigint
);


ALTER TABLE public.actividad OWNER TO postgres;

--
-- TOC entry 204 (class 1259 OID 16403)
-- Name: actividad_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.actividad_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.actividad_seq OWNER TO postgres;

--
-- TOC entry 205 (class 1259 OID 16405)
-- Name: aip_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.aip_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.aip_seq OWNER TO postgres;

--
-- TOC entry 206 (class 1259 OID 16407)
-- Name: auditor; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.auditor (
    id_auditor bigint NOT NULL,
    comisionado boolean,
    expresoconformidad boolean,
    mail character varying(255) NOT NULL,
    nombre character varying(255) NOT NULL,
    tokenresultado character varying(1000),
    id_eleccion bigint NOT NULL,
    habilitarevision boolean,
    idmigracion bigint
);


ALTER TABLE public.auditor OWNER TO postgres;

--
-- TOC entry 207 (class 1259 OID 16413)
-- Name: auditor_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.auditor_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.auditor_seq OWNER TO postgres;

--
-- TOC entry 208 (class 1259 OID 16415)
-- Name: calendario; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.calendario (
    id_calendario bigint NOT NULL,
    fechadeberia timestamp without time zone NOT NULL,
    fechaejecucion timestamp without time zone,
    indice integer NOT NULL,
    realizado boolean NOT NULL,
    tipocalendario character varying(255) NOT NULL,
    id_eleccion bigint NOT NULL
);


ALTER TABLE public.calendario OWNER TO postgres;

--
-- TOC entry 209 (class 1259 OID 16418)
-- Name: calendario_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.calendario_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.calendario_seq OWNER TO postgres;

--
-- TOC entry 210 (class 1259 OID 16420)
-- Name: candidato; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.candidato (
    id_candidato bigint NOT NULL,
    contenidofoto bytea NOT NULL,
    extensionfoto character varying(255) NOT NULL,
    nombre character varying(255) NOT NULL,
    nombrefoto character varying(255) NOT NULL,
    id_eleccion bigint NOT NULL,
    bioespanol character varying(2000),
    bioingles character varying(2000),
    bioportugues character varying(2000),
    orden integer,
    solosp boolean,
    idmigracion bigint,
    linkespanol text,
    linkingles text,
    linkportugues text
);


ALTER TABLE public.candidato OWNER TO postgres;

--
-- TOC entry 211 (class 1259 OID 16426)
-- Name: candidato_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.candidato_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.candidato_seq OWNER TO postgres;

--
-- TOC entry 212 (class 1259 OID 16428)
-- Name: comisionado; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.comisionado (
    id_comisionado bigint NOT NULL,
    mail character varying(255) NOT NULL,
    nombre character varying(255) NOT NULL,
    tokenresultado character varying(255)
);


ALTER TABLE public.comisionado OWNER TO postgres;

--
-- TOC entry 213 (class 1259 OID 16439)
-- Name: eleccion; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.eleccion (
    id_eleccion bigint NOT NULL,
    descripcionespanol character varying(2000) NOT NULL,
    descripcioningles character varying(2000) NOT NULL,
    descripcionportugues character varying(2000) NOT NULL,
    fechacreacion timestamp without time zone NOT NULL,
    fechafin timestamp without time zone NOT NULL,
    fechainicio timestamp without time zone NOT NULL,
    habilitadolinkresultado boolean NOT NULL,
    habilitadolinkvotacion boolean NOT NULL,
    linkespanol character varying(1000) NOT NULL,
    linkingles character varying(1000) NOT NULL,
    linkportugues character varying(1000) NOT NULL,
    maxcandidatos integer NOT NULL,
    tituloespanol character varying(1000) NOT NULL,
    tituloingles character varying(1000) NOT NULL,
    tituloportugues character varying(1000) NOT NULL,
    tokenresultado character varying(2000),
    auxfechafin character varying(255),
    auditoresseteado boolean,
    candidatosseteado boolean,
    padronseteado boolean,
    habilitadolinkauditor boolean NOT NULL,
    solosp boolean,
    remitentepordefecto character varying(255),
    candidatosaleatorios boolean,
    diffutc integer NOT NULL,
    solicitarrevision boolean,
    idmigracion bigint,
    migrada boolean,
    categoria character varying(255)
);


ALTER TABLE public.eleccion OWNER TO postgres;

--
-- TOC entry 214 (class 1259 OID 16445)
-- Name: eleccion_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.eleccion_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.eleccion_seq OWNER TO postgres;

--
-- TOC entry 215 (class 1259 OID 16447)
-- Name: email; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.email (
    id bigint NOT NULL,
    asunto character varying(255),
    bcc character varying(255),
    cc character varying(255),
    cuerpo character varying(8000) NOT NULL,
    desde character varying(255),
    destinatarios character varying(255),
    enviado boolean,
    tipotemplate character varying(255),
    id_eleccion bigint,
    fechacreado timestamp without time zone
);


ALTER TABLE public.email OWNER TO postgres;

--
-- TOC entry 216 (class 1259 OID 16453)
-- Name: emaile_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.emaile_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.emaile_seq OWNER TO postgres;

--
-- TOC entry 217 (class 1259 OID 16455)
-- Name: emailhistorico; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.emailhistorico (
    id bigint NOT NULL,
    asunto character varying(255),
    bcc character varying(255),
    cc character varying(255),
    cuerpo character varying(8000) NOT NULL,
    desde character varying(255),
    destinatarios character varying(255),
    enviado boolean,
    fechacreado timestamp without time zone,
    ideleccion bigint,
    tipotemplate character varying(255)
);


ALTER TABLE public.emailhistorico OWNER TO postgres;

--
-- TOC entry 218 (class 1259 OID 16461)
-- Name: ipinhabilitada; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.ipinhabilitada (
    idipinhabilitada bigint NOT NULL,
    fechaultimointento timestamp without time zone NOT NULL,
    intentos integer NOT NULL,
    ip character varying(255) NOT NULL
);


ALTER TABLE public.ipinhabilitada OWNER TO postgres;

--
-- TOC entry 219 (class 1259 OID 16464)
-- Name: parametro; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.parametro (
    clave character varying(255) NOT NULL,
    valor character varying(255)
);


ALTER TABLE public.parametro OWNER TO postgres;

--
-- TOC entry 229 (class 1259 OID 16724)
-- Name: personalizacion; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.personalizacion (
    id_personalizacion bigint NOT NULL,
    pic_small_logo character varying(255) NOT NULL,
    pic_big_logo character varying(255) NOT NULL,
    pic_simbolo character varying(255) NOT NULL,
    cont_pic_small_logo bytea,
    cont_pic_big_logo bytea,
    cont_pic_simbolo bytea
);


ALTER TABLE public.personalizacion OWNER TO postgres;

--
-- TOC entry 230 (class 1259 OID 16795)
-- Name: personalizacion_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.personalizacion_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.personalizacion_seq OWNER TO postgres;

--
-- TOC entry 220 (class 1259 OID 16470)
-- Name: supraeleccion; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.supraeleccion (
    id bigint NOT NULL,
    idelecciona bigint NOT NULL,
    ideleccionb bigint NOT NULL
);


ALTER TABLE public.supraeleccion OWNER TO postgres;

--
-- TOC entry 221 (class 1259 OID 16473)
-- Name: supraeleccion_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.supraeleccion_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.supraeleccion_seq OWNER TO postgres;

--
-- TOC entry 222 (class 1259 OID 16475)
-- Name: templateeleccion; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.templateeleccion (
    id_template_eleccion bigint NOT NULL,
    asuntoen character varying(255),
    asuntopt character varying(255),
    cuerpoen character varying,
    cuerpoes character varying,
    cuerpopt character varying,
    tipo character varying,
    id_eleccion bigint,
    asuntoes text
);


ALTER TABLE public.templateeleccion OWNER TO postgres;

--
-- TOC entry 223 (class 1259 OID 16481)
-- Name: templateeleccion_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.templateeleccion_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.templateeleccion_seq OWNER TO postgres;

--
-- TOC entry 224 (class 1259 OID 16483)
-- Name: usuario_padron_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.usuario_padron_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.usuario_padron_seq OWNER TO postgres;

--
-- TOC entry 225 (class 1259 OID 16485)
-- Name: usuarioadmin; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.usuarioadmin (
    user_id character varying(255) NOT NULL,
    email text,
    password text,
    id_eleccion_autorizado bigint
);


ALTER TABLE public.usuarioadmin OWNER TO postgres;

--
-- TOC entry 226 (class 1259 OID 16491)
-- Name: usuariopadron; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.usuariopadron (
    id_usuario_padron bigint NOT NULL,
    cantvotos integer NOT NULL,
    idioma character varying(255) NOT NULL,
    mail character varying(255) NOT NULL,
    nombre character varying(2000) NOT NULL,
    orgid character varying(255),
    pais character varying(255),
    tokenvotacion character varying(255),
    yavoto boolean NOT NULL,
    id_eleccion bigint NOT NULL,
    fechavoto timestamp without time zone,
    idmigracion bigint
);


ALTER TABLE public.usuariopadron OWNER TO postgres;

--
-- TOC entry 227 (class 1259 OID 16497)
-- Name: voto; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.voto (
    idvoto bigint NOT NULL,
    codigo character varying(255) NOT NULL,
    fechavoto timestamp without time zone,
    ip character varying(255),
    id_candidato bigint NOT NULL,
    id_eleccion bigint NOT NULL,
    id_usuario_padron bigint NOT NULL
);


ALTER TABLE public.voto OWNER TO postgres;

--
-- TOC entry 228 (class 1259 OID 16503)
-- Name: voto_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.voto_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.voto_seq OWNER TO postgres;

--
-- TOC entry 2966 (class 0 OID 16394)
-- Dependencies: 202
-- Data for Name: accesosips; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.accesosips (id, fechaprimerintento, fechaultimointento, intentos, ip) FROM stdin;
\.


--
-- TOC entry 2967 (class 0 OID 16397)
-- Dependencies: 203
-- Data for Name: actividad; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.actividad (id_actividad, descripcion, ip, nomuser, tiempo, tipoactividad, ideleccion) FROM stdin;
\.


--
-- TOC entry 2970 (class 0 OID 16407)
-- Dependencies: 206
-- Data for Name: auditor; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.auditor (id_auditor, comisionado, expresoconformidad, mail, nombre, tokenresultado, id_eleccion, habilitarevision, idmigracion) FROM stdin;
\.


--
-- TOC entry 2972 (class 0 OID 16415)
-- Dependencies: 208
-- Data for Name: calendario; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.calendario (id_calendario, fechadeberia, fechaejecucion, indice, realizado, tipocalendario, id_eleccion) FROM stdin;
\.


--
-- TOC entry 2974 (class 0 OID 16420)
-- Dependencies: 210
-- Data for Name: candidato; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.candidato (id_candidato, contenidofoto, extensionfoto, nombre, nombrefoto, id_eleccion, bioespanol, bioingles, bioportugues, orden, solosp, idmigracion, linkespanol, linkingles, linkportugues) FROM stdin;
\.


--
-- TOC entry 2976 (class 0 OID 16428)
-- Dependencies: 212
-- Data for Name: comisionado; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.comisionado (id_comisionado, mail, nombre, tokenresultado) FROM stdin;
\.


--
-- TOC entry 2977 (class 0 OID 16439)
-- Dependencies: 213
-- Data for Name: eleccion; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.eleccion (id_eleccion, descripcionespanol, descripcioningles, descripcionportugues, fechacreacion, fechafin, fechainicio, habilitadolinkresultado, habilitadolinkvotacion, linkespanol, linkingles, linkportugues, maxcandidatos, tituloespanol, tituloingles, tituloportugues, tokenresultado, auxfechafin, auditoresseteado, candidatosseteado, padronseteado, habilitadolinkauditor, solosp, remitentepordefecto, candidatosaleatorios, diffutc, solicitarrevision, idmigracion, migrada, categoria) FROM stdin;
\.


--
-- TOC entry 2979 (class 0 OID 16447)
-- Dependencies: 215
-- Data for Name: email; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.email (id, asunto, bcc, cc, cuerpo, desde, destinatarios, enviado, tipotemplate, id_eleccion, fechacreado) FROM stdin;
\.


--
-- TOC entry 2981 (class 0 OID 16455)
-- Dependencies: 217
-- Data for Name: emailhistorico; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.emailhistorico (id, asunto, bcc, cc, cuerpo, desde, destinatarios, enviado, fechacreado, ideleccion, tipotemplate) FROM stdin;
\.


--
-- TOC entry 2982 (class 0 OID 16461)
-- Dependencies: 218
-- Data for Name: ipinhabilitada; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.ipinhabilitada (idipinhabilitada, fechaultimointento, intentos, ip) FROM stdin;
\.


--
-- TOC entry 2983 (class 0 OID 16464)
-- Dependencies: 219
-- Data for Name: parametro; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.parametro (clave, valor) FROM stdin;
WS_IPS_HABILITADAS	127.0.0/24
WS_AUTH_TOKEN	12345678
WEBSITE_DEFAULT	http://server:port/elecciones
EMAIL_CLAVE	mailpwd
EMAIL_USUARIO	mailusr
EMAIL_HOST	mail.server
APP	TEST
URL	http://server:port/
REMITENTE_ESTANDAR	noreply@mail.server
\.


--
-- TOC entry 2993 (class 0 OID 16724)
-- Dependencies: 229
-- Data for Name: personalizacion; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.personalizacion (id_personalizacion, pic_small_logo, pic_big_logo, pic_simbolo, cont_pic_small_logo, cont_pic_big_logo, cont_pic_simbolo) FROM stdin;
1	logo.png	logoLACNIC.png	simbolo-lacnic.png	\N	\N	\N
\.


--
-- TOC entry 2984 (class 0 OID 16470)
-- Dependencies: 220
-- Data for Name: supraeleccion; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.supraeleccion (id, idelecciona, ideleccionb) FROM stdin;
\.


--
-- TOC entry 2986 (class 0 OID 16475)
-- Dependencies: 222
-- Data for Name: templateeleccion; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.templateeleccion (id_template_eleccion, asuntoen, asuntopt, cuerpoen, cuerpoes, cuerpopt, tipo, id_eleccion, asuntoes) FROM stdin;
9						NUEVO	\N	
10	[Elections] $eleccion.tituloIngles	[Eleicoes] $eleccion.tituloPortugues	Estimados, El auditor $auditor.nombre, ha habilitado la revisión de la elección: $eleccion.tituloEspanol	Estimados, El auditor $auditor.nombre, ha habilitado la revisión de la elección: $eleccion.tituloEspanol	Estimados, El auditor $auditor.nombre, ha habilitado la revisión de la elección: $eleccion.tituloEspanol	AUDITOR_REVISION	\N	[Elecciones] $eleccion.tituloEspanol
1	[Elections] $eleccion.tituloIngles	[Eleições] $eleccion.tituloPortugues	Dear $usuario.nombre, \n\nFrom this moment on your link is enabled to participate in \n\n$eleccion.tituloIngles. \n\nYou can now access it below and vote:\n\n$usuario.linkVotar\n\n\n	Estimado/a $usuario.nombre, \n\nEn este momento está activo su enlace para participar en \n\n$eleccion.tituloEspanol\n\na partir de ahora usted podrá acceder al siguiente enlace y votar\n\n$usuario.linkVotar\n\n\n	Prezado(a) $usuario.nombre, \n\nA partir deste momento está ativado o seu link para participar na \n\n$eleccion.tituloPortugues\n\nVocê já pode acessá-lo abaixo e votar:\n\n$usuario.linkVotar\n\n\n	COMIENZO_ELECCION	\N	[Elecciones] $eleccion.tituloEspanol
2	[Elections] $eleccion.tituloIngles	[Eleicoes] $eleccion.tituloPortugues	Dear,\n\n\n\nThe auditor $auditor.nombre, is in agreement with the $eleccion.tituloIngles\n\n\n\n	Estimados,\n\nEl auditor $auditor.nombre, ha indicado su conformidad con $eleccion.tituloEspanol\n\n\n\n	Prezados,\n\n\nO(a) auditor(a) $auditor.nombre, est· de acordo com a $eleccion.tituloPortugues\n\n	AUDITOR_CONFORME	\N	[Elecciones] $eleccion.tituloEspanol
3	[Elections] Audit	[Eleições] Auditoria	Dear $auditor.nombre,\n\n\n\nTo view the results and audit of the votes $eleccion.tituloIngles, please go the following link:\n\n\n\n$auditor.linkResultado\n\n\n\n\n\n\n	Estimado/a $auditor.nombre,\n\nPara visualizar los resultados y auditar los votos de\n\n$eleccion.tituloEspanol\n\nsirvase ingresar en el siguiente enlace\n\n$auditor.linkResultado\n\n\n\n	Prezado(a) $auditor.nombre,\n\nPara visualizar os resultados e auditar os votos da \n\n$eleccion.tituloPortugues, por favor, ingresse ao seguinte link:\n\n$auditor.linkResultado\n\n\n\n\n	AUDITORES	\N	[Elecciones] Auditoria
4	[Elections] Verification Codes	[Eleições] Códigos de verificação	Dear $usuario.nombre,\n\n\n\nThank you for your participation in the $eleccion.tituloIngles\n\n\n\nWe received your vote correctly and we are sending the verification codes below:\n\n\n\nCODE/CANDIDATE\n\n----------------------------------------\n\n$usuario.resumenCodigos\n\n\n\nFrom $eleccion.fechaFinString forward, you will be able to access the following link and see the results of the election\n\n\n\n$eleccion.linkResultado\n\n\n\n\n\n\n\n\n	Estimado/a $usuario.nombre,\n\nGracias por su participación en la $eleccion.tituloEspanol\n\nHemos recibido su voto correctamente. A continuación le enviamos los codigos de verificación.\n\nCODIGO/CANDIDATO\n----------------------------------------\n$usuario.resumenCodigos\n\nA partir del dia $eleccion.fechaFinString podrá acceder al siguiente enlace y ver los resultados de la elección\n\n$eleccion.linkResultado\n\n\n\n	Prezado(a)  $usuario.nombre,\n\n\n\nAgradecemos a sua participação na $eleccion.tituloPortugues.\n\n\n\nRecebemos corretamente o seu voto e a continuação lhe enviamos os códigos de verificação. \n\n\n\nCÓDIGO/CANDIDATO\n\n----------------------------------------\n\n$usuario.resumenCodigos\n\n\n\nA partir do dia $eleccion.fechaFinString você poderá acessar ao seguinte link e ver os resultados da eleição\n\n\n\n$eleccion.linkResultado\n\n\n\n\n\n\n	CODIGOS_VOTACION	\N	[Elecciones] Códigos de verificación
5	[Elections] $eleccion.tituloIngles	[Eleições] $eleccion.tituloPortugues	Dear $usuario.nombre, \n\n\n\nThere is no much more time left to participate in this $eleccion.tituloIngles\n\n\n\nRemember that you can access the following link and vote:\n\n\n\n$usuario.linkVotar\n\n\n\nThe deadline to vote is $eleccion.fechaFinString\n\n\n\n\n\n	Estimado/a $usuario.nombre, \n\nQueda poco tiempo para paticipar en la $eleccion.tituloEspanol\n\nrecuerde que usted puede acceder al siguiente enlace y votar\n\n$usuario.linkVotar\n\nLa fecha limite para votar es $eleccion.fechaFinString\n\n\n\n	Prezado(a) $usuario.nombre, \n\n\n\nResta pouco tempo para participar na $eleccion.tituloPortugues\n\n\n\nLembre-se que vocÍ pode acessar ao seguinte link e votar:\n\n\n\n$usuario.linkVotar\n\n\n\nA data limite para votar È $eleccion.fechaFinString\n\n\n\n\n\n\n	ESTA_POR_FINALIZAR	\N	[Elecciones] $eleccion.tituloEspanol
6			\n--\nLACNIC\nvotaciones@lacnic.net\nCasa de Internet de Latinoamérica y el Caribe\nRambla Rep. de México 6125\n11400 Montevideo-Uruguay\n+598 2604 22 22 \nwww.lacnic.net	\n--\nLACNIC\nvotaciones@lacnic.net\nCasa de Internet de Latinoamérica y el Caribe\nRambla Rep. de México 6125\n11400 Montevideo-Uruguay\n+598 2604 22 22 \nwww.lacnic.net	\n--\nLACNIC\nvotaciones@lacnic.net\nCasa de Internet de Latinoamérica y el Caribe\nRambla Rep. de México 6125\n11400 Montevideo-Uruguay\n+598 2604 22 22 \nwww.lacnic.net	FIRMA	\N	
7	[Elections] $eleccion.tituloIngles	[Eleições] $eleccion.tituloPortugues	Dear $usuario.nombre, \n\n\n\nThe following $eleccion.fechaInicioString the $eleccion.tituloIngles will begin.\n\n\n\nFrom that moment on you will be able to access the link below and vote:\n\n\n\n$usuario.linkVotar\n\n	Estimado/a $usuario.nombre, \n\nEl proximo dia $eleccion.fechaInicioString comenzará la $eleccion.tituloEspanol\n\na partir de ese momento usted podrá acceder al siguiente enlace y votar\n\n$usuario.linkVotar\n\n\n	Prezado(a) $usuario.nombre, \n\n\n\nNo próximo dia $eleccion.fechaInicioString começará a $eleccion.tituloPortugues.\n\n\n\nA partir desse momento você poderá acessar ao seguinte link e votar:\n\n\n\n$usuario.linkVotar\n\n\n\n\n\n	AVISO_ELECCION	\N	[Elecciones] $eleccion.tituloEspanol
8	[Elections] Results	[Eleições] resultados	Dear $usuario.nombre,\n\nTo view the results and verify your votes from $eleccion.tituloIngles, please, go to the following link:\n\n$eleccion.linkResultado\n\n\n\n	Estimado/a $usuario.nombre,\n\nPara visualizar los resultados y verificar sus votos de la $eleccion.tituloEspanol, sirvase ingresar en el siguiente enlace\n\n$eleccion.linkResultado\n\n\n\n\n	Prezado(a) $usuario.nombre,\n\nPara visualizar os resultados e verificar seus votos da $eleccion.tituloPortugues, por favor, ingresse ao seguinte link:\n\n$eleccion.linkResultado\n\n\n\n	RESULTADO_VOTACION	\N	[Elecciones] Resultados
\.


--
-- TOC entry 2989 (class 0 OID 16485)
-- Dependencies: 225
-- Data for Name: usuarioadmin; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.usuarioadmin (user_id, email, password, id_eleccion_autorizado) FROM stdin;
admin	admin@admin.com	21232F297A57A5A743894A0E4A801FC3	0
\.


--
-- TOC entry 2990 (class 0 OID 16491)
-- Dependencies: 226
-- Data for Name: usuariopadron; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.usuariopadron (id_usuario_padron, cantvotos, idioma, mail, nombre, orgid, pais, tokenvotacion, yavoto, id_eleccion, fechavoto, idmigracion) FROM stdin;
\.


--
-- TOC entry 2991 (class 0 OID 16497)
-- Dependencies: 227
-- Data for Name: voto; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.voto (idvoto, codigo, fechavoto, ip, id_candidato, id_eleccion, id_usuario_padron) FROM stdin;
\.


--
-- TOC entry 3000 (class 0 OID 0)
-- Dependencies: 204
-- Name: actividad_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.actividad_seq', 1, false);


--
-- TOC entry 3001 (class 0 OID 0)
-- Dependencies: 205
-- Name: aip_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.aip_seq', 1, true);


--
-- TOC entry 3002 (class 0 OID 0)
-- Dependencies: 207
-- Name: auditor_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.auditor_seq', 1, false);


--
-- TOC entry 3003 (class 0 OID 0)
-- Dependencies: 209
-- Name: calendario_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.calendario_seq', 1, false);


--
-- TOC entry 3004 (class 0 OID 0)
-- Dependencies: 211
-- Name: candidato_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.candidato_seq', 1, false);


--
-- TOC entry 3005 (class 0 OID 0)
-- Dependencies: 214
-- Name: eleccion_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.eleccion_seq', 1, false);


--
-- TOC entry 3006 (class 0 OID 0)
-- Dependencies: 216
-- Name: emaile_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.emaile_seq', 1, false);


--
-- TOC entry 3007 (class 0 OID 0)
-- Dependencies: 230
-- Name: personalizacion_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.personalizacion_seq', 2, false);


--
-- TOC entry 3008 (class 0 OID 0)
-- Dependencies: 221
-- Name: supraeleccion_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.supraeleccion_seq', 1, false);


--
-- TOC entry 3009 (class 0 OID 0)
-- Dependencies: 223
-- Name: templateeleccion_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.templateeleccion_seq', 11, false);


--
-- TOC entry 3010 (class 0 OID 0)
-- Dependencies: 224
-- Name: usuario_padron_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.usuario_padron_seq', 1, false);


--
-- TOC entry 3011 (class 0 OID 0)
-- Dependencies: 228
-- Name: voto_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.voto_seq', 1, false);


--
-- TOC entry 2787 (class 2606 OID 16577)
-- Name: accesosips accesosips_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.accesosips
    ADD CONSTRAINT accesosips_pkey PRIMARY KEY (id);


--
-- TOC entry 2789 (class 2606 OID 16579)
-- Name: actividad actividad_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.actividad
    ADD CONSTRAINT actividad_pkey PRIMARY KEY (id_actividad);


--
-- TOC entry 2791 (class 2606 OID 16581)
-- Name: auditor auditor_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.auditor
    ADD CONSTRAINT auditor_pkey PRIMARY KEY (id_auditor);


--
-- TOC entry 2793 (class 2606 OID 16583)
-- Name: calendario calendario_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.calendario
    ADD CONSTRAINT calendario_pkey PRIMARY KEY (id_calendario);


--
-- TOC entry 2795 (class 2606 OID 16585)
-- Name: candidato candidato_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.candidato
    ADD CONSTRAINT candidato_pkey PRIMARY KEY (id_candidato);


--
-- TOC entry 2797 (class 2606 OID 16587)
-- Name: comisionado comisionado_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.comisionado
    ADD CONSTRAINT comisionado_pkey PRIMARY KEY (id_comisionado);


--
-- TOC entry 2799 (class 2606 OID 16591)
-- Name: eleccion eleccion_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eleccion
    ADD CONSTRAINT eleccion_pkey PRIMARY KEY (id_eleccion);


--
-- TOC entry 2801 (class 2606 OID 16593)
-- Name: email email_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.email
    ADD CONSTRAINT email_pkey PRIMARY KEY (id);


--
-- TOC entry 2803 (class 2606 OID 16595)
-- Name: emailhistorico emailhistorico_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.emailhistorico
    ADD CONSTRAINT emailhistorico_pkey PRIMARY KEY (id);


--
-- TOC entry 2805 (class 2606 OID 16597)
-- Name: ipinhabilitada ipinhabilitada_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.ipinhabilitada
    ADD CONSTRAINT ipinhabilitada_pkey PRIMARY KEY (idipinhabilitada);


--
-- TOC entry 2807 (class 2606 OID 16599)
-- Name: parametro parametro_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.parametro
    ADD CONSTRAINT parametro_pkey PRIMARY KEY (clave);


--
-- TOC entry 2822 (class 2606 OID 16731)
-- Name: personalizacion personalizacion_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.personalizacion
    ADD CONSTRAINT personalizacion_pkey PRIMARY KEY (id_personalizacion);


--
-- TOC entry 2809 (class 2606 OID 16601)
-- Name: supraeleccion supraeleccion_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.supraeleccion
    ADD CONSTRAINT supraeleccion_pkey PRIMARY KEY (id);


--
-- TOC entry 2811 (class 2606 OID 16603)
-- Name: templateeleccion templateeleccion_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.templateeleccion
    ADD CONSTRAINT templateeleccion_pkey PRIMARY KEY (id_template_eleccion);


--
-- TOC entry 2813 (class 2606 OID 16605)
-- Name: usuarioadmin usuarioadmin_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.usuarioadmin
    ADD CONSTRAINT usuarioadmin_pkey PRIMARY KEY (user_id);


--
-- TOC entry 2818 (class 2606 OID 16607)
-- Name: usuariopadron usuariopadron_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.usuariopadron
    ADD CONSTRAINT usuariopadron_pkey PRIMARY KEY (id_usuario_padron);


--
-- TOC entry 2820 (class 2606 OID 16609)
-- Name: voto voto_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.voto
    ADD CONSTRAINT voto_pkey PRIMARY KEY (idvoto);


--
-- TOC entry 2814 (class 1259 OID 16610)
-- Name: indexupall; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX indexupall ON public.usuariopadron USING btree (id_usuario_padron, cantvotos, idioma, mail, nombre, orgid, pais, tokenvotacion, yavoto, id_eleccion);


--
-- TOC entry 2815 (class 1259 OID 16611)
-- Name: indexuptodos; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX indexuptodos ON public.usuariopadron USING btree (id_usuario_padron, cantvotos, idioma, mail, nombre, orgid, pais, tokenvotacion, yavoto, id_eleccion);


--
-- TOC entry 2816 (class 1259 OID 16612)
-- Name: padroneleccion; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX padroneleccion ON public.usuariopadron USING btree (id_eleccion);


--
-- TOC entry 2834 (class 2606 OID 16613)
-- Name: usuariopadron fk1do2ntjih65b8p4dgf7yxpsjj; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.usuariopadron
    ADD CONSTRAINT fk1do2ntjih65b8p4dgf7yxpsjj FOREIGN KEY (id_eleccion) REFERENCES public.eleccion(id_eleccion);


--
-- TOC entry 2836 (class 2606 OID 16618)
-- Name: voto fk1rxcmws4uua549obakp1s7qqu; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.voto
    ADD CONSTRAINT fk1rxcmws4uua549obakp1s7qqu FOREIGN KEY (id_usuario_padron) REFERENCES public.usuariopadron(id_usuario_padron);


--
-- TOC entry 2823 (class 2606 OID 16623)
-- Name: auditor fk3bceb6de890bc135; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.auditor
    ADD CONSTRAINT fk3bceb6de890bc135 FOREIGN KEY (id_eleccion) REFERENCES public.eleccion(id_eleccion);


--
-- TOC entry 2837 (class 2606 OID 16628)
-- Name: voto fk4x75bejx7sq67bk5tajblmrhf; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.voto
    ADD CONSTRAINT fk4x75bejx7sq67bk5tajblmrhf FOREIGN KEY (id_candidato) REFERENCES public.candidato(id_candidato);


--
-- TOC entry 2824 (class 2606 OID 16633)
-- Name: auditor fk53knnkifx5fqgm1jgcheuwsyo; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.auditor
    ADD CONSTRAINT fk53knnkifx5fqgm1jgcheuwsyo FOREIGN KEY (id_eleccion) REFERENCES public.eleccion(id_eleccion);


--
-- TOC entry 2832 (class 2606 OID 16638)
-- Name: templateeleccion fk60dab3b6rn3m1y85b3hvqdpkv; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.templateeleccion
    ADD CONSTRAINT fk60dab3b6rn3m1y85b3hvqdpkv FOREIGN KEY (id_eleccion) REFERENCES public.eleccion(id_eleccion);


--
-- TOC entry 2827 (class 2606 OID 16643)
-- Name: candidato fk7sa6ixqcdhp53qkjrwituhwct; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.candidato
    ADD CONSTRAINT fk7sa6ixqcdhp53qkjrwituhwct FOREIGN KEY (id_eleccion) REFERENCES public.eleccion(id_eleccion);


--
-- TOC entry 2828 (class 2606 OID 16648)
-- Name: candidato fk95c3b56d890bc135; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.candidato
    ADD CONSTRAINT fk95c3b56d890bc135 FOREIGN KEY (id_eleccion) REFERENCES public.eleccion(id_eleccion);


--
-- TOC entry 2833 (class 2606 OID 16653)
-- Name: templateeleccion fk96wiao69tqccwuhdextjjx7a2; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.templateeleccion
    ADD CONSTRAINT fk96wiao69tqccwuhdextjjx7a2 FOREIGN KEY (id_eleccion) REFERENCES public.eleccion(id_eleccion);


--
-- TOC entry 2829 (class 2606 OID 16658)
-- Name: candidato fk9728k0jhg0l5qw3folv2smh34; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.candidato
    ADD CONSTRAINT fk9728k0jhg0l5qw3folv2smh34 FOREIGN KEY (id_eleccion) REFERENCES public.eleccion(id_eleccion);


--
-- TOC entry 2825 (class 2606 OID 16663)
-- Name: auditor fkene41r5h8dgxtaa0hka4ao8e8; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.auditor
    ADD CONSTRAINT fkene41r5h8dgxtaa0hka4ao8e8 FOREIGN KEY (id_eleccion) REFERENCES public.eleccion(id_eleccion);


--
-- TOC entry 2830 (class 2606 OID 16668)
-- Name: email fkf3wiqomx3ab7qo9353qblqgy5; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.email
    ADD CONSTRAINT fkf3wiqomx3ab7qo9353qblqgy5 FOREIGN KEY (id_eleccion) REFERENCES public.eleccion(id_eleccion);


--
-- TOC entry 2838 (class 2606 OID 16673)
-- Name: voto fkiwnuc9dco1kvcsj4p0004mvmr; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.voto
    ADD CONSTRAINT fkiwnuc9dco1kvcsj4p0004mvmr FOREIGN KEY (id_eleccion) REFERENCES public.eleccion(id_eleccion);


--
-- TOC entry 2839 (class 2606 OID 16678)
-- Name: voto fkj0kgemrk6tfc3n4nw6smlu9y3; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.voto
    ADD CONSTRAINT fkj0kgemrk6tfc3n4nw6smlu9y3 FOREIGN KEY (id_eleccion) REFERENCES public.eleccion(id_eleccion);


--
-- TOC entry 2835 (class 2606 OID 16683)
-- Name: usuariopadron fkn8xln3ppeqiq1d9i6hggnybln; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.usuariopadron
    ADD CONSTRAINT fkn8xln3ppeqiq1d9i6hggnybln FOREIGN KEY (id_eleccion) REFERENCES public.eleccion(id_eleccion);


--
-- TOC entry 2826 (class 2606 OID 16688)
-- Name: calendario fkoka6ievrhsn86qre5cu4beg14; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.calendario
    ADD CONSTRAINT fkoka6ievrhsn86qre5cu4beg14 FOREIGN KEY (id_eleccion) REFERENCES public.eleccion(id_eleccion);


--
-- TOC entry 2831 (class 2606 OID 16693)
-- Name: email fkpntyqelk43ka0s3ffg16p2sms; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.email
    ADD CONSTRAINT fkpntyqelk43ka0s3ffg16p2sms FOREIGN KEY (id_eleccion) REFERENCES public.eleccion(id_eleccion);


-- Completed on 2020-12-01 16:40:18

--
-- PostgreSQL database dump complete
--

