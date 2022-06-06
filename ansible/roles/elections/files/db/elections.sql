--
-- PostgreSQL database dump
--

-- Dumped from database version 12.4
-- Dumped by pg_dump version 12.4

-- Started on 2021-07-13 13:12:59
CREATE DATABASE elections;

USE elections;
--
-- TOC entry 203 (class 1259 OID 17684)
-- Name: activity; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE activity (
    activity_id bigint NOT NULL,
    description character varying(20000) NOT NULL,
    ip character varying(255) NOT NULL,
    username character varying(255) NOT NULL,
    "timestamp" timestamp without time zone NOT NULL,
    activitytype character varying(255) NOT NULL,
    election_id bigint
);


ALTER TABLE activity OWNER TO postgres;

--
-- TOC entry 204 (class 1259 OID 17690)
-- Name: activity_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE activity_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE activity_seq OWNER TO postgres;

--
-- TOC entry 206 (class 1259 OID 17694)
-- Name: auditor; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE auditor (
    auditor_id bigint NOT NULL,
    commissioner boolean,
    agreedconformity boolean,
    mail character varying(255) NOT NULL,
    name character varying(255) NOT NULL,
    resulttoken character varying(1000),
    election_id bigint NOT NULL,
    revisionavailable boolean,
    migration_id bigint
);


ALTER TABLE auditor OWNER TO postgres;

--
-- TOC entry 207 (class 1259 OID 17700)
-- Name: auditor_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE auditor_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE auditor_seq OWNER TO postgres;

--
-- TOC entry 208 (class 1259 OID 17707)
-- Name: candidate; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE candidate (
    candidate_id bigint NOT NULL,
    pictureinfo bytea NOT NULL,
    pictureextension character varying(255) NOT NULL,
    name character varying(255) NOT NULL,
    picturename character varying(255) NOT NULL,
    election_id bigint NOT NULL,
    biospanish character varying(2000),
    bioenglish character varying(2000),
    bioportuguese character varying(2000),
    candidateorder integer,
    onlysp boolean,
    migration_id bigint,
    linkspanish text,
    linkenglish text,
    linkportuguese text,
	mail character varying(255)
);


ALTER TABLE candidate OWNER TO postgres;

--
-- TOC entry 209 (class 1259 OID 17713)
-- Name: candidate_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE candidate_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE candidate_seq OWNER TO postgres;

--
-- TOC entry 210 (class 1259 OID 17715)
-- Name: commissioner; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE commissioner (
    commissioner_id bigint NOT NULL,
    mail character varying(255) NOT NULL,
    name character varying(255) NOT NULL,
    resulttoken character varying(255)
);


ALTER TABLE commissioner OWNER TO postgres;

--
-- TOC entry 228 (class 1259 OID 17920)
-- Name: commissioner_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE commissioner_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE commissioner_seq OWNER TO postgres;

--
-- TOC entry 217 (class 1259 OID 17752)
-- Name: customization; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE customization (
    customization_id bigint NOT NULL,
    pic_small_logo character varying(255) NOT NULL,
    pic_big_logo character varying(255) NOT NULL,
    pic_symbol character varying(255) NOT NULL,
    cont_pic_small_logo bytea,
    cont_pic_big_logo bytea,
    cont_pic_symbol bytea,
    site_title character varying(255) NOT NULL,
    login_title character varying(255) NOT NULL,
    show_home boolean NOT NULL,
    home_html text
);


ALTER TABLE customization OWNER TO postgres;

--
-- TOC entry 218 (class 1259 OID 17758)
-- Name: customization_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE customization_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE customization_seq OWNER TO postgres;

