--
-- PostgreSQL database dump
--

-- Dumped from database version 12.22 (Ubuntu 12.22-0ubuntu0.20.04.4)
-- Dumped by pg_dump version 14.17 (Homebrew)

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
-- Name: activity; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.activity (
    activity_id bigint NOT NULL,
    activitytype character varying(255) NOT NULL,
    description text,
    election_id bigint,
    ip character varying(255) NOT NULL,
    "timestamp" timestamp(6) without time zone NOT NULL,
    username character varying(255) NOT NULL
);


ALTER TABLE public.activity OWNER TO postgres;

--
-- Name: activity_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.activity_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.activity_seq OWNER TO postgres;

--
-- Name: auditor; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.auditor (
    auditor_id bigint NOT NULL,
    agreedconformity boolean,
    commissioner boolean,
    mail character varying(255) NOT NULL,
    migration_id bigint,
    name character varying(255) NOT NULL,
    reminder_frequency character varying(64),
    resulttoken character varying(1000),
    revisionavailable boolean,
    election_id bigint NOT NULL,
    CONSTRAINT auditor_reminder_frequency_check CHECK (((reminder_frequency)::text = ANY ((ARRAY['MONDAY_WEDNESDAY_FRIDAY'::character varying, 'TUESDAY_THURSDAY'::character varying, 'MONDAY_TO_FRIDAY'::character varying, 'EVERY_DAY'::character varying, 'DISABLED'::character varying])::text[])))
);


ALTER TABLE public.auditor OWNER TO postgres;

--
-- Name: auditor_candidate_decision_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.auditor_candidate_decision_seq
    START WITH 90000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.auditor_candidate_decision_seq OWNER TO postgres;

--
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
-- Name: auditorcandidatedecision; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.auditorcandidatedecision (
    id bigint NOT NULL,
    approved_date timestamp(6) without time zone,
    decisiondate timestamp(6) without time zone NOT NULL,
    decisionstatus character varying(255) NOT NULL,
    pre_decision_status character varying(255),
    pre_decision_date timestamp(6) without time zone,
    pre_decision_comment character varying(4000),
    final_decision_status character varying(255),
    final_decision_date timestamp(6) without time zone,
    final_decision_comment character varying(4000),
    preapproved_date timestamp(6) without time zone,
    auditor_id bigint NOT NULL,
    candidate_id bigint NOT NULL,
    CONSTRAINT auditorcandidatedecision_decisionstatus_check CHECK (((decisionstatus)::text = ANY ((ARRAY['ANALYZING'::character varying, 'PREAPPROVED'::character varying, 'APPROVED'::character varying, 'REJECTED'::character varying, 'NO_APPLY'::character varying])::text[])))
);


ALTER TABLE public.auditorcandidatedecision OWNER TO postgres;

--
-- Name: auditor_results; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.auditor_results (
    auditor_result_id bigint NOT NULL,
    election_id bigint NOT NULL,
    result_english text,
    result_letter_english bytea,
    result_letter_portuguese bytea,
    result_letter_spanish bytea,
    result_portuguese text,
    result_spanish text
);


ALTER TABLE public.auditor_results OWNER TO postgres;

--
-- Name: auditor_results_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.auditor_results_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.auditor_results_seq OWNER TO postgres;

