-- accesosip
ALTER TABLE accesosips RENAME COLUMN id TO ipaccess_id;
ALTER TABLE accesosips RENAME COLUMN fechaprimerintento TO firstattemptdate;
ALTER TABLE accesosips RENAME COLUMN fechaultimointento TO lastattemptdate;
ALTER TABLE accesosips RENAME COLUMN intentos TO attemptcount;
ALTER TABLE accesosips RENAME TO ipaccess;

-- actividad
ALTER TABLE actividad RENAME COLUMN id_actividad TO activity_id;
ALTER TABLE actividad RENAME COLUMN descripcion TO description;
ALTER TABLE actividad RENAME COLUMN nomuser TO username;
ALTER TABLE actividad RENAME COLUMN tiempo TO timestamp;
ALTER TABLE actividad RENAME COLUMN tipoactividad TO activitytype;
ALTER TABLE actividad RENAME COLUMN ideleccion TO election_id;
ALTER TABLE actividad RENAME TO activity;

-- auditor
ALTER TABLE auditor RENAME COLUMN id_auditor TO auditor_id;
ALTER TABLE auditor RENAME COLUMN comisionado TO commissioner;
ALTER TABLE auditor RENAME COLUMN expresoconformidad TO agreedconformity;
ALTER TABLE auditor RENAME COLUMN nombre TO name;
ALTER TABLE auditor RENAME COLUMN tokenresultado TO resulttoken;
ALTER TABLE auditor RENAME COLUMN id_eleccion TO election_id;
ALTER TABLE auditor RENAME COLUMN habilitarevision TO revisionavailable;
ALTER TABLE auditor RENAME COLUMN idmigracion TO migration_id;

-- calendario
DROP TABLE calendario;

-- candidato
ALTER TABLE candidato RENAME COLUMN id_candidato TO candidate_id;
ALTER TABLE candidato RENAME COLUMN contenidofoto TO pictureinfo;
ALTER TABLE candidato RENAME COLUMN extensionfoto TO pictureextension;
ALTER TABLE candidato RENAME COLUMN nombre TO name;
ALTER TABLE candidato RENAME COLUMN nombrefoto TO picturename;
ALTER TABLE candidato RENAME COLUMN id_eleccion TO election_id;
ALTER TABLE candidato RENAME COLUMN bioespanol TO biospanish;
ALTER TABLE candidato RENAME COLUMN bioingles TO bioenglish;
ALTER TABLE candidato RENAME COLUMN bioportugues TO bioportuguese;
ALTER TABLE candidato RENAME COLUMN orden TO candidateorder;
ALTER TABLE candidato RENAME COLUMN solosp TO onlysp;
ALTER TABLE candidato RENAME COLUMN idmigracion TO migration_id;
ALTER TABLE candidato RENAME COLUMN linkespanol TO linkspanish;
ALTER TABLE candidato RENAME COLUMN linkingles TO linkenglish;
ALTER TABLE candidato RENAME COLUMN linkportugues TO linkportuguese;
ALTER TABLE candidato RENAME TO candidate;

-- comisionado
ALTER TABLE comisionado RENAME COLUMN id_comisionado TO commissioner_id;
ALTER TABLE comisionado RENAME COLUMN nombre TO name;
ALTER TABLE comisionado RENAME COLUMN tokenresultado TO resulttoken;
ALTER TABLE comisionado RENAME TO commissioner;

