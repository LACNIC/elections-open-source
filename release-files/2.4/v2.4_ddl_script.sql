CREATE SEQUENCE public.auditor_candidate_decision_seq
    START WITH 90000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE SEQUENCE public.candidate_countrylink_seq
    START WITH 60000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE SEQUENCE public.candidate_election_task_progress_seq
    START WITH 95000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE SEQUENCE public.candidate_question_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE SEQUENCE public.election_event_seq
    START WITH 80000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE SEQUENCE public.election_restricted_country_seq
    START WITH 85000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE SEQUENCE public.nomination_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE SEQUENCE public.organization_seq
    START WITH 90000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE SEQUENCE public.sync_audit_seq
    START WITH 97000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE SEQUENCE public.sync_run_seq
    START WITH 98000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE SEQUENCE public.supportnomination_seq
    START WITH 70000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE SEQUENCE public.task_seq
    START WITH 50000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE SEQUENCE public.workorganization_seq
    START WITH 60000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE public.auditorcandidatedecision (
    id bigint NOT NULL,
    approved_date timestamp(6) without time zone,
    comment character varying(4000),
    decisiondate timestamp(6) without time zone NOT NULL,
    decisionstatus character varying(255) NOT NULL,
    preapproved_date timestamp(6) without time zone,
    auditor_id bigint NOT NULL,
    candidate_id bigint NOT NULL,
    CONSTRAINT auditorcandidatedecision_decisionstatus_check CHECK (((decisionstatus)::text = ANY ((ARRAY['ANALYZING'::character varying, 'PREAPPROVED'::character varying, 'APPROVED'::character varying, 'REJECTED'::character varying, 'NO_APPLY'::character varying])::text[])))
);

CREATE TABLE public.candidate_election_task_progress (
    id bigint NOT NULL,
    enddate timestamp(6) without time zone,
    startdate timestamp(6) without time zone,
    status character varying(255) NOT NULL,
    candidate_id bigint NOT NULL,
    election_task_id bigint NOT NULL,
    CONSTRAINT candidate_election_task_progress_status_check CHECK (((status)::text = ANY ((ARRAY['NOT_STARTED'::character varying, 'STARTED'::character varying, 'COMPLETED'::character varying, 'OMITTED'::character varying])::text[])))
);

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

CREATE TABLE public.candidateworkorganization (
    id bigint NOT NULL,
    organizationgroup character varying(4000),
    organizationname character varying(1000) NOT NULL,
    workorganizationtype character varying(50) NOT NULL,
    candidate_id bigint NOT NULL,
    CONSTRAINT candidateworkorganization_workorganizationtype_check CHECK (((workorganizationtype)::text = ANY ((ARRAY['PAID'::character varying, 'AD_HONOREM'::character varying])::text[])))
);

CREATE TABLE public.electioncalendar (
    id bigint NOT NULL,
    calendarkey character varying(255) NOT NULL,
    enddate timestamp(6) without time zone,
    publicable boolean NOT NULL,
    startdate timestamp(6) without time zone,
    election_id bigint NOT NULL,
    CONSTRAINT electioncalendar_calendarkey_check CHECK (((calendarkey)::text = ANY ((ARRAY['N_23_PERIODO_PADRON_MILACNIC_SYNC'::character varying, 'N_1_SINGLE_CALL_FOR_CANDIDATES_PUBLISHED'::character varying, 'N_2_PERIODO_CALL_FOR_CANDIDATES'::character varying, 'N_3_SINGLE_PADRON_CLOSED'::character varying, 'N_4_SINGLE_PADRON_PUBLISHED'::character varying, 'N_5_PERIODO_PADRON_CLAIMS'::character varying, 'N_6_PERIODO_ADDITIONAL_EVALUATION'::character varying, 'N_7_PERIODO_CANDIDATE_FORMS_VERIFICATION'::character varying, 'N_8_PERIODO_EVALUATIONS_VALIDATION'::character varying, 'N_9_PERIODO_CANDIDATE_CLAIMS'::character varying, 'N_10_PERIODO_CANDIDATE_CLAIMS_RESOLUTION'::character varying, 'N_11_SINGLE_CANDIDATES_PUBLISHED'::character varying, 'N_12_PERIODO_CANDIDATE_QUESTIONS'::character varying, 'N_13_PERIODO_PADRON_CLAIMS_RESOLUTION'::character varying, 'N_14_SINGLE_PADRON_UPDATED'::character varying, 'N_15_PERIODO_CANDIDATE_CLAIMS_BY_CE'::character varying, 'N_16_PERIODO_VOTING'::character varying, 'N_17_SINGLE_PROVISIONAL_RESULTS_PUBLISHED'::character varying, 'N_18_PERIODO_CE_AUDIT'::character varying, 'N_19_PERIODO_VOTER_AUDIT'::character varying, 'N_20_SINGLE_OFFICIAL_RESULTS_NO_CLAIMS_PUBLISHED'::character varying, 'N_21_PERIODO_VOTER_CLAIMS_RESPONSE'::character varying, 'N_22_SINGLE_OFFICIAL_RESULTS_WITH_CLAIMS_PUBLISHED'::character varying])::text[])))
);