--
-- TOC entry 211 (class 1259 OID 17721)
-- Name: election; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE election (
    election_id bigint NOT NULL,
    descriptionspanish character varying(2000) NOT NULL,
    descriptionenglish character varying(2000) NOT NULL,
    descriptionportuguese character varying(2000) NOT NULL,
    creationdate timestamp without time zone NOT NULL,
    enddate timestamp without time zone NOT NULL,
    startdate timestamp without time zone NOT NULL,
    resultlinkavailable boolean NOT NULL,
    votinglinkavailable boolean NOT NULL,
    linkspanish character varying(1000) NOT NULL,
    linkenglish character varying(1000) NOT NULL,
    linkportuguese character varying(1000) NOT NULL,
    maxcandidates integer NOT NULL,
    titlespanish character varying(1000) NOT NULL,
    titleenglish character varying(1000) NOT NULL,
    titleportuguese character varying(1000) NOT NULL,
    resulttoken character varying(2000),
    auditorsset boolean,
    candidatesset boolean,
    electorsset boolean,
    auditorlinkavailable boolean NOT NULL,
    onlysp boolean,
    defaultsender character varying(255),
    randomordercandidates boolean,
    diffutc integer NOT NULL,
    revisionrequest boolean,
    migration_id bigint,
    migrated boolean,
    category character varying(255),
    closed boolean NOT NULL,
    closeddate timestamp without time zone
);


ALTER TABLE election OWNER TO postgres;

--
-- TOC entry 212 (class 1259 OID 17727)
-- Name: election_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE election_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE election_seq OWNER TO postgres;

--
-- TOC entry 221 (class 1259 OID 17765)
-- Name: electionemailtemplate; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE electionemailtemplate (
    electionemailtemplate_id bigint NOT NULL,
    subjecten character varying(255),
    subjectpt character varying(255),
    bodyen character varying,
    bodysp character varying,
    bodypt character varying,
    type character varying,
    election_id bigint,
    subjectsp text
);


ALTER TABLE electionemailtemplate OWNER TO postgres;

--
-- TOC entry 222 (class 1259 OID 17771)
-- Name: electionemailtemplate_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE electionemailtemplate_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE electionemailtemplate_seq OWNER TO postgres;

--
-- TOC entry 213 (class 1259 OID 17729)
-- Name: email; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE email (
    email_id bigint NOT NULL,
    subject character varying(255),
    bcc character varying(255),
    cc character varying(255),
    body character varying(8000) NOT NULL,
    sender character varying(255),
    recipients character varying(255),
    sent boolean,
    templatetype character varying(255),
    election_id bigint,
    createddate timestamp without time zone
);


ALTER TABLE email OWNER TO postgres;

--
-- TOC entry 214 (class 1259 OID 17735)
-- Name: email_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE email_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE email_seq OWNER TO postgres;

--
-- TOC entry 215 (class 1259 OID 17737)
-- Name: emailhistory; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE emailhistory (
    emailhistory_id bigint NOT NULL,
    subject character varying(255),
    bcc character varying(255),
    cc character varying(255),
    body character varying(8000) NOT NULL,
    sender character varying(255),
    recipients character varying(255),
    sent boolean,
    createddate timestamp without time zone,
    election_id bigint,
    templatetype character varying(255)
);


ALTER TABLE emailhistory OWNER TO postgres;

--
-- TOC entry 202 (class 1259 OID 17681)
-- Name: ipaccess; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE ipaccess (
    ipaccess_id bigint NOT NULL,
    firstattemptdate timestamp without time zone NOT NULL,
    lastattemptdate timestamp without time zone NOT NULL,
    attemptcount integer NOT NULL,
    ip character varying(255) NOT NULL
);


ALTER TABLE ipaccess OWNER TO postgres;

--
-- TOC entry 205 (class 1259 OID 17692)
-- Name: ipaccess_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE ipaccess_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE ipaccess_seq OWNER TO postgres;

--
-- TOC entry 219 (class 1259 OID 17760)
-- Name: jointelection; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE jointelection (
    jointelection_id bigint NOT NULL,
    electiona_id bigint NOT NULL,
    electionb_id bigint NOT NULL
);


ALTER TABLE jointelection OWNER TO postgres;

--
-- TOC entry 220 (class 1259 OID 17763)
-- Name: jointelection_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE jointelection_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE jointelection_seq OWNER TO postgres;

--
-- TOC entry 216 (class 1259 OID 17746)
-- Name: parameter; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE parameter (
    key character varying(255) NOT NULL,
    value character varying(255)
);