--
-- Name: candidate; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.candidate (
    candidate_id bigint NOT NULL,
    bioenglish text,
    bioportuguese text,
    biospanish text,
    campuscoursecalification character varying(255),
    campuscourseselected bigint,
    campuscoursestatus character varying(255),
    candidateorder integer,
    candidate_type character varying(32),
    evaluationstatus character varying(255),
    linkenglish text,
    linkportuguese text,
    linkspanish text,
    linkedinurl text,
    mail character varying(255),
    migration_id bigint,
    name character varying(1000) NOT NULL,
    onlysp boolean,
    pictureextension character varying(255) NOT NULL,
    pictureinfo bytea NOT NULL,
    picturename character varying(255) NOT NULL,
    proctorioresultfile bytea,
    qadultincountry boolean,
    qcanspeakspanish boolean,
    qcivilrightslimitation boolean,
    q_declaration_aso_knowledge boolean,
    q_declaration_competencies boolean,
    q_declaration_conflicts_interest boolean,
    q_declaration_data_usage boolean,
    q_declaration_disciplinary boolean,
    q_declaration_dynamic boolean,
    q_declaration_iana_knowledge boolean,
    q_declaration_incompatibilities boolean,
    q_declaration_pep character varying(255),
    qhealthmentallimitation boolean,
    qhealthtravellimitation boolean,
    qlegallimitationanycountry boolean,
    qothernonstatutoryanswer1english text,
    qothernonstatutoryanswer1portuguese text,
    qothernonstatutoryanswer1spanish text,
    qotherstatutoryanswer1english text,
    qotherstatutoryanswer1portuguese text,
    qotherstatutoryanswer1spanish text,
    qotherstatutoryanswer2english text,
    qotherstatutoryanswer2portuguese text,
    qotherstatutoryanswer2spanish text,
    qotherstatutoryanswer3english text,
    qotherstatutoryanswer3portuguese text,
    qotherstatutoryanswer3spanish text,
    qotherstatutoryanswer4english text,
    qotherstatutoryanswer4portuguese text,
    qotherstatutoryanswer4spanish text,
    qunemployed boolean,
    reminder_frequency character varying(64),
    status character varying(255),
    winner boolean DEFAULT false NOT NULL,
    election_id bigint NOT NULL,
    CONSTRAINT candidate_campuscoursestatus_check CHECK (((campuscoursestatus)::text = ANY ((ARRAY['PENDING'::character varying, 'SENT'::character varying, 'STARTED'::character varying, 'COMPLETED'::character varying, 'NOT_APPLICABLE'::character varying])::text[]))),
    CONSTRAINT candidate_candidate_type_check CHECK (((candidate_type)::text = ANY ((ARRAY['NORMAL'::character varying, 'ABSTENTION'::character varying])::text[]))),
    CONSTRAINT candidate_evaluationstatus_check CHECK (((evaluationstatus)::text = ANY ((ARRAY['PENDING'::character varying, 'CREDENTIALS_REQUESTED'::character varying, 'CREDENTIALS_SENT'::character varying, 'COMPLETED'::character varying, 'NOT_APPLICABLE'::character varying])::text[]))),
    CONSTRAINT candidate_q_declaration_pep_check CHECK (((q_declaration_pep)::text = ANY ((ARRAY['NOT_PEP'::character varying, 'PEP'::character varying])::text[]))),
    CONSTRAINT candidate_reminder_frequency_check CHECK (((reminder_frequency)::text = ANY ((ARRAY['MONDAY_WEDNESDAY_FRIDAY'::character varying, 'TUESDAY_THURSDAY'::character varying, 'MONDAY_TO_FRIDAY'::character varying, 'EVERY_DAY'::character varying, 'DISABLED'::character varying])::text[]))),
    CONSTRAINT candidate_status_check CHECK (((status)::text = ANY ((ARRAY['INCOMPLETE'::character varying, 'PRECOMPLETE'::character varying, 'COMPLETE'::character varying, 'CONFIRMED_AND_PUBLISHED'::character varying, 'REJECTED'::character varying])::text[])))
);


ALTER TABLE public.candidate OWNER TO postgres;

--
-- Name: candidate_countrylink_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.candidate_countrylink_seq
    START WITH 60000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.candidate_countrylink_seq OWNER TO postgres;

--
-- Name: candidate_election_task_progress; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.candidate_election_task_progress (
    id bigint NOT NULL,
    enddate timestamp(6) without time zone,
    startdate timestamp(6) without time zone,
    status character varying(255) NOT NULL,
    candidate_id bigint NOT NULL,
    election_task_id bigint NOT NULL,
    CONSTRAINT candidate_election_task_progress_status_check CHECK (((status)::text = ANY ((ARRAY['NOT_STARTED'::character varying, 'STARTED'::character varying, 'COMPLETED'::character varying, 'OMITTED'::character varying])::text[])))
);


ALTER TABLE public.candidate_election_task_progress OWNER TO postgres;

--
-- Name: candidate_election_task_progress_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.candidate_election_task_progress_seq
    START WITH 95000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.candidate_election_task_progress_seq OWNER TO postgres;

--
-- Name: candidate_question; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.candidate_question (
    candidate_question_id bigint NOT NULL,
    answer_english text,
    answer_portuguese text,
    answer_spanish text,
    asked_by_email character varying(320),
    asked_by_name character varying(500),
    creation_date timestamp(6) without time zone NOT NULL,
    current_owner character varying(16) NOT NULL,
    published_date timestamp(6) without time zone,
    question_english text,
    question_language character varying(16) NOT NULL,
    question_portuguese text,
    question_spanish text,
    status character varying(64) NOT NULL,
    update_date timestamp(6) without time zone NOT NULL,
    candidate_id bigint NOT NULL,
    election_id bigint NOT NULL,
    CONSTRAINT candidate_question_current_owner_check CHECK (((current_owner)::text = ANY ((ARRAY['LACNIC'::character varying, 'CANDIDATE'::character varying, 'NONE'::character varying])::text[]))),
    CONSTRAINT candidate_question_question_language_check CHECK (((question_language)::text = ANY ((ARRAY['SP'::character varying, 'EN'::character varying, 'PT'::character varying])::text[]))),
    CONSTRAINT candidate_question_status_check CHECK (((status)::text = ANY ((ARRAY['QUESTION_RECEIVED_LACNIC'::character varying, 'QUESTION_READY_FOR_CANDIDATE'::character varying, 'ANSWER_SUBMITTED_BY_CANDIDATE'::character varying, 'PUBLISHED'::character varying, 'REJECTED'::character varying])::text[])))
);


ALTER TABLE public.candidate_question OWNER TO postgres;

--
-- Name: candidate_question_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.candidate_question_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.candidate_question_seq OWNER TO postgres;

--
-- Name: candidate_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.candidate_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.candidate_seq OWNER TO postgres;