-- eleccion
ALTER TABLE eleccion RENAME COLUMN id_eleccion TO election_id;
ALTER TABLE eleccion RENAME COLUMN descripcionespanol TO descriptionspanish;
ALTER TABLE eleccion RENAME COLUMN descripcioningles TO descriptionenglish;
ALTER TABLE eleccion RENAME COLUMN descripcionportugues TO descriptionportuguese;
ALTER TABLE eleccion RENAME COLUMN fechacreacion TO creationdate;
ALTER TABLE eleccion RENAME COLUMN fechafin TO enddate;
ALTER TABLE eleccion RENAME COLUMN fechainicio TO startdate;
ALTER TABLE eleccion RENAME COLUMN habilitadolinkresultado TO resultlinkavailable;
ALTER TABLE eleccion RENAME COLUMN habilitadolinkvotacion TO votinglinkavailable;
ALTER TABLE eleccion RENAME COLUMN linkespanol TO linkspanish;
ALTER TABLE eleccion RENAME COLUMN linkingles TO linkenglish;
ALTER TABLE eleccion RENAME COLUMN linkportugues TO linkportuguese;
ALTER TABLE eleccion RENAME COLUMN maxcandidatos TO maxcandidates;
ALTER TABLE eleccion RENAME COLUMN tituloespanol TO titlespanish;
ALTER TABLE eleccion RENAME COLUMN tituloingles TO titleenglish;
ALTER TABLE eleccion RENAME COLUMN tituloportugues TO titleportuguese;
ALTER TABLE eleccion RENAME COLUMN tokenresultado TO resulttoken;
ALTER TABLE eleccion RENAME COLUMN auditoresseteado TO auditorsset;
ALTER TABLE eleccion RENAME COLUMN candidatosseteado TO candidatesset;
ALTER TABLE eleccion RENAME COLUMN padronseteado TO electorsset;
ALTER TABLE eleccion RENAME COLUMN habilitadolinkauditor TO auditorlinkavailable;
ALTER TABLE eleccion RENAME COLUMN solosp TO onlysp;
ALTER TABLE eleccion RENAME COLUMN remitentepordefecto TO defaultsender;
ALTER TABLE eleccion RENAME COLUMN candidatosaleatorios TO randomordercandidates;
ALTER TABLE eleccion RENAME COLUMN solicitarrevision TO revisionrequest;
ALTER TABLE eleccion RENAME COLUMN idmigracion TO migration_id;
ALTER TABLE eleccion RENAME COLUMN migrada TO migrated;
ALTER TABLE eleccion RENAME COLUMN categoria TO category;
ALTER TABLE eleccion DROP COLUMN auxfechafin;
ALTER TABLE eleccion RENAME TO election;

-- email
ALTER TABLE email RENAME COLUMN id TO email_id;
ALTER TABLE email RENAME COLUMN asunto TO subject;
ALTER TABLE email RENAME COLUMN cuerpo TO body;
ALTER TABLE email RENAME COLUMN desde TO sender;
ALTER TABLE email RENAME COLUMN destinatarios TO recipients;
ALTER TABLE email RENAME COLUMN enviado TO sent;
ALTER TABLE email RENAME COLUMN tipotemplate TO templatetype;
ALTER TABLE email RENAME COLUMN id_eleccion TO election_id;
ALTER TABLE email RENAME COLUMN fechacreado TO createddate;

-- emailhistorico
ALTER TABLE emailhistorico RENAME COLUMN id TO emailhistory_id;
ALTER TABLE emailhistorico RENAME COLUMN asunto TO subject;
ALTER TABLE emailhistorico RENAME COLUMN cuerpo TO body;
ALTER TABLE emailhistorico RENAME COLUMN desde TO sender;
ALTER TABLE emailhistorico RENAME COLUMN destinatarios TO recipients;
ALTER TABLE emailhistorico RENAME COLUMN enviado TO sent;
ALTER TABLE emailhistorico RENAME COLUMN tipotemplate TO templatetype;
ALTER TABLE emailhistorico RENAME COLUMN ideleccion TO election_id;
ALTER TABLE emailhistorico RENAME COLUMN fechacreado TO createddate;
ALTER TABLE emailhistorico RENAME TO emailhistory;

-- ipinhabilitada
DROP TABLE ipinhabilitada;

-- parametro
ALTER TABLE parametro RENAME COLUMN clave TO key;
ALTER TABLE parametro RENAME COLUMN valor TO value;
ALTER TABLE parametro RENAME TO parameter;

-- personalizacion
ALTER TABLE personalizacion RENAME COLUMN id_personalizacion TO customization_id;
ALTER TABLE personalizacion RENAME COLUMN pic_simbolo TO pic_symbol;
ALTER TABLE personalizacion RENAME COLUMN cont_pic_simbolo TO cont_pic_symbol;
ALTER TABLE personalizacion RENAME COLUMN titulo_sitio TO site_title;
ALTER TABLE personalizacion RENAME COLUMN titulo_login TO login_title;
ALTER TABLE personalizacion  RENAME TO customization;