ALTER TABLE parameter OWNER TO postgres;

--
-- TOC entry 224 (class 1259 OID 17775)
-- Name: useradmin; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE useradmin (
    useradmin_id character varying(255) NOT NULL,
    email text,
    password text,
    authorizedelection_id bigint
);


ALTER TABLE useradmin OWNER TO postgres;

--
-- TOC entry 225 (class 1259 OID 17781)
-- Name: uservoter; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE uservoter (
    uservoter_id bigint NOT NULL,
    voteamount integer NOT NULL,
    language character varying(255) NOT NULL,
    mail character varying(255) NOT NULL,
    name character varying(2000) NOT NULL,
    orgid character varying(255),
    country character varying(255),
    votetoken character varying(255),
    voted boolean NOT NULL,
    election_id bigint NOT NULL,
    votedate timestamp without time zone,
    migration_id bigint
);


ALTER TABLE uservoter OWNER TO postgres;

--
-- TOC entry 223 (class 1259 OID 17773)
-- Name: uservoter_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE uservoter_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE uservoter_seq OWNER TO postgres;

--
-- TOC entry 226 (class 1259 OID 17787)
-- Name: vote; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE vote (
    vote_id bigint NOT NULL,
    code character varying(255) NOT NULL,
    votedate timestamp without time zone,
    ip character varying(255),
    candidate_id bigint NOT NULL,
    election_id bigint NOT NULL,
    uservoter_id bigint
);


ALTER TABLE vote OWNER TO postgres;

--
-- TOC entry 227 (class 1259 OID 17793)
-- Name: vote_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE vote_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE vote_seq OWNER TO postgres;

--
-- TOC entry 2954 (class 0 OID 17684)
-- Dependencies: 203
-- Data for Name: activity; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY activity (activity_id, description, ip, username, "timestamp", activitytype, election_id) FROM stdin;
\.


--
-- TOC entry 2957 (class 0 OID 17694)
-- Dependencies: 206
-- Data for Name: auditor; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY auditor (auditor_id, commissioner, agreedconformity, mail, name, resulttoken, election_id, revisionavailable, migration_id) FROM stdin;
\.


--
-- TOC entry 2959 (class 0 OID 17707)
-- Dependencies: 208
-- Data for Name: candidate; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY candidate (candidate_id, pictureinfo, pictureextension, name, picturename, election_id, biospanish, bioenglish, bioportuguese, candidateorder, onlysp, migration_id, linkspanish, linkenglish, linkportuguese) FROM stdin;
\.


--
-- TOC entry 2961 (class 0 OID 17715)
-- Dependencies: 210
-- Data for Name: commissioner; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY commissioner (commissioner_id, mail, name, resulttoken) FROM stdin;
\.


--
-- TOC entry 2968 (class 0 OID 17752)
-- Dependencies: 217
-- Data for Name: customization; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY customization (customization_id, pic_small_logo, pic_big_logo, pic_symbol, cont_pic_small_logo, cont_pic_big_logo, cont_pic_symbol, site_title, login_title, show_home, home_html) FROM stdin;
1	logo.png	logoLACNIC.png	simbolo-lacnic.png	\N	\N	\N	Sistema de Elecciones - Lacnic	Elecciones LACNIC	f	\N
\.


--
-- TOC entry 2962 (class 0 OID 17721)
-- Dependencies: 211
-- Data for Name: election; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY election (election_id, descriptionspanish, descriptionenglish, descriptionportuguese, creationdate, enddate, startdate, resultlinkavailable, votinglinkavailable, linkspanish, linkenglish, linkportuguese, maxcandidates, titlespanish, titleenglish, titleportuguese, resulttoken, auditorsset, candidatesset, electorsset, auditorlinkavailable, onlysp, defaultsender, randomordercandidates, diffutc, revisionrequest, migration_id, migrated, category) FROM stdin;
\.