--
-- Name: candidatecountrylink; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.candidatecountrylink (
    id bigint NOT NULL,
    countrycode character varying(3) NOT NULL,
    primarycountry boolean NOT NULL,
    qcitizen boolean,
    qeligibleforcitizenship boolean,
    qfamilyresidenceover5y boolean,
    qinternetcommunityorgparticipation boolean,
    qlongemploymentoradvisory5y boolean,
    qresidenceover5y boolean,
    candidate_id bigint NOT NULL
);


ALTER TABLE public.candidatecountrylink OWNER TO postgres;

--
-- Name: candidateworkorganization; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.candidateworkorganization (
    id bigint NOT NULL,
    organizationgroup character varying(4000),
    organizationname character varying(1000) NOT NULL,
    workorganizationtype character varying(50) NOT NULL,
    candidate_id bigint NOT NULL,
    CONSTRAINT candidateworkorganization_workorganizationtype_check CHECK (((workorganizationtype)::text = ANY ((ARRAY['PAID'::character varying, 'AD_HONOREM'::character varying])::text[])))
);


ALTER TABLE public.candidateworkorganization OWNER TO postgres;

--
-- Name: commissioner; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.commissioner (
    commissioner_id bigint NOT NULL,
    mail character varying(255) NOT NULL,
    name character varying(255) NOT NULL
);


ALTER TABLE public.commissioner OWNER TO postgres;

--
-- Name: commissioner_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.commissioner_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.commissioner_seq OWNER TO postgres;

--
-- Name: customization; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.customization (
    customization_id bigint NOT NULL,
    cont_pic_big_logo bytea,
    cont_pic_small_logo bytea,
    cont_pic_symbol bytea,
    home_html text,
    login_title character varying(255) NOT NULL,
    pic_big_logo character varying(255) NOT NULL,
    pic_small_logo character varying(255) NOT NULL,
    pic_symbol character varying(255) NOT NULL,
    show_home boolean NOT NULL,
    site_title character varying(255) NOT NULL
);


ALTER TABLE public.customization OWNER TO postgres;

--
-- Name: customization_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.customization_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.customization_seq OWNER TO postgres;

--
-- Name: election; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.election (
    election_id bigint NOT NULL,
    auditorlinkavailable boolean NOT NULL,
    donominationlinkavailable boolean NOT NULL,
    auditorsset boolean,
    candidatesset boolean,
    category character varying(255),
    closed boolean NOT NULL,
    closeddate timestamp(6) without time zone,
    creationdate timestamp(6) without time zone NOT NULL,
    defaultrecipient character varying(2000),
    defaultsender character varying(2000),
    authorized_nominate_emails text,
    authorized_support_emails text,
    authorized_user_emails text,
    descriptionenglish text,
    descriptionportuguese text,
    descriptionspanish text,
    diffutc integer,
    electorsset boolean,
    linkenglish text NOT NULL,
    linkportuguese text NOT NULL,
    linkspanish text NOT NULL,
    maxcandidates integer NOT NULL,
    migrated boolean,
    nominationtaskslinkavailable boolean NOT NULL,
    nominationsupportlinkavailable boolean NOT NULL,
    onlysp boolean,
    public_election_token character varying(1000),
    publicelectionlinkavailable boolean NOT NULL,
    randomordercandidates boolean,
    resultlinkavailable boolean NOT NULL,
    resulttoken character varying(1000),
    revisionrequest boolean NOT NULL,
    titleenglish text NOT NULL,
    titleportuguese text NOT NULL,
    titlespanish text NOT NULL,
    votinglinkavailable boolean NOT NULL,
    calendarset boolean,
    campuscourse bigint,
    campuscourseenglish bigint,
    campuscourseportuguese bigint,
    election_type character varying(255),
    public_link_recovery_mode character varying(255),
    manageorganizationsmanual boolean,
    managevotersmanual boolean,
    migration_id bigint,
    organizationsset boolean,
    tasksset boolean,
    callspanish text,
    callenglish text,
    callportuguese text,
    callset boolean,
    CONSTRAINT election_category_check CHECK (((category)::text = ANY ((ARRAY['STATUTORY'::character varying, 'MODERATORS'::character varying, 'OTHER'::character varying, 'TEST'::character varying])::text[]))),
    CONSTRAINT election_election_type_check CHECK (((election_type)::text = ANY ((ARRAY['BOARD'::character varying, 'IANA'::character varying, 'ASO'::character varying, 'MODERATORS'::character varying, 'FISCAL_COMMISSION'::character varying, 'ELECTORAL_COMMISSION'::character varying, 'OTHER'::character varying])::text[]))),
    CONSTRAINT election_public_link_recovery_mode_check CHECK (((public_link_recovery_mode)::text = ANY ((ARRAY['NONE'::character varying, 'ONLY_BR'::character varying, 'ALL'::character varying])::text[])))
);


ALTER TABLE public.election OWNER TO postgres;

--
-- Name: election_event_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.election_event_seq
    START WITH 80000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.election_event_seq OWNER TO postgres;

--
-- Name: election_restricted_country_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.election_restricted_country_seq
    START WITH 85000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.election_restricted_country_seq OWNER TO postgres;

--
-- Name: election_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.election_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.election_seq OWNER TO postgres;

