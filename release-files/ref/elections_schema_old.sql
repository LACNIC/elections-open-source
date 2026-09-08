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
    description text,
    election_id bigint,
    ip character varying(255) NOT NULL,
    username character varying(255) NOT NULL,
    "timestamp" timestamp without time zone NOT NULL,
    activitytype character varying(255) NOT NULL
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
    commissioner boolean,
    agreedconformity boolean,
    revisionavailable boolean,
    migration_id bigint,
    mail character varying(255) NOT NULL,
    name character varying(255) NOT NULL,
    resulttoken character varying(1000),
    election_id bigint NOT NULL
);


ALTER TABLE public.auditor OWNER TO postgres;

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
-- Name: candidate; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.candidate (
    candidate_id bigint NOT NULL,
    biospanish text,
    bioenglish text,
    bioportuguese text,
    pictureinfo bytea NOT NULL,
    pictureextension character varying(255) NOT NULL,
    migration_id bigint,
    linkspanish text,
    linkenglish text,
    linkportuguese text,
    name character varying(1000) NOT NULL,
    picturename character varying(255) NOT NULL,
    candidateorder integer,
    onlysp boolean,
    election_id bigint NOT NULL,
    mail character varying(255)
);


ALTER TABLE public.candidate OWNER TO postgres;

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
    auditorsset boolean,
    randomordercandidates boolean,
    candidatesset boolean,
    category character varying(255),
    descriptionspanish text,
    descriptionenglish text,
    descriptionportuguese text,
    diffutc integer,
    creationdate timestamp without time zone NOT NULL,
    enddate timestamp without time zone NOT NULL,
    startdate timestamp without time zone NOT NULL,
    auditorlinkavailable boolean NOT NULL,
    resultlinkavailable boolean NOT NULL,
    votinglinkavailable boolean NOT NULL,
    migration_id bigint,
    linkspanish text NOT NULL,
    linkenglish text NOT NULL,
    linkportuguese text NOT NULL,
    maxcandidates integer NOT NULL,
    migrated boolean,
    electorsset boolean,
    defaultsender character varying(2000),
    revisionrequest boolean NOT NULL,
    onlysp boolean,
    titlespanish text NOT NULL,
    titleenglish text NOT NULL,
    titleportuguese text NOT NULL,
    resulttoken character varying(1000),
    closed boolean DEFAULT false NOT NULL,
    closeddate timestamp without time zone
);