--
-- TOC entry 2972 (class 0 OID 17765)
-- Dependencies: 221
-- Data for Name: electionemailtemplate; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY electionemailtemplate (electionemailtemplate_id, subjecten, subjectpt, bodyen, bodysp, bodypt, type, election_id, subjectsp) FROM stdin;
9						NEW	\N	
6			\n--\nLACNIC\nvotaciones@lacnic.net\nCasa de Internet de Latinoamérica y el Caribe\nRambla Rep. de México 6125\n11400 Montevideo-Uruguay\n+598 2604 22 22 \nwww.lacnic.net	\n--\nLACNIC\nvotaciones@lacnic.net\nCasa de Internet de Latinoamérica y el Caribe\nRambla Rep. de México 6125\n11400 Montevideo-Uruguay\n+598 2604 22 22 \nwww.lacnic.net	\n--\nLACNIC\nvotaciones@lacnic.net\nCasa de Internet de Latinoamérica y el Caribe\nRambla Rep. de México 6125\n11400 Montevideo-Uruguay\n+598 2604 22 22 \nwww.lacnic.net	SIGNATURE	\N	
10	[Elections] $election.titleEnglish	[Eleições] $election.titlePortuguese	Dear,\n\nThe auditor $auditor.name, has enabled revision for election: $election.titleEnglish	Estimados,\n\nEl auditor $auditor.name, ha habilitado la revisión de la elección: $election.titleSpanish	Prezados,\n\nNo(a) auditor(a) $auditor.name, habilitou a revisão da eleição: $election.titlePortuguese	AUDITOR_REVISION	\N	[Elecciones] $election.titleSpanish
1	[Elections] $election.titleEnglish	[Eleições] $election.titlePortuguese	Dear $user.name,\n\nFrom this moment on your link is enabled to participate in \n\n$election.titleEnglish.\n\nYou can now access it below and vote:\n\n$user.voteLink\n\n\n	Estimado/a $user.name,\n\nEn este momento está activo su enlace para participar en \n\n$election.titleSpanish\n\nA partir de ahora usted podrá acceder al siguiente enlace y votar\n\n$user.voteLink\n\n\n	Prezado(a) $user.name,\n\nA partir deste momento está ativado o seu link para participar na \n\n$election.titlePortuguese\n\nVocê já pode acessá-lo abaixo e votar:\n\n$user.voteLink\n\n\n	ELECTION_START	\N	[Elecciones] $election.titleSpanish
2	[Elections] $election.titleEnglish	[Eleições] $election.titlePortuguese	Dear,\n\nThe auditor $auditor.name, is in agreement with the $election.titleEnglish\n\n	Estimados,\n\nEl auditor $auditor.name, ha indicado su conformidad con $election.titleSpanish\n\n	Prezados,\n\nO(a) auditor(a) $auditor.name, está de acordo com a $election.titlePortuguese\n\n	AUDITOR_AGREEMENT	\N	[Elecciones] $election.titleSpanish
3	[Elections] Audit	[Eleições] Auditoria	Dear $auditor.name,\n\nTo view the results and audit the votes of\n\n$election.titleEnglish\n\nPlease go to the following link:\n\n$auditor.resultLink\n\n	Estimado/a $auditor.name,\n\nPara visualizar los resultados y auditar los votos de\n\n$election.titleSpanish\n\nPor favor, ingresar en el siguiente enlace\n\n$auditor.resultLink\n\n	Prezado(a) $auditor.name,\n\nPara visualizar os resultados e auditar os votos da\n\n$election.titlePortuguese\n\nPor favor, ingresse ao seguinte link:\n\n$auditor.resultLink\n\n	AUDITOR	\N	[Elecciones] Auditoría
8	[Elections] Results	[Eleições] Resultados	Dear $user.name,\n\nTo view the results and verify your votes from $election.titleEnglish, please go to the following link:\n\n$election.resultLink\n\n	Estimado/a $user.name,\n\nPara visualizar los resultados y verificar sus votos de la $election.titleSpanish, por favor ingrese en el siguiente enlace:\n\n$election.resultLink\n\n	Prezado(a) $user.name,\n\nPara visualizar os resultados e verificar seus votos da $election.titlePortuguese, por favor ingresse ao seguinte link:\n\n$election.resultLink\n\n	VOTE_RESULT	\N	[Elecciones] Resultados
4	[Elections] Verification Codes	[Eleições] Códigos de verificação	Dear $user.name,\n\nThank you for your participation in the $election.titleEnglish\n\nWe received your vote correctly and we are sending the verification codes below:\n\nCODE/CANDIDATE\n\n----------------------------------------\n\n$user.codesSummary\n\nFrom $election.endDateString forward, you will be able to access the following link and see the results of the election:\n\n$election.resultLink\n\n	Estimado/a $user.name,\n\nGracias por su participación en la $election.titleSpanish\n\nHemos recibido su voto correctamente y a continuación le enviamos los codigos de verificación:\n\nCODIGO/CANDIDATO\n\n----------------------------------------\n\n$user.codesSummary\n\nA partir del día $election.endDateString podrá acceder al siguiente enlace y ver los resultados de la elección:\n\n$election.resultLink\n\n	Prezado(a)  $user.name,\n\nAgradecemos a sua participação na $election.titlePortuguese.\n\nRecebemos corretamente o seu voto e a continuação lhe enviamos os códigos de verificação:\n\nCÓDIGO/CANDIDATO\n\n----------------------------------------\n\n$user.codesSummary\n\nA partir do dia $election.endDateString você poderá acessar ao seguinte link e ver os resultados da eleição:\n\n$election.resultLink\n\n	VOTE_CODES	\N	[Elecciones] Códigos de verificación
5	[Elections] $election.titleEnglish	[Eleições] $election.titlePortuguese	Dear $user.name,\n\nThere is no much more time left to participate in this $election.titleEnglish\n\nRemember that you can access the following link and vote:\n\n$user.voteLink\n\nThe deadline to vote is $election.endDateString\n\n	Estimado/a $user.name,\n\nQueda poco tiempo para paticipar en la $election.titleSpanish\n\nRecuerde que usted puede acceder al siguiente enlace y votar:\n\n$user.voteLink\n\nLa fecha limite para votar es $election.endDateString\n\n	Prezado(a) $user.name,\n\nResta pouco tempo para participar na $election.titlePortuguese\n\nLembre-se que vocÍ pode acessar ao seguinte link e votar:\n\n$user.voteLink\n\nA data limite para votar è $election.endDateString\n\n	ELECTION_ABOUT_TO_END	\N	[Elecciones] $election.titleSpanish
7	[Elections] $election.titleEnglish	[Eleições] $election.titlePortuguese	Dear $user.name,\n\nThe following $election.startDateString the $election.titleEnglish will begin.\n\nFrom that moment on you will be able to access the link below and vote:\n\n$user.voteLink\n\n	Estimado/a $user.name,\n\nEl próximo día $election.startDateString comenzará la $election.titleSpanish.\n\nA partir de ese momento usted podrá acceder al siguiente enlace y votar:\n\n$user.voteLink\n\n	Prezado(a) $user.name,\n\nNo próximo dia $election.startDateString começará a $election.titlePortuguese.\n\nA partir desse momento você poderá acessar ao seguinte link e votar:\n\n$user.voteLink\n\n	ELECTION_NOTICE	\N	[Elecciones] $election.titleSpanish
\.