--
-- Name: electioncalendar; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.electioncalendar (
    id bigint NOT NULL,
    calendarkey character varying(255) NOT NULL,
    enddate timestamp(6) without time zone,
    publicable boolean NOT NULL,
    startdate timestamp(6) without time zone,
    election_id bigint NOT NULL,
    CONSTRAINT electioncalendar_calendarkey_check CHECK (((calendarkey)::text = ANY ((ARRAY['N_23_PERIODO_PADRON_MILACNIC_SYNC'::character varying, 'N_1_SINGLE_CALL_FOR_CANDIDATES_PUBLISHED'::character varying, 'N_2_PERIODO_CALL_FOR_CANDIDATES'::character varying, 'N_3_SINGLE_PADRON_CLOSED'::character varying, 'N_4_SINGLE_PADRON_PUBLISHED'::character varying, 'N_5_PERIODO_PADRON_CLAIMS'::character varying, 'N_6_PERIODO_ADDITIONAL_EVALUATION'::character varying, 'N_7_PERIODO_CANDIDATE_FORMS_VERIFICATION'::character varying, 'N_8_PERIODO_EVALUATIONS_VALIDATION'::character varying, 'N_9_PERIODO_CANDIDATE_CLAIMS'::character varying, 'N_10_PERIODO_CANDIDATE_CLAIMS_RESOLUTION'::character varying, 'N_11_SINGLE_CANDIDATES_PUBLISHED'::character varying, 'N_12_PERIODO_CANDIDATE_QUESTIONS'::character varying, 'N_13_PERIODO_PADRON_CLAIMS_RESOLUTION'::character varying, 'N_14_SINGLE_PADRON_UPDATED'::character varying, 'N_15_PERIODO_CANDIDATE_CLAIMS_BY_CE'::character varying, 'N_16_PERIODO_VOTING'::character varying, 'N_17_SINGLE_PROVISIONAL_RESULTS_PUBLISHED'::character varying, 'N_18_PERIODO_CE_AUDIT'::character varying, 'N_19_PERIODO_VOTER_AUDIT'::character varying, 'N_20_SINGLE_OFFICIAL_RESULTS_NO_CLAIMS_PUBLISHED'::character varying, 'N_21_PERIODO_VOTER_CLAIMS_RESPONSE'::character varying, 'N_22_SINGLE_OFFICIAL_RESULTS_WITH_CLAIMS_PUBLISHED'::character varying])::text[])))
);


ALTER TABLE public.electioncalendar OWNER TO postgres;

--
-- Name: electionemailtemplate; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.electionemailtemplate (
    electionemailtemplate_id bigint NOT NULL,
    bodyen text,
    bodypt text,
    bodysp text,
    subjecten text,
    subjectpt text,
    subjectsp text,
    type character varying(255),
    election_id bigint
);


ALTER TABLE public.electionemailtemplate OWNER TO postgres;

--
-- Name: electionemailtemplate_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.electionemailtemplate_seq
    START WITH 500
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.electionemailtemplate_seq OWNER TO postgres;

--
-- Name: electionrestrictedcountry; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.electionrestrictedcountry (
    id bigint NOT NULL,
    countrycode character varying(3) NOT NULL,
    election_id bigint NOT NULL
);


ALTER TABLE public.electionrestrictedcountry OWNER TO postgres;

--
-- Name: electiontask; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.electiontask (
    id bigint NOT NULL,
    dependencylevel character varying(255),
    displayorder integer,
    publicable boolean NOT NULL,
    taskkey character varying(255) NOT NULL,
    election_id bigint NOT NULL,
    election_calendar_id bigint NOT NULL,
    CONSTRAINT electiontask_dependencylevel_check CHECK (((dependencylevel)::text = ANY ((ARRAY['LEVEL_1'::character varying, 'LEVEL_2'::character varying, 'LEVEL_3'::character varying, 'LEVEL_4'::character varying, 'LEVEL_5'::character varying, 'LEVEL_6'::character varying])::text[]))),
    CONSTRAINT electiontask_taskkey_check CHECK (((taskkey)::text = ANY ((ARRAY['PROFILE'::character varying, 'COUNTRIES'::character varying, 'INCOMPATIBILITIES'::character varying, 'ORG_SUPPORTS'::character varying, 'USER_SUPPORTS_2'::character varying, 'USER_SUPPORTS_5'::character varying, 'ORGANIZATIONS'::character varying, 'COURSE'::character varying, 'OTHER_STATUTORY_QUESTIONS'::character varying, 'OTHER_NON_STATUTORY_QUESTIONS'::character varying, 'DECLARATIONS'::character varying, 'DECLARATIONS_NON_STATUTORY'::character varying, 'EVALUATION'::character varying])::text[])))
);


ALTER TABLE public.electiontask OWNER TO postgres;

--
-- Name: email; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.email (
    email_id bigint NOT NULL,
    bcc character varying(255),
    body text NOT NULL,
    cc character varying(255),
    createddate timestamp(6) without time zone,
    prioritized boolean,
    recipients text,
    sender character varying(255),
    sent boolean,
    subject text NOT NULL,
    templatetype character varying(255) NOT NULL,
    election_id bigint
);