CREATE TABLE public.electionrestrictedcountry (
    id bigint NOT NULL,
    countrycode character varying(3) NOT NULL,
    election_id bigint NOT NULL
);

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

CREATE TABLE public.nomination (
    id bigint NOT NULL,
    acceptnominationtoken character varying(1000),
    nominationdate character varying(255),
    nominationemail character varying(255),
    nominationname character varying(255),
    nominationphonenumber character varying(255),
    nominationreason character varying(2000),
    status character varying(255) NOT NULL,
    candidate_id bigint,
    election_id bigint NOT NULL,
    organization_id bigint NOT NULL,
    CONSTRAINT nomination_status_check CHECK (((status)::text = ANY ((ARRAY['PROPOSED'::character varying, 'ACCEPTED_BY_CANDIDATE'::character varying, 'REJECTED_BY_CANDIDATE'::character varying, 'INVALID'::character varying, 'APPROVED'::character varying])::text[])))
);

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

CREATE TABLE public.sync_run (
    id bigint NOT NULL,
    createdrows integer NOT NULL,
    duration_ms bigint,
    health_indicators character varying(1000),
    message character varying(1000),
    processedrows integer NOT NULL,
    status character varying(32) NOT NULL,
    syncat timestamp(6) without time zone NOT NULL,
    syncrunid character varying(64) NOT NULL,
    updatedrows integer NOT NULL,
    ws_elapsed_ms bigint,
    ws_status_code integer,
    election_id bigint NOT NULL
);

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

ALTER TABLE public.auditor
    ADD COLUMN reminder_frequency character varying(64);