--
-- TOC entry 2964 (class 0 OID 17729)
-- Dependencies: 213
-- Data for Name: email; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY email (email_id, subject, bcc, cc, body, sender, recipients, sent, templatetype, election_id, createddate) FROM stdin;
\.


--
-- TOC entry 2966 (class 0 OID 17737)
-- Dependencies: 215
-- Data for Name: emailhistory; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY emailhistory (emailhistory_id, subject, bcc, cc, body, sender, recipients, sent, createddate, election_id, templatetype) FROM stdin;
\.


--
-- TOC entry 2953 (class 0 OID 17681)
-- Dependencies: 202
-- Data for Name: ipaccess; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY ipaccess (ipaccess_id, firstattemptdate, lastattemptdate, attemptcount, ip) FROM stdin;
\.


--
-- TOC entry 2970 (class 0 OID 17760)
-- Dependencies: 219
-- Data for Name: jointelection; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY jointelection (jointelection_id, electiona_id, electionb_id) FROM stdin;
\.


--
-- TOC entry 2967 (class 0 OID 17746)
-- Dependencies: 216
-- Data for Name: parameter; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY parameter (key, value) FROM stdin;
WS_AUTH_TOKEN	12345678
WEBSITE_DEFAULT	http://server:port/elections
EMAIL_HOST	mail.server
APP	TEST
URL	http://server:port/
DEFAULT_SENDER	noreply@mail.server
WS_AUTHORIZED_IPS	127.0.0/24
EMAIL_PASSWORD	mailpwd
EMAIL_USER	mailusr
WS_AUTH_METHOD	APP
WS_LACNIC_AUTH_URL	https://pai.lacnic.net/portal-ws/authorization
WS_MAX_PAGE_SIZE	50
\.