-- supraeleccion
ALTER TABLE supraeleccion RENAME COLUMN id TO jointelection_id;
ALTER TABLE supraeleccion RENAME COLUMN idelecciona TO electiona_id;
ALTER TABLE supraeleccion RENAME COLUMN ideleccionb TO electionb_id;
ALTER TABLE supraeleccion RENAME TO jointelection;

-- templateeleccion
ALTER TABLE templateeleccion RENAME COLUMN id_template_eleccion TO electionemailtemplate_id;
ALTER TABLE templateeleccion RENAME COLUMN asuntoen TO subjecten;
ALTER TABLE templateeleccion RENAME COLUMN asuntopt TO subjectpt;
ALTER TABLE templateeleccion RENAME COLUMN cuerpoen TO bodyen;
ALTER TABLE templateeleccion RENAME COLUMN cuerpoes TO bodysp;
ALTER TABLE templateeleccion RENAME COLUMN cuerpopt TO bodypt;
ALTER TABLE templateeleccion RENAME COLUMN tipo TO type;
ALTER TABLE templateeleccion RENAME COLUMN id_eleccion TO election_id;
ALTER TABLE templateeleccion RENAME COLUMN asuntoes TO subjectsp;
ALTER TABLE templateeleccion  RENAME TO electionemailtemplate;

-- usuarioadmin
ALTER TABLE usuarioadmin RENAME COLUMN user_id TO useradmin_id;
ALTER TABLE usuarioadmin RENAME COLUMN id_eleccion_autorizado TO authorizedelection_id;
ALTER TABLE usuarioadmin  RENAME TO useradmin;

-- usuariopadron
ALTER TABLE usuariopadron RENAME COLUMN id_usuario_padron TO uservoter_id;
ALTER TABLE usuariopadron RENAME COLUMN cantvotos TO voteamount;
ALTER TABLE usuariopadron RENAME COLUMN idioma TO language;
ALTER TABLE usuariopadron RENAME COLUMN nombre TO name;
ALTER TABLE usuariopadron RENAME COLUMN pais TO country;
ALTER TABLE usuariopadron RENAME COLUMN tokenvotacion TO votetoken;
ALTER TABLE usuariopadron RENAME COLUMN yavoto TO voted;
ALTER TABLE usuariopadron RENAME COLUMN id_eleccion TO election_id;
ALTER TABLE usuariopadron RENAME COLUMN fechavoto TO votedate;
ALTER TABLE usuariopadron RENAME COLUMN idmigracion TO migration_id;
ALTER TABLE usuariopadron RENAME TO uservoter;

-- voto
ALTER TABLE voto RENAME COLUMN idvoto TO vote_id;
ALTER TABLE voto RENAME COLUMN codigo TO code;
ALTER TABLE voto RENAME COLUMN fechavoto TO votedate;
ALTER TABLE voto RENAME COLUMN id_candidato TO candidate_id;
ALTER TABLE voto RENAME COLUMN id_eleccion TO election_id;
ALTER TABLE voto RENAME COLUMN id_usuario_padron TO uservoter_id;
ALTER TABLE voto RENAME TO vote;

-- secuencias
ALTER SEQUENCE public.actividad_seq RENAME TO activity_seq;
ALTER SEQUENCE public.aip_seq RENAME TO ipaccess_seq;
DROP SEQUENCE public.calendario_seq;
ALTER SEQUENCE public.candidato_seq RENAME TO candidate_seq;
ALTER SEQUENCE public.eleccion_seq RENAME TO election_seq;
ALTER SEQUENCE public.emaile_seq RENAME TO email_seq;
ALTER SEQUENCE public.personalizacion_seq RENAME TO customization_seq;
ALTER SEQUENCE public.supraeleccion_seq RENAME TO jointelection_seq;
ALTER SEQUENCE public.templateeleccion_seq RENAME TO electionemailtemplate_seq;
ALTER SEQUENCE public.usuario_padron_seq RENAME TO uservoter_seq;
ALTER SEQUENCE public.voto_seq RENAME TO vote_seq;
CREATE SEQUENCE public.commissioner_seq START WITH 1 INCREMENT BY 1 NO MINVALUE NO MAXVALUE CACHE 1;
ALTER TABLE public.commissioner_seq OWNER TO postgres;

-- base de datos
ALTER DATABASE elecciones RENAME TO elections;