ALTER TABLE public.candidate
    ADD COLUMN campuscoursecalification character varying(255),
    ADD COLUMN campuscoursestatus character varying(255),
    ADD COLUMN candidate_type character varying(32),
    ADD COLUMN evaluationstatus character varying(255),
    ADD COLUMN linkedinurl text,
    ADD COLUMN proctorioresultfile bytea,
    ADD COLUMN qadultincountry boolean,
    ADD COLUMN qcanspeakspanish boolean,
    ADD COLUMN qcivilrightslimitation boolean,
    ADD COLUMN q_declaration_aso_knowledge boolean,
    ADD COLUMN q_declaration_competencies boolean,
    ADD COLUMN q_declaration_conflicts_interest boolean,
    ADD COLUMN q_declaration_data_usage boolean,
    ADD COLUMN q_declaration_disciplinary boolean,
    ADD COLUMN q_declaration_dynamic boolean,
    ADD COLUMN q_declaration_iana_knowledge boolean,
    ADD COLUMN q_declaration_incompatibilities boolean,
    ADD COLUMN q_declaration_pep character varying(255),
    ADD COLUMN qhealthmentallimitation boolean,
    ADD COLUMN qhealthtravellimitation boolean,
    ADD COLUMN qlegallimitationanycountry boolean,
    ADD COLUMN qothernonstatutoryanswer1english text,
    ADD COLUMN qothernonstatutoryanswer1portuguese text,
    ADD COLUMN qothernonstatutoryanswer1spanish text,
    ADD COLUMN qotherstatutoryanswer1english text,
    ADD COLUMN qotherstatutoryanswer1portuguese text,
    ADD COLUMN qotherstatutoryanswer1spanish text,
    ADD COLUMN qotherstatutoryanswer2english text,
    ADD COLUMN qotherstatutoryanswer2portuguese text,
    ADD COLUMN qotherstatutoryanswer2spanish text,
    ADD COLUMN qotherstatutoryanswer3english text,
    ADD COLUMN qotherstatutoryanswer3portuguese text,
    ADD COLUMN qotherstatutoryanswer3spanish text,
    ADD COLUMN qotherstatutoryanswer4english text,
    ADD COLUMN qotherstatutoryanswer4portuguese text,
    ADD COLUMN qotherstatutoryanswer4spanish text,
    ADD COLUMN qunemployed boolean,
    ADD COLUMN reminder_frequency character varying(64),
    ADD COLUMN status character varying(255);

ALTER TABLE public.election
    ADD COLUMN defaultrecipient character varying(2000),
    ADD COLUMN public_election_token character varying(1000),
    ADD COLUMN calendarset boolean,
    ADD COLUMN campuscourse bigint,
    ADD COLUMN election_type character varying(255),
    ADD COLUMN isboardelection boolean,
    ADD COLUMN manageorganizationsmanual boolean,
    ADD COLUMN managevotersmanual boolean,
    ADD COLUMN organizationsset boolean,
    ADD COLUMN tasksset boolean;

ALTER TABLE public.email
    ADD COLUMN prioritized boolean;

ALTER TABLE public.emailhistory
    ADD COLUMN prioritized boolean;

ALTER TABLE public.activity
    ALTER COLUMN "timestamp" TYPE timestamp(6) without time zone;

ALTER TABLE public.election
    ALTER COLUMN closed DROP DEFAULT;

ALTER TABLE public.election
    ALTER COLUMN closed DROP NOT NULL;

ALTER TABLE public.election
    ALTER COLUMN closeddate TYPE timestamp(6) without time zone;

ALTER TABLE public.election
    ALTER COLUMN creationdate TYPE timestamp(6) without time zone;

ALTER TABLE public.election
    ADD COLUMN IF NOT EXISTS donominationlinkavailable boolean NOT NULL DEFAULT true;

ALTER TABLE public.election
    ADD COLUMN IF NOT EXISTS nominationtaskslinkavailable boolean NOT NULL DEFAULT true;

ALTER TABLE public.election
    ADD COLUMN IF NOT EXISTS nominationsupportlinkavailable boolean NOT NULL DEFAULT true;

ALTER TABLE public.election
    ADD COLUMN IF NOT EXISTS publicelectionlinkavailable boolean NOT NULL DEFAULT true;

ALTER TABLE public.email
    ALTER COLUMN createddate TYPE timestamp(6) without time zone;

ALTER TABLE public.emailhistory
    ALTER COLUMN createddate TYPE timestamp(6) without time zone;

ALTER TABLE public.ipaccess
    ALTER COLUMN firstattemptdate TYPE timestamp(6) without time zone;

ALTER TABLE public.ipaccess
    ALTER COLUMN lastattemptdate TYPE timestamp(6) without time zone;

ALTER TABLE public.uservoter
    ALTER COLUMN votedate TYPE timestamp(6) without time zone;

ALTER TABLE public.vote
    ALTER COLUMN votedate TYPE timestamp(6) without time zone;

ALTER TABLE ONLY public.customization
    ADD CONSTRAINT customization_pkey PRIMARY KEY (customization_id);

-- Testing mode: se omite el CHECK de activitytype para permitir ActivityType nuevos