ALTER TABLE public.email OWNER TO postgres;

--
-- Name: email_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.email_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.email_seq OWNER TO postgres;

--
-- Name: emailhistory; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.emailhistory (
    emailhistory_id bigint NOT NULL,
    bcc character varying(255),
    body text NOT NULL,
    cc character varying(255),
    createddate timestamp(6) without time zone,
    election_id bigint,
    prioritized boolean,
    recipients character varying(255),
    sender character varying(255),
    sent boolean,
    subject text NOT NULL,
    templatetype character varying(255)
);


ALTER TABLE public.emailhistory OWNER TO postgres;

--
-- Name: ipaccess; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.ipaccess (
    ipaccess_id bigint NOT NULL,
    attemptcount integer NOT NULL,
    firstattemptdate timestamp(6) without time zone NOT NULL,
    ip character varying(255) NOT NULL,
    lastattemptdate timestamp(6) without time zone NOT NULL
);


ALTER TABLE public.ipaccess OWNER TO postgres;

--
-- Name: ipaccess_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.ipaccess_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.ipaccess_seq OWNER TO postgres;

--
-- Name: jointelection; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.jointelection (
    jointelection_id bigint NOT NULL,
    electiona_id bigint NOT NULL,
    electionb_id bigint NOT NULL
);


ALTER TABLE public.jointelection OWNER TO postgres;

--
-- Name: jointelection_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.jointelection_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.jointelection_seq OWNER TO postgres;

--
-- Name: nomination; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.nomination (
    id bigint NOT NULL,
    acceptnominationtoken character varying(1000),
    nominationdate character varying(255),
    nominationemail character varying(255),
    nominationname character varying(255),
    nominationphonenumber character varying(255),
    nominationreasonspanish character varying(2000),
    nominationreasonenglish character varying(2000),
    nominationreasonportuguese character varying(2000),
    status character varying(255) NOT NULL,
    candidate_id bigint,
    election_id bigint NOT NULL,
    organization_id bigint NOT NULL,
    CONSTRAINT nomination_status_check CHECK (((status)::text = ANY ((ARRAY['PROPOSED'::character varying, 'ACCEPTED_BY_CANDIDATE'::character varying, 'REJECTED_BY_CANDIDATE'::character varying, 'INVALID'::character varying, 'APPROVED'::character varying])::text[])))
);


ALTER TABLE public.nomination OWNER TO postgres;

--
-- Name: nomination_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.nomination_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.nomination_seq OWNER TO postgres;

--
-- Name: organization; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.organization (
    id bigint NOT NULL,
    asn character varying(255),
    category character varying(255),
    cnpj character varying(255),
    country character varying(255),
    created_at timestamp(6) without time zone NOT NULL,
    deudor boolean NOT NULL,
    donominationtoken character varying(255),
    member boolean,
    membershipcontactemail character varying(255),
    membershipcontactid character varying(255),
    membershipcontactlanguage character varying(255),
    membershipcontactname character varying(255),
    name character varying(255) NOT NULL,
    orgid character varying(255) NOT NULL,
    updated_at timestamp(6) without time zone NOT NULL,
    votes integer,
    election_id bigint NOT NULL
);


ALTER TABLE public.organization OWNER TO postgres;

--
-- Name: organization_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.organization_seq
    START WITH 90000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.organization_seq OWNER TO postgres;

--
-- Name: sync_audit; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.sync_audit (
    id bigint NOT NULL,
    eventdate timestamp(6) without time zone NOT NULL,
    fieldname character varying(32) NOT NULL,
    new_text text,
    old_text text,
    orgid character varying(255) NOT NULL,
    syncrunid character varying(64) NOT NULL,
    election_id bigint NOT NULL
);


ALTER TABLE public.sync_audit OWNER TO postgres;

--
-- Name: sync_audit_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.sync_audit_seq
    START WITH 97000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.sync_audit_seq OWNER TO postgres;

--
-- Name: sync_run; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.sync_run (
    id bigint NOT NULL,
    createdrows integer NOT NULL,
    deleted_rows integer NOT NULL,
    duration_ms bigint,
    health_indicators character varying(1000),
    message character varying(1000),
    processedrows integer NOT NULL,
    status character varying(32) NOT NULL,
    sync_type character varying(32) NOT NULL,
    syncat timestamp(6) without time zone NOT NULL,
    syncrunid character varying(64) NOT NULL,
    updatedrows integer NOT NULL,
    ws_elapsed_ms bigint,
    ws_status_code integer,
    election_id bigint NOT NULL
);


ALTER TABLE public.sync_run OWNER TO postgres;

--
-- Name: sync_run_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.sync_run_seq
    START WITH 98000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.sync_run_seq OWNER TO postgres;

--
-- Name: parameter; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.parameter (
    key character varying(255) NOT NULL,
    value text
);


ALTER TABLE public.parameter OWNER TO postgres;