--
-- TOC entry 2975 (class 0 OID 17775)
-- Dependencies: 224
-- Data for Name: useradmin; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY useradmin (useradmin_id, email, password, authorizedelection_id) FROM stdin;
admin	admin@admin.com	21232F297A57A5A743894A0E4A801FC3	0
\.


--
-- TOC entry 2976 (class 0 OID 17781)
-- Dependencies: 225
-- Data for Name: uservoter; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY uservoter (uservoter_id, voteamount, language, mail, name, orgid, country, votetoken, voted, election_id, votedate, migration_id) FROM stdin;
\.


--
-- TOC entry 2977 (class 0 OID 17787)
-- Dependencies: 226
-- Data for Name: vote; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY vote (vote_id, code, votedate, ip, candidate_id, election_id, uservoter_id) FROM stdin;
\.


--
-- TOC entry 2985 (class 0 OID 0)
-- Dependencies: 204
-- Name: activity_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('activity_seq', 1, false);


--
-- TOC entry 2986 (class 0 OID 0)
-- Dependencies: 207
-- Name: auditor_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('auditor_seq', 1, false);


--
-- TOC entry 2987 (class 0 OID 0)
-- Dependencies: 209
-- Name: candidate_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('candidate_seq', 1, false);


--
-- TOC entry 2988 (class 0 OID 0)
-- Dependencies: 228
-- Name: commissioner_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('commissioner_seq', 1, false);


--
-- TOC entry 2989 (class 0 OID 0)
-- Dependencies: 218
-- Name: customization_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('customization_seq', 2, false);


--
-- TOC entry 2990 (class 0 OID 0)
-- Dependencies: 212
-- Name: election_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('election_seq', 1, false);


--
-- TOC entry 2991 (class 0 OID 0)
-- Dependencies: 222
-- Name: electionemailtemplate_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('electionemailtemplate_seq', 11, false);


--
-- TOC entry 2992 (class 0 OID 0)
-- Dependencies: 214
-- Name: email_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('email_seq', 1, false);


--
-- TOC entry 2993 (class 0 OID 0)
-- Dependencies: 205
-- Name: ipaccess_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('ipaccess_seq', 1, true);


--
-- TOC entry 2994 (class 0 OID 0)
-- Dependencies: 220
-- Name: jointelection_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('jointelection_seq', 1, false);


--
-- TOC entry 2995 (class 0 OID 0)
-- Dependencies: 223
-- Name: uservoter_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('uservoter_seq', 1, false);


--
-- TOC entry 2996 (class 0 OID 0)
-- Dependencies: 227
-- Name: vote_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('vote_seq', 1, false);


--
-- TOC entry 2779 (class 2606 OID 17796)
-- Name: ipaccess accesosips_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY ipaccess
    ADD CONSTRAINT ipaccess_pkey PRIMARY KEY (ipaccess_id);


--
-- TOC entry 2781 (class 2606 OID 17798)
-- Name: activity actividad_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY activity
    ADD CONSTRAINT activity_pkey PRIMARY KEY (activity_id);


--
-- TOC entry 2783 (class 2606 OID 17800)
-- Name: auditor auditor_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY auditor
    ADD CONSTRAINT auditor_pkey PRIMARY KEY (auditor_id);