ALTER TABLE public.auditor
    ADD CONSTRAINT auditor_reminder_frequency_check CHECK (((reminder_frequency)::text = ANY ((ARRAY['MONDAY_WEDNESDAY_FRIDAY'::character varying, 'TUESDAY_THURSDAY'::character varying, 'MONDAY_TO_FRIDAY'::character varying, 'EVERY_DAY'::character varying, 'DISABLED'::character varying])::text[])));

ALTER TABLE public.candidate
    ADD CONSTRAINT candidate_campuscoursestatus_check CHECK (((campuscoursestatus)::text = ANY ((ARRAY['PENDING'::character varying, 'SENT'::character varying, 'STARTED'::character varying, 'COMPLETED'::character varying, 'NOT_APPLICABLE'::character varying])::text[]))),
    ADD CONSTRAINT candidate_candidate_type_check CHECK (((candidate_type)::text = ANY ((ARRAY['NORMAL'::character varying, 'ABSTENTION'::character varying])::text[]))),
    ADD CONSTRAINT candidate_evaluationstatus_check CHECK (((evaluationstatus)::text = ANY ((ARRAY['PENDING'::character varying, 'CREDENTIALS_REQUESTED'::character varying, 'CREDENTIALS_SENT'::character varying, 'COMPLETED'::character varying, 'NOT_APPLICABLE'::character varying])::text[]))),
    ADD CONSTRAINT candidate_q_declaration_pep_check CHECK (((q_declaration_pep)::text = ANY ((ARRAY['NOT_PEP'::character varying, 'PEP'::character varying])::text[]))),
    ADD CONSTRAINT candidate_reminder_frequency_check CHECK (((reminder_frequency)::text = ANY ((ARRAY['MONDAY_WEDNESDAY_FRIDAY'::character varying, 'TUESDAY_THURSDAY'::character varying, 'MONDAY_TO_FRIDAY'::character varying, 'EVERY_DAY'::character varying, 'DISABLED'::character varying])::text[]))),
    ADD CONSTRAINT candidate_status_check CHECK (((status)::text = ANY ((ARRAY['INCOMPLETE'::character varying, 'PRECOMPLETE'::character varying, 'COMPLETE'::character varying, 'CONFIRMED_AND_PUBLISHED'::character varying, 'REJECTED'::character varying])::text[])));

ALTER TABLE public.election
    ADD CONSTRAINT election_category_check CHECK (((category)::text = ANY ((ARRAY['STATUTORY'::character varying, 'MODERATORS'::character varying, 'OTHER'::character varying, 'TEST'::character varying])::text[]))),
    ADD CONSTRAINT election_election_type_check CHECK (((election_type)::text = ANY ((ARRAY['BOARD'::character varying, 'IANA'::character varying, 'ASO'::character varying, 'MODERATORS'::character varying, 'FISCAL_COMMISSION'::character varying, 'ELECTORAL_COMMISSION'::character varying, 'OTHER'::character varying])::text[])));

ALTER TABLE ONLY public.auditor
    DROP CONSTRAINT fk53knnkifx5fqgm1jgcheuwsyo,
    DROP CONSTRAINT fkene41r5h8dgxtaa0hka4ao8e8;

ALTER TABLE ONLY public.candidate
    DROP CONSTRAINT fk7sa6ixqcdhp53qkjrwituhwct,
    DROP CONSTRAINT fk9728k0jhg0l5qw3folv2smh34;

ALTER TABLE ONLY public.electionemailtemplate
    DROP CONSTRAINT fk60dab3b6rn3m1y85b3hvqdpkv,
    DROP CONSTRAINT fk96wiao69tqccwuhdextjjx7a2;

ALTER TABLE ONLY public.email
    DROP CONSTRAINT fkf3wiqomx3ab7qo9353qblqgy5,
    DROP CONSTRAINT fkpntyqelk43ka0s3ffg16p2sms;

ALTER TABLE ONLY public.uservoter
    DROP CONSTRAINT fk1do2ntjih65b8p4dgf7yxpsjj,
    DROP CONSTRAINT fkn8xln3ppeqiq1d9i6hggnybln;