--
-- Name: supportnomination; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.supportnomination (
    id bigint NOT NULL,
    supportstatus character varying(255),
    supportstatusdate timestamp(6) without time zone,
    supportingcontactemail character varying(255),
    supportingcontactname character varying(255),
    token character varying(1000) NOT NULL,
    nomination_id bigint NOT NULL,
    supporting_organization_id bigint,
    CONSTRAINT supportnomination_supportstatus_check CHECK (((supportstatus)::text = ANY ((ARRAY['PROPOSED'::character varying, 'ACCEPTED'::character varying, 'REJECTED'::character varying, 'INVALID'::character varying, 'APPROVED'::character varying])::text[])))
);


ALTER TABLE public.supportnomination OWNER TO postgres;

--
-- Name: supportnomination_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.supportnomination_seq
    START WITH 70000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.supportnomination_seq OWNER TO postgres;

--
-- Name: task_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.task_seq
    START WITH 50000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.task_seq OWNER TO postgres;

--
-- Name: useradmin; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.useradmin (
    useradmin_id character varying(255) NOT NULL,
    email text,
    password text
);


ALTER TABLE public.useradmin OWNER TO postgres;

--
-- Name: uservoter; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.uservoter (
    uservoter_id bigint NOT NULL,
    country character varying(255),
    language character varying(255) NOT NULL,
    mail character varying(255) NOT NULL,
    migration_id bigint,
    name character varying(1000) NOT NULL,
    orgid character varying(255),
    orgname character varying(1000),
    version integer,
    voteamount integer NOT NULL,
    votedate timestamp(6) without time zone,
    votetoken character varying(1000),
    voted boolean NOT NULL,
    election_id bigint NOT NULL
);


ALTER TABLE public.uservoter OWNER TO postgres;

--
-- Name: uservoter_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.uservoter_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.uservoter_seq OWNER TO postgres;

--
-- Name: vote; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.vote (
    vote_id bigint NOT NULL,
    code character varying(255) NOT NULL,
    ip character varying(255),
    votedate timestamp(6) without time zone,
    candidate_id bigint NOT NULL,
    election_id bigint NOT NULL,
    uservoter_id bigint
);


ALTER TABLE public.vote OWNER TO postgres;

--
-- Name: vote_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.vote_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.vote_seq OWNER TO postgres;

--
-- Name: workorganization_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.workorganization_seq
    START WITH 60000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.workorganization_seq OWNER TO postgres;

--
-- Name: activity activity_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.activity
    ADD CONSTRAINT activity_pkey PRIMARY KEY (activity_id);


--
-- Name: auditor auditor_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.auditor
    ADD CONSTRAINT auditor_pkey PRIMARY KEY (auditor_id);


--
-- Name: auditor_results auditor_results_election_id_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.auditor_results
    ADD CONSTRAINT auditor_results_election_id_key UNIQUE (election_id);


--
-- Name: auditor_results auditor_results_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.auditor_results
    ADD CONSTRAINT auditor_results_pkey PRIMARY KEY (auditor_result_id);


--
-- Name: auditorcandidatedecision auditorcandidatedecision_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.auditorcandidatedecision
    ADD CONSTRAINT auditorcandidatedecision_pkey PRIMARY KEY (id);


--
-- Name: candidate_election_task_progress candidate_election_task_progress_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.candidate_election_task_progress
    ADD CONSTRAINT candidate_election_task_progress_pkey PRIMARY KEY (id);


--
-- Name: candidate candidate_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.candidate
    ADD CONSTRAINT candidate_pkey PRIMARY KEY (candidate_id);


--
-- Name: candidate_question candidate_question_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.candidate_question
    ADD CONSTRAINT candidate_question_pkey PRIMARY KEY (candidate_question_id);


--
-- Name: candidatecountrylink candidatecountrylink_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.candidatecountrylink
    ADD CONSTRAINT candidatecountrylink_pkey PRIMARY KEY (id);


--
-- Name: candidateworkorganization candidateworkorganization_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.candidateworkorganization
    ADD CONSTRAINT candidateworkorganization_pkey PRIMARY KEY (id);


--
-- Name: commissioner commissioner_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.commissioner
    ADD CONSTRAINT commissioner_pkey PRIMARY KEY (commissioner_id);


--
-- Name: customization customization_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.customization
    ADD CONSTRAINT customization_pkey PRIMARY KEY (customization_id);


--
-- Name: election election_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.election
    ADD CONSTRAINT election_pkey PRIMARY KEY (election_id);


--
-- Name: electioncalendar electioncalendar_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.electioncalendar
    ADD CONSTRAINT electioncalendar_pkey PRIMARY KEY (id);


--
-- Name: electionemailtemplate electionemailtemplate_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.electionemailtemplate
    ADD CONSTRAINT electionemailtemplate_pkey PRIMARY KEY (electionemailtemplate_id);


--
-- Name: electionrestrictedcountry electionrestrictedcountry_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.electionrestrictedcountry
    ADD CONSTRAINT electionrestrictedcountry_pkey PRIMARY KEY (id);


--
-- Name: electiontask electiontask_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.electiontask
    ADD CONSTRAINT electiontask_pkey PRIMARY KEY (id);


--
-- Name: email email_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.email
    ADD CONSTRAINT email_pkey PRIMARY KEY (email_id);