--
-- TOC entry 2785 (class 2606 OID 17804)
-- Name: candidate candidato_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY candidate
    ADD CONSTRAINT candidate_pkey PRIMARY KEY (candidate_id);


--
-- TOC entry 2787 (class 2606 OID 17806)
-- Name: commissioner comisionado_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY commissioner
    ADD CONSTRAINT commissioner_pkey PRIMARY KEY (commissioner_id);


--
-- TOC entry 2789 (class 2606 OID 17808)
-- Name: election eleccion_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY election
    ADD CONSTRAINT election_pkey PRIMARY KEY (election_id);


--
-- TOC entry 2791 (class 2606 OID 17810)
-- Name: email email_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY email
    ADD CONSTRAINT email_pkey PRIMARY KEY (email_id);


--
-- TOC entry 2793 (class 2606 OID 17812)
-- Name: emailhistory emailhistorico_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY emailhistory
    ADD CONSTRAINT emailhistory_pkey PRIMARY KEY (emailhistory_id);


--
-- TOC entry 2795 (class 2606 OID 17816)
-- Name: parameter parametro_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY parameter
    ADD CONSTRAINT parameter_pkey PRIMARY KEY (key);


--
-- TOC entry 2797 (class 2606 OID 17818)
-- Name: customization personalizacion_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY customization
    ADD CONSTRAINT customization_pkey PRIMARY KEY (customization_id);


--
-- TOC entry 2799 (class 2606 OID 17820)
-- Name: jointelection supraeleccion_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY jointelection
    ADD CONSTRAINT jointelection_pkey PRIMARY KEY (jointelection_id);


--
-- TOC entry 2801 (class 2606 OID 17822)
-- Name: electionemailtemplate templateeleccion_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY electionemailtemplate
    ADD CONSTRAINT electionemailtemplate_pkey PRIMARY KEY (electionemailtemplate_id);


--
-- TOC entry 2803 (class 2606 OID 17824)
-- Name: useradmin usuarioadmin_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY useradmin
    ADD CONSTRAINT useradmin_pkey PRIMARY KEY (useradmin_id);


--
-- TOC entry 2808 (class 2606 OID 17826)
-- Name: uservoter usuariopadron_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY uservoter
    ADD CONSTRAINT uservoter_pkey PRIMARY KEY (uservoter_id);


--
-- TOC entry 2810 (class 2606 OID 17828)
-- Name: vote voto_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY vote
    ADD CONSTRAINT vote_pkey PRIMARY KEY (vote_id);


--
-- TOC entry 2804 (class 1259 OID 17829)
-- Name: indexupall; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX indexuvall ON uservoter USING btree (uservoter_id, voteamount, language, mail, name, orgid, country, votetoken, voted, election_id);


--
-- TOC entry 2806 (class 1259 OID 17831)
-- Name: padroneleccion; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX indexuvelection ON uservoter USING btree (election_id);


--
-- TOC entry 2821 (class 2606 OID 17832)
-- Name: uservoter fk1do2ntjih65b8p4dgf7yxpsjj; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY uservoter
    ADD CONSTRAINT fk1do2ntjih65b8p4dgf7yxpsjj FOREIGN KEY (election_id) REFERENCES election(election_id);


--
-- TOC entry 2823 (class 2606 OID 17837)
-- Name: vote fk1rxcmws4uua549obakp1s7qqu; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY vote
    ADD CONSTRAINT fk1rxcmws4uua549obakp1s7qqu FOREIGN KEY (uservoter_id) REFERENCES uservoter(uservoter_id);


--
-- TOC entry 2811 (class 2606 OID 17842)
-- Name: auditor fk3bceb6de890bc135; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY auditor
    ADD CONSTRAINT fk3bceb6de890bc135 FOREIGN KEY (election_id) REFERENCES election(election_id);


--
-- TOC entry 2824 (class 2606 OID 17847)
-- Name: vote fk4x75bejx7sq67bk5tajblmrhf; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY vote
    ADD CONSTRAINT fk4x75bejx7sq67bk5tajblmrhf FOREIGN KEY (candidate_id) REFERENCES candidate(candidate_id);