ALTER TABLE ONLY public.vote
    DROP CONSTRAINT fk1rxcmws4uua549obakp1s7qqu,
    DROP CONSTRAINT fk4x75bejx7sq67bk5tajblmrhf,
    DROP CONSTRAINT fkiwnuc9dco1kvcsj4p0004mvmr,
    DROP CONSTRAINT fkj0kgemrk6tfc3n4nw6smlu9y3;

ALTER TABLE ONLY public.auditor
    ADD CONSTRAINT fk3jy51sk09c8mbpkptaqsxkhju FOREIGN KEY (election_id) REFERENCES public.election(election_id);

ALTER TABLE ONLY public.candidate
    ADD CONSTRAINT fk5al0v9ymsja93o20ou3sdjgf1 FOREIGN KEY (election_id) REFERENCES public.election(election_id);

ALTER TABLE ONLY public.electionemailtemplate
    ADD CONSTRAINT fk1inuiygkjsk8k09oe56j73rho FOREIGN KEY (election_id) REFERENCES public.election(election_id);

ALTER TABLE ONLY public.email
    ADD CONSTRAINT fklabo8opwhq4261nnt0c4domt8 FOREIGN KEY (election_id) REFERENCES public.election(election_id);

ALTER TABLE ONLY public.uservoter
    ADD CONSTRAINT fk4x8dd67rgqo9oi3c1mlc52gqt FOREIGN KEY (election_id) REFERENCES public.election(election_id);

ALTER TABLE ONLY public.vote
    ADD CONSTRAINT fkdw6sdbi2b0gefcar4kqtpopfr FOREIGN KEY (uservoter_id) REFERENCES public.uservoter(uservoter_id);

ALTER TABLE ONLY public.vote
    ADD CONSTRAINT fklkshhl5gwds8lre8k5k4jw509 FOREIGN KEY (candidate_id) REFERENCES public.candidate(candidate_id);

ALTER TABLE ONLY public.vote
    ADD CONSTRAINT fkqe0blv72ysqtnysxa38g0lqcj FOREIGN KEY (election_id) REFERENCES public.election(election_id);

ALTER TABLE ONLY public.auditorcandidatedecision
    ADD CONSTRAINT auditorcandidatedecision_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.candidate_election_task_progress
    ADD CONSTRAINT candidate_election_task_progress_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.candidate_election_task_progress
    ADD CONSTRAINT ukoc96oic3w7cnjtmw0ixal1rqr UNIQUE (candidate_id, election_task_id);

ALTER TABLE ONLY public.candidate_question
    ADD CONSTRAINT candidate_question_pkey PRIMARY KEY (candidate_question_id);

ALTER TABLE ONLY public.candidatecountrylink
    ADD CONSTRAINT candidatecountrylink_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.candidateworkorganization
    ADD CONSTRAINT candidateworkorganization_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.electioncalendar
    ADD CONSTRAINT electioncalendar_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.electionrestrictedcountry
    ADD CONSTRAINT electionrestrictedcountry_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.electiontask
    ADD CONSTRAINT electiontask_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.nomination
    ADD CONSTRAINT nomination_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.nomination
    ADD CONSTRAINT uke5lokejwprcloxonttmml2em7 UNIQUE (candidate_id);

ALTER TABLE ONLY public.organization
    ADD CONSTRAINT organization_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.sync_audit
    ADD CONSTRAINT sync_audit_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.sync_run
    ADD CONSTRAINT sync_run_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.supportnomination
    ADD CONSTRAINT supportnomination_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.auditorcandidatedecision
    ADD CONSTRAINT fk18abwuy57fsnhmn0k6u17uuig FOREIGN KEY (candidate_id) REFERENCES public.candidate(candidate_id);

ALTER TABLE ONLY public.auditorcandidatedecision
    ADD CONSTRAINT fk3bysuulhtgdpsw9mnsrcc21oy FOREIGN KEY (auditor_id) REFERENCES public.auditor(auditor_id);