ALTER TABLE public.election OWNER TO postgres;

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
-- Name: electionemailtemplate; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.electionemailtemplate (
    electionemailtemplate_id bigint NOT NULL,
    subjecten text,
    subjectsp text,
    subjectpt text,
    bodyen text,
    bodysp text,
    bodypt text,
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
-- Name: email; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.email (
    email_id bigint NOT NULL,
    subject text NOT NULL,
    bcc character varying(255),
    cc character varying(255),
    body text NOT NULL,
    sender character varying(255),
    recipients text,
    sent boolean,
    createddate timestamp without time zone,
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
    subject text NOT NULL,
    bcc character varying(255),
    cc character varying(255),
    body text NOT NULL,
    sender character varying(255),
    recipients character varying(255),
    sent boolean,
    createddate timestamp without time zone,
    election_id bigint,
    templatetype character varying(255)
);


ALTER TABLE public.emailhistory OWNER TO postgres;

--
-- Name: ipaccess; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.ipaccess (
    ipaccess_id bigint NOT NULL,
    firstattemptdate timestamp without time zone NOT NULL,
    lastattemptdate timestamp without time zone NOT NULL,
    attemptcount integer NOT NULL,
    ip character varying(255) NOT NULL
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
-- Name: parameter; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.parameter (
    key character varying(255) NOT NULL,
    value text
);


ALTER TABLE public.parameter OWNER TO postgres;

--
-- Name: useradmin; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.useradmin (
    useradmin_id character varying(255) NOT NULL,
    email text,
    authorizedelection_id bigint,
    password text
);


ALTER TABLE public.useradmin OWNER TO postgres;

--
-- Name: uservoter; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.uservoter (
    uservoter_id bigint NOT NULL,
    voteamount integer NOT NULL,
    votedate timestamp without time zone,
    migration_id bigint,
    language character varying(255) NOT NULL,
    mail character varying(255) NOT NULL,
    name character varying(1000) NOT NULL,
    orgid character varying(255),
    country character varying(255),
    votetoken character varying(1000),
    voted boolean NOT NULL,
    election_id bigint NOT NULL,
    version integer
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
    votedate timestamp without time zone,
    ip character varying(255),
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
-- Name: candidate candidate_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.candidate
    ADD CONSTRAINT candidate_pkey PRIMARY KEY (candidate_id);


--
-- Name: commissioner commissioner_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.commissioner
    ADD CONSTRAINT commissioner_pkey PRIMARY KEY (commissioner_id);


--
-- Name: election election_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.election
    ADD CONSTRAINT election_pkey PRIMARY KEY (election_id);


--
-- Name: electionemailtemplate electionemailtemplate_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.electionemailtemplate
    ADD CONSTRAINT electionemailtemplate_pkey PRIMARY KEY (electionemailtemplate_id);


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
-- Name: parameter parameter_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.parameter
    ADD CONSTRAINT parameter_pkey PRIMARY KEY (key);


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
-- Name: uservoter fk1do2ntjih65b8p4dgf7yxpsjj; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.uservoter
    ADD CONSTRAINT fk1do2ntjih65b8p4dgf7yxpsjj FOREIGN KEY (election_id) REFERENCES public.election(election_id);


--
-- Name: vote fk1rxcmws4uua549obakp1s7qqu; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.vote
    ADD CONSTRAINT fk1rxcmws4uua549obakp1s7qqu FOREIGN KEY (uservoter_id) REFERENCES public.uservoter(uservoter_id);


--
-- Name: vote fk4x75bejx7sq67bk5tajblmrhf; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.vote
    ADD CONSTRAINT fk4x75bejx7sq67bk5tajblmrhf FOREIGN KEY (candidate_id) REFERENCES public.candidate(candidate_id);


--
-- Name: auditor fk53knnkifx5fqgm1jgcheuwsyo; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.auditor
    ADD CONSTRAINT fk53knnkifx5fqgm1jgcheuwsyo FOREIGN KEY (election_id) REFERENCES public.election(election_id);


--
-- Name: electionemailtemplate fk60dab3b6rn3m1y85b3hvqdpkv; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.electionemailtemplate
    ADD CONSTRAINT fk60dab3b6rn3m1y85b3hvqdpkv FOREIGN KEY (election_id) REFERENCES public.election(election_id);


--
-- Name: candidate fk7sa6ixqcdhp53qkjrwituhwct; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.candidate
    ADD CONSTRAINT fk7sa6ixqcdhp53qkjrwituhwct FOREIGN KEY (election_id) REFERENCES public.election(election_id);


--
-- Name: electionemailtemplate fk96wiao69tqccwuhdextjjx7a2; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.electionemailtemplate
    ADD CONSTRAINT fk96wiao69tqccwuhdextjjx7a2 FOREIGN KEY (election_id) REFERENCES public.election(election_id);


--
-- Name: candidate fk9728k0jhg0l5qw3folv2smh34; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.candidate
    ADD CONSTRAINT fk9728k0jhg0l5qw3folv2smh34 FOREIGN KEY (election_id) REFERENCES public.election(election_id);


--
-- Name: auditor fkene41r5h8dgxtaa0hka4ao8e8; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.auditor
    ADD CONSTRAINT fkene41r5h8dgxtaa0hka4ao8e8 FOREIGN KEY (election_id) REFERENCES public.election(election_id);


--
-- Name: email fkf3wiqomx3ab7qo9353qblqgy5; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.email
    ADD CONSTRAINT fkf3wiqomx3ab7qo9353qblqgy5 FOREIGN KEY (election_id) REFERENCES public.election(election_id);


--
-- Name: vote fkiwnuc9dco1kvcsj4p0004mvmr; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.vote
    ADD CONSTRAINT fkiwnuc9dco1kvcsj4p0004mvmr FOREIGN KEY (election_id) REFERENCES public.election(election_id);


--
-- Name: vote fkj0kgemrk6tfc3n4nw6smlu9y3; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.vote
    ADD CONSTRAINT fkj0kgemrk6tfc3n4nw6smlu9y3 FOREIGN KEY (election_id) REFERENCES public.election(election_id);


--
-- Name: uservoter fkn8xln3ppeqiq1d9i6hggnybln; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.uservoter
    ADD CONSTRAINT fkn8xln3ppeqiq1d9i6hggnybln FOREIGN KEY (election_id) REFERENCES public.election(election_id);


--
-- Name: email fkpntyqelk43ka0s3ffg16p2sms; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.email
    ADD CONSTRAINT fkpntyqelk43ka0s3ffg16p2sms FOREIGN KEY (election_id) REFERENCES public.election(election_id);


--
-- PostgreSQL database dump complete
--