--
-- Name: emailhistory emailhistory_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.emailhistory
    ADD CONSTRAINT emailhistory_pkey PRIMARY KEY (emailhistory_id);


--
-- Name: ipaccess ipaccess_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.ipaccess
    ADD CONSTRAINT ipaccess_pkey PRIMARY KEY (ipaccess_id);


--
-- Name: jointelection jointelection_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.jointelection
    ADD CONSTRAINT jointelection_pkey PRIMARY KEY (jointelection_id);


--
-- Name: nomination nomination_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.nomination
    ADD CONSTRAINT nomination_pkey PRIMARY KEY (id);


--
-- Name: organization organization_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.organization
    ADD CONSTRAINT organization_pkey PRIMARY KEY (id);


--
-- Name: sync_audit sync_audit_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sync_audit
    ADD CONSTRAINT sync_audit_pkey PRIMARY KEY (id);


--
-- Name: sync_run sync_run_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sync_run
    ADD CONSTRAINT sync_run_pkey PRIMARY KEY (id);


--
-- Name: parameter parameter_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.parameter
    ADD CONSTRAINT parameter_pkey PRIMARY KEY (key);


--
-- Name: supportnomination supportnomination_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.supportnomination
    ADD CONSTRAINT supportnomination_pkey PRIMARY KEY (id);


--
-- Name: nomination uke5lokejwprcloxonttmml2em7; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.nomination
    ADD CONSTRAINT uke5lokejwprcloxonttmml2em7 UNIQUE (candidate_id);


--
-- Name: candidate_election_task_progress ukoc96oic3w7cnjtmw0ixal1rqr; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.candidate_election_task_progress
    ADD CONSTRAINT ukoc96oic3w7cnjtmw0ixal1rqr UNIQUE (candidate_id, election_task_id);


--
-- Name: useradmin useradmin_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.useradmin
    ADD CONSTRAINT useradmin_pkey PRIMARY KEY (useradmin_id);


--
-- Name: uservoter uservoter_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.uservoter
    ADD CONSTRAINT uservoter_pkey PRIMARY KEY (uservoter_id);


--
-- Name: vote vote_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.vote
    ADD CONSTRAINT vote_pkey PRIMARY KEY (vote_id);


--
-- Name: auditorcandidatedecision fk18abwuy57fsnhmn0k6u17uuig; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.auditorcandidatedecision
    ADD CONSTRAINT fk18abwuy57fsnhmn0k6u17uuig FOREIGN KEY (candidate_id) REFERENCES public.candidate(candidate_id);


--
-- Name: auditor_results auditor_results_election_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.auditor_results
    ADD CONSTRAINT auditor_results_election_id_fkey FOREIGN KEY (election_id) REFERENCES public.election(election_id) ON DELETE CASCADE;


--
-- Name: candidate_question fk1fltlr6kw3g2dvxcwii0q5vui; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.candidate_question
    ADD CONSTRAINT fk1fltlr6kw3g2dvxcwii0q5vui FOREIGN KEY (election_id) REFERENCES public.election(election_id);


--
-- Name: electionemailtemplate fk1inuiygkjsk8k09oe56j73rho; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.electionemailtemplate
    ADD CONSTRAINT fk1inuiygkjsk8k09oe56j73rho FOREIGN KEY (election_id) REFERENCES public.election(election_id);


--
-- Name: sync_audit fk24080ie8syk2w4gvc139u0xoi; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sync_audit
    ADD CONSTRAINT fk24080ie8syk2w4gvc139u0xoi FOREIGN KEY (election_id) REFERENCES public.election(election_id);


--
-- Name: supportnomination fk2gy7ylwue7b1a7uao4wbmc1m8; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.supportnomination
    ADD CONSTRAINT fk2gy7ylwue7b1a7uao4wbmc1m8 FOREIGN KEY (supporting_organization_id) REFERENCES public.organization(id);


--
-- Name: auditorcandidatedecision fk3bysuulhtgdpsw9mnsrcc21oy; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.auditorcandidatedecision
    ADD CONSTRAINT fk3bysuulhtgdpsw9mnsrcc21oy FOREIGN KEY (auditor_id) REFERENCES public.auditor(auditor_id);


--
-- Name: auditor fk3jy51sk09c8mbpkptaqsxkhju; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.auditor
    ADD CONSTRAINT fk3jy51sk09c8mbpkptaqsxkhju FOREIGN KEY (election_id) REFERENCES public.election(election_id);


--
-- Name: candidatecountrylink fk3pjdi09rewqg913flyb8gpysk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.candidatecountrylink
    ADD CONSTRAINT fk3pjdi09rewqg913flyb8gpysk FOREIGN KEY (candidate_id) REFERENCES public.candidate(candidate_id);


--
-- Name: uservoter fk4x8dd67rgqo9oi3c1mlc52gqt; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.uservoter
    ADD CONSTRAINT fk4x8dd67rgqo9oi3c1mlc52gqt FOREIGN KEY (election_id) REFERENCES public.election(election_id);