ALTER TABLE ONLY public.candidate_election_task_progress
    ADD CONSTRAINT fk8iap3aklnkgokmauk6t8etg2o FOREIGN KEY (candidate_id) REFERENCES public.candidate(candidate_id);

ALTER TABLE ONLY public.candidate_election_task_progress
    ADD CONSTRAINT fkbsmw0hf4d1fh7igo4wwdwhsi6 FOREIGN KEY (election_task_id) REFERENCES public.electiontask(id);

ALTER TABLE ONLY public.candidate_question
    ADD CONSTRAINT fk1fltlr6kw3g2dvxcwii0q5vui FOREIGN KEY (election_id) REFERENCES public.election(election_id);

ALTER TABLE ONLY public.candidate_question
    ADD CONSTRAINT fkr84x35bv0urkoameefe5x2qyj FOREIGN KEY (candidate_id) REFERENCES public.candidate(candidate_id);

ALTER TABLE ONLY public.candidatecountrylink
    ADD CONSTRAINT fk3pjdi09rewqg913flyb8gpysk FOREIGN KEY (candidate_id) REFERENCES public.candidate(candidate_id);

ALTER TABLE ONLY public.candidateworkorganization
    ADD CONSTRAINT fkewy6lv0kqibo7fl73ft1u7c4j FOREIGN KEY (candidate_id) REFERENCES public.candidate(candidate_id);

ALTER TABLE ONLY public.electioncalendar
    ADD CONSTRAINT fk657grgmmh439u1fa41ejtpv4l FOREIGN KEY (election_id) REFERENCES public.election(election_id);

ALTER TABLE ONLY public.electionrestrictedcountry
    ADD CONSTRAINT fkt82tnv8ejh0mq8f8wt6voi691 FOREIGN KEY (election_id) REFERENCES public.election(election_id);

ALTER TABLE ONLY public.electiontask
    ADD CONSTRAINT fk67yt0ghtfiv1rk1n5cnlg84hb FOREIGN KEY (election_calendar_id) REFERENCES public.electioncalendar(id);

ALTER TABLE ONLY public.electiontask
    ADD CONSTRAINT fkidvwat2r8cr4ccgyvo5c89t1r FOREIGN KEY (election_id) REFERENCES public.election(election_id);

ALTER TABLE ONLY public.nomination
    ADD CONSTRAINT fkcv3te3li344u1qfwytxr3mf9k FOREIGN KEY (organization_id) REFERENCES public.organization(id);

ALTER TABLE ONLY public.nomination
    ADD CONSTRAINT fkilday9to7ky1nkf81jn2vden6 FOREIGN KEY (candidate_id) REFERENCES public.candidate(candidate_id);

ALTER TABLE ONLY public.nomination
    ADD CONSTRAINT fks60m94r0ychex4uk2th7t46ef FOREIGN KEY (election_id) REFERENCES public.election(election_id);

ALTER TABLE ONLY public.organization
    ADD CONSTRAINT fki4bbwnqoo0ay7l61enb8c6mj4 FOREIGN KEY (election_id) REFERENCES public.election(election_id);

ALTER TABLE ONLY public.sync_audit
    ADD CONSTRAINT fk24080ie8syk2w4gvc139u0xoi FOREIGN KEY (election_id) REFERENCES public.election(election_id);

ALTER TABLE ONLY public.sync_run
    ADD CONSTRAINT fk8x34dwjjc72p40aa52g84nvst FOREIGN KEY (election_id) REFERENCES public.election(election_id);

ALTER TABLE ONLY public.supportnomination
    ADD CONSTRAINT fk2gy7ylwue7b1a7uao4wbmc1m8 FOREIGN KEY (supporting_organization_id) REFERENCES public.organization(id);

ALTER TABLE ONLY public.supportnomination
    ADD CONSTRAINT fk5qqvw819v3cchrrwok3sol9sr FOREIGN KEY (nomination_id) REFERENCES public.nomination(id);