--
-- TOC entry 2812 (class 2606 OID 17852)
-- Name: auditor fk53knnkifx5fqgm1jgcheuwsyo; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY auditor
    ADD CONSTRAINT fk53knnkifx5fqgm1jgcheuwsyo FOREIGN KEY (election_id) REFERENCES election(election_id);


--
-- TOC entry 2819 (class 2606 OID 17857)
-- Name: electionemailtemplate fk60dab3b6rn3m1y85b3hvqdpkv; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY electionemailtemplate
    ADD CONSTRAINT fk60dab3b6rn3m1y85b3hvqdpkv FOREIGN KEY (election_id) REFERENCES election(election_id);


--
-- TOC entry 2814 (class 2606 OID 17862)
-- Name: candidate fk7sa6ixqcdhp53qkjrwituhwct; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY candidate
    ADD CONSTRAINT fk7sa6ixqcdhp53qkjrwituhwct FOREIGN KEY (election_id) REFERENCES election(election_id);


--
-- TOC entry 2815 (class 2606 OID 17867)
-- Name: candidate fk95c3b56d890bc135; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY candidate
    ADD CONSTRAINT fk95c3b56d890bc135 FOREIGN KEY (election_id) REFERENCES election(election_id);


--
-- TOC entry 2820 (class 2606 OID 17872)
-- Name: electionemailtemplate fk96wiao69tqccwuhdextjjx7a2; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY electionemailtemplate
    ADD CONSTRAINT fk96wiao69tqccwuhdextjjx7a2 FOREIGN KEY (election_id) REFERENCES election(election_id);


--
-- TOC entry 2816 (class 2606 OID 17877)
-- Name: candidate fk9728k0jhg0l5qw3folv2smh34; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY candidate
    ADD CONSTRAINT fk9728k0jhg0l5qw3folv2smh34 FOREIGN KEY (election_id) REFERENCES election(election_id);


--
-- TOC entry 2813 (class 2606 OID 17882)
-- Name: auditor fkene41r5h8dgxtaa0hka4ao8e8; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY auditor
    ADD CONSTRAINT fkene41r5h8dgxtaa0hka4ao8e8 FOREIGN KEY (election_id) REFERENCES election(election_id);


--
-- TOC entry 2817 (class 2606 OID 17887)
-- Name: email fkf3wiqomx3ab7qo9353qblqgy5; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY email
    ADD CONSTRAINT fkf3wiqomx3ab7qo9353qblqgy5 FOREIGN KEY (election_id) REFERENCES election(election_id);


--
-- TOC entry 2825 (class 2606 OID 17892)
-- Name: vote fkiwnuc9dco1kvcsj4p0004mvmr; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY vote
    ADD CONSTRAINT fkiwnuc9dco1kvcsj4p0004mvmr FOREIGN KEY (election_id) REFERENCES election(election_id);


--
-- TOC entry 2826 (class 2606 OID 17897)
-- Name: vote fkj0kgemrk6tfc3n4nw6smlu9y3; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY vote
    ADD CONSTRAINT fkj0kgemrk6tfc3n4nw6smlu9y3 FOREIGN KEY (election_id) REFERENCES election(election_id);


--
-- TOC entry 2822 (class 2606 OID 17902)
-- Name: uservoter fkn8xln3ppeqiq1d9i6hggnybln; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY uservoter
    ADD CONSTRAINT fkn8xln3ppeqiq1d9i6hggnybln FOREIGN KEY (election_id) REFERENCES election(election_id);


--
-- TOC entry 2818 (class 2606 OID 17912)
-- Name: email fkpntyqelk43ka0s3ffg16p2sms; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY email
    ADD CONSTRAINT fkpntyqelk43ka0s3ffg16p2sms FOREIGN KEY (election_id) REFERENCES election(election_id);


-- Completed on 2021-07-13 13:12:59

--
-- PostgreSQL database dump complete
--