--
-- Name: candidate fk5al0v9ymsja93o20ou3sdjgf1; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.candidate
    ADD CONSTRAINT fk5al0v9ymsja93o20ou3sdjgf1 FOREIGN KEY (election_id) REFERENCES public.election(election_id);


--
-- Name: supportnomination fk5qqvw819v3cchrrwok3sol9sr; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.supportnomination
    ADD CONSTRAINT fk5qqvw819v3cchrrwok3sol9sr FOREIGN KEY (nomination_id) REFERENCES public.nomination(id);


--
-- Name: electioncalendar fk657grgmmh439u1fa41ejtpv4l; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.electioncalendar
    ADD CONSTRAINT fk657grgmmh439u1fa41ejtpv4l FOREIGN KEY (election_id) REFERENCES public.election(election_id);


--
-- Name: electiontask fk67yt0ghtfiv1rk1n5cnlg84hb; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.electiontask
    ADD CONSTRAINT fk67yt0ghtfiv1rk1n5cnlg84hb FOREIGN KEY (election_calendar_id) REFERENCES public.electioncalendar(id);


--
-- Name: candidate_election_task_progress fk8iap3aklnkgokmauk6t8etg2o; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.candidate_election_task_progress
    ADD CONSTRAINT fk8iap3aklnkgokmauk6t8etg2o FOREIGN KEY (candidate_id) REFERENCES public.candidate(candidate_id);


--
-- Name: sync_run fk8x34dwjjc72p40aa52g84nvst; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sync_run
    ADD CONSTRAINT fk8x34dwjjc72p40aa52g84nvst FOREIGN KEY (election_id) REFERENCES public.election(election_id);


--
-- Name: candidate_election_task_progress fkbsmw0hf4d1fh7igo4wwdwhsi6; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.candidate_election_task_progress
    ADD CONSTRAINT fkbsmw0hf4d1fh7igo4wwdwhsi6 FOREIGN KEY (election_task_id) REFERENCES public.electiontask(id);


--
-- Name: nomination fkcv3te3li344u1qfwytxr3mf9k; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.nomination
    ADD CONSTRAINT fkcv3te3li344u1qfwytxr3mf9k FOREIGN KEY (organization_id) REFERENCES public.organization(id);


--
-- Name: vote fkdw6sdbi2b0gefcar4kqtpopfr; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.vote
    ADD CONSTRAINT fkdw6sdbi2b0gefcar4kqtpopfr FOREIGN KEY (uservoter_id) REFERENCES public.uservoter(uservoter_id);


--
-- Name: candidateworkorganization fkewy6lv0kqibo7fl73ft1u7c4j; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.candidateworkorganization
    ADD CONSTRAINT fkewy6lv0kqibo7fl73ft1u7c4j FOREIGN KEY (candidate_id) REFERENCES public.candidate(candidate_id);


--
-- Name: organization fki4bbwnqoo0ay7l61enb8c6mj4; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.organization
    ADD CONSTRAINT fki4bbwnqoo0ay7l61enb8c6mj4 FOREIGN KEY (election_id) REFERENCES public.election(election_id);


--
-- Name: electiontask fkidvwat2r8cr4ccgyvo5c89t1r; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.electiontask
    ADD CONSTRAINT fkidvwat2r8cr4ccgyvo5c89t1r FOREIGN KEY (election_id) REFERENCES public.election(election_id);


--
-- Name: nomination fkilday9to7ky1nkf81jn2vden6; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.nomination
    ADD CONSTRAINT fkilday9to7ky1nkf81jn2vden6 FOREIGN KEY (candidate_id) REFERENCES public.candidate(candidate_id);


--
-- Name: email fklabo8opwhq4261nnt0c4domt8; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.email
    ADD CONSTRAINT fklabo8opwhq4261nnt0c4domt8 FOREIGN KEY (election_id) REFERENCES public.election(election_id);


--
-- Name: vote fklkshhl5gwds8lre8k5k4jw509; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.vote
    ADD CONSTRAINT fklkshhl5gwds8lre8k5k4jw509 FOREIGN KEY (candidate_id) REFERENCES public.candidate(candidate_id);


--
-- Name: vote fkqe0blv72ysqtnysxa38g0lqcj; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.vote
    ADD CONSTRAINT fkqe0blv72ysqtnysxa38g0lqcj FOREIGN KEY (election_id) REFERENCES public.election(election_id);


--
-- Name: candidate_question fkr84x35bv0urkoameefe5x2qyj; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.candidate_question
    ADD CONSTRAINT fkr84x35bv0urkoameefe5x2qyj FOREIGN KEY (candidate_id) REFERENCES public.candidate(candidate_id);


--
-- Name: nomination fks60m94r0ychex4uk2th7t46ef; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.nomination
    ADD CONSTRAINT fks60m94r0ychex4uk2th7t46ef FOREIGN KEY (election_id) REFERENCES public.election(election_id);


--
-- Name: electionrestrictedcountry fkt82tnv8ejh0mq8f8wt6voi691; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.electionrestrictedcountry
    ADD CONSTRAINT fkt82tnv8ejh0mq8f8wt6voi691 FOREIGN KEY (election_id) REFERENCES public.election(election_id);


--
-- PostgreSQL database dump complete
--
