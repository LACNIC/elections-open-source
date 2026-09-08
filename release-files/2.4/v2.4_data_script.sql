UPDATE public.email
SET prioritized = false
WHERE prioritized IS NULL;

UPDATE public.emailhistory
SET prioritized = false
WHERE prioritized IS NULL;

UPDATE public.auditor
SET reminder_frequency = 'DISABLED'
WHERE reminder_frequency IS NULL;

UPDATE public.candidate
SET candidate_type = COALESCE(candidate_type, 'NORMAL'),
    status = COALESCE(status, 'CONFIRMED_AND_PUBLISHED'),
    reminder_frequency = COALESCE(reminder_frequency, 'DISABLED');

UPDATE public.election
SET public_election_token = COALESCE(
        public_election_token,
        md5(election_id::text || clock_timestamp()::text || random()::text)
        || md5(random()::text || clock_timestamp()::text || election_id::text)
    ),
    election_type = CASE
        WHEN category = 'MODERATORS' THEN 'MODERATORS'
        ELSE 'BOARD'
    END,
    isboardelection = CASE
        WHEN category = 'MODERATORS' THEN false
        ELSE true
    END,
    manageorganizationsmanual = true,
    managevotersmanual = true,
    organizationsset = false,
    calendarset = false,
    tasksset = false;

WITH calendar_seed(calendar_key, board_default, moderators_default) AS (
    VALUES
        ('N_23_PERIODO_PADRON_MILACNIC_SYNC', true, true),
        ('N_1_SINGLE_CALL_FOR_CANDIDATES_PUBLISHED', false, false),
        ('N_2_PERIODO_CALL_FOR_CANDIDATES', true, true),
        ('N_3_SINGLE_PADRON_CLOSED', false, false),
        ('N_4_SINGLE_PADRON_PUBLISHED', true, true),
        ('N_5_PERIODO_PADRON_CLAIMS', false, false),
        ('N_6_PERIODO_ADDITIONAL_EVALUATION', true, false),
        ('N_7_PERIODO_CANDIDATE_FORMS_VERIFICATION', true, true),
        ('N_8_PERIODO_EVALUATIONS_VALIDATION', true, false),
        ('N_9_PERIODO_CANDIDATE_CLAIMS', false, false),
        ('N_10_PERIODO_CANDIDATE_CLAIMS_RESOLUTION', false, false),
        ('N_11_SINGLE_CANDIDATES_PUBLISHED', true, true),
        ('N_12_PERIODO_CANDIDATE_QUESTIONS', true, true),
        ('N_13_PERIODO_PADRON_CLAIMS_RESOLUTION', false, false),
        ('N_14_SINGLE_PADRON_UPDATED', false, false),
        ('N_15_PERIODO_CANDIDATE_CLAIMS_BY_CE', false, false),
        ('N_16_PERIODO_VOTING', true, true),
        ('N_17_SINGLE_PROVISIONAL_RESULTS_PUBLISHED', true, true),
        ('N_18_PERIODO_CE_AUDIT', true, true),
        ('N_19_PERIODO_VOTER_AUDIT', false, false),
        ('N_20_SINGLE_OFFICIAL_RESULTS_NO_CLAIMS_PUBLISHED', false, false),
        ('N_21_PERIODO_VOTER_CLAIMS_RESPONSE', false, false),
        ('N_22_SINGLE_OFFICIAL_RESULTS_WITH_CLAIMS_PUBLISHED', false, false)
),
election_seed AS (
    SELECT
        e.election_id,
        e.election_type,
        COALESCE(e.startdate, e.enddate, e.creationdate) AS default_date,
        COALESCE(e.startdate, e.enddate, e.creationdate) AS voting_start,
        COALESCE(e.enddate, e.startdate, e.creationdate) AS voting_end
    FROM public.election e
)
INSERT INTO public.electioncalendar (id, calendarkey, enddate, publicable, startdate, election_id)
SELECT
    nextval('public.election_event_seq'),
    seed.calendar_key,
    CASE
        WHEN seed.calendar_key = 'N_16_PERIODO_VOTING' THEN es.voting_end
        WHEN (es.election_type = 'MODERATORS' AND seed.moderators_default)
            OR (es.election_type <> 'MODERATORS' AND seed.board_default) THEN es.default_date
        ELSE NULL
    END,
    false,
    CASE
        WHEN seed.calendar_key = 'N_16_PERIODO_VOTING' THEN es.voting_start
        WHEN (es.election_type = 'MODERATORS' AND seed.moderators_default)
            OR (es.election_type <> 'MODERATORS' AND seed.board_default) THEN es.default_date
        ELSE NULL
    END,
    es.election_id
FROM election_seed es
CROSS JOIN calendar_seed seed
LEFT JOIN public.electioncalendar existing
    ON existing.election_id = es.election_id
    AND existing.calendarkey = seed.calendar_key
WHERE existing.id IS NULL;

WITH task_seed(task_key, dependency_level, display_order, board_enabled, moderators_enabled, target_calendar_key) AS (
    VALUES
        ('PROFILE', 'LEVEL_1', 10, true, true, 'N_2_PERIODO_CALL_FOR_CANDIDATES'),
        ('COUNTRIES', 'LEVEL_1', 20, true, true, 'N_2_PERIODO_CALL_FOR_CANDIDATES'),
        ('INCOMPATIBILITIES', 'LEVEL_1', 30, true, true, 'N_2_PERIODO_CALL_FOR_CANDIDATES'),
        ('ORG_SUPPORTS', 'LEVEL_2', 40, true, false, 'N_2_PERIODO_CALL_FOR_CANDIDATES'),
        ('USER_SUPPORTS_2', 'LEVEL_2', 50, false, true, 'N_2_PERIODO_CALL_FOR_CANDIDATES'),
        ('ORGANIZATIONS', 'LEVEL_2', 70, true, true, 'N_2_PERIODO_CALL_FOR_CANDIDATES'),
        ('COURSE', 'LEVEL_2', 80, true, false, 'N_2_PERIODO_CALL_FOR_CANDIDATES'),
        ('OTHER_STATUTORY_QUESTIONS', 'LEVEL_2', 90, true, false, 'N_2_PERIODO_CALL_FOR_CANDIDATES'),
        ('OTHER_NON_STATUTORY_QUESTIONS', 'LEVEL_2', 100, false, true, 'N_2_PERIODO_CALL_FOR_CANDIDATES'),
        ('DECLARATIONS', 'LEVEL_2', 110, true, true, 'N_2_PERIODO_CALL_FOR_CANDIDATES'),
        ('EVALUATION', 'LEVEL_3', 130, true, false, 'N_6_PERIODO_ADDITIONAL_EVALUATION')
),
election_seed AS (
    SELECT
        e.election_id,
        e.election_type
    FROM public.election e
)
INSERT INTO public.electiontask (id, dependencylevel, displayorder, publicable, taskkey, election_id, election_calendar_id)
SELECT
    nextval('public.task_seq'),
    seed.dependency_level,
    seed.display_order,
    false,
    seed.task_key,
    es.election_id,
    calendar.id
FROM election_seed es
JOIN task_seed seed
    ON (es.election_type = 'MODERATORS' AND seed.moderators_enabled)
    OR (es.election_type <> 'MODERATORS' AND seed.board_enabled)
JOIN public.electioncalendar calendar
    ON calendar.election_id = es.election_id
    AND calendar.calendarkey = seed.target_calendar_key
LEFT JOIN public.electiontask existing
    ON existing.election_id = es.election_id
    AND existing.taskkey = seed.task_key
WHERE existing.id IS NULL;

UPDATE public.election e
SET organizationsset = false,
    calendarset = EXISTS (
        SELECT 1
        FROM public.electioncalendar c
        WHERE c.election_id = e.election_id
    ),
    tasksset = EXISTS (
        SELECT 1
        FROM public.electiontask t
        WHERE t.election_id = e.election_id
    );

ALTER TABLE public.election DROP COLUMN enddate;
ALTER TABLE public.election DROP COLUMN startdate;

CREATE TEMP TABLE parameter_backup AS
SELECT *
FROM public.parameter;

DELETE FROM public.parameter;

\ir ../ref/parameter_NEW.sql

INSERT INTO public.parameter (key, value)
SELECT backup.key, backup.value
FROM parameter_backup backup
LEFT JOIN public.parameter current_parameter
    ON current_parameter.key = backup.key
WHERE current_parameter.key IS NULL;

DROP TABLE parameter_backup;

-- Preserve election-specific templates by moving them out of the reference ID range
-- before reloading the global base templates.
SELECT setval(
    'public.electionemailtemplate_seq',
    GREATEST(
        COALESCE((SELECT MAX(electionemailtemplate_id) FROM public.electionemailtemplate), 1),
        50000
    )
);

WITH election_scoped_templates AS (
    SELECT electionemailtemplate_id
    FROM public.electionemailtemplate
    WHERE election_id IS NOT NULL
    ORDER BY electionemailtemplate_id
)
UPDATE public.electionemailtemplate template
SET electionemailtemplate_id = nextval('public.electionemailtemplate_seq')
FROM election_scoped_templates scoped
WHERE template.electionemailtemplate_id = scoped.electionemailtemplate_id;

DELETE FROM public.electionemailtemplate
WHERE election_id IS NULL;

\ir ../ref/electionemailtemplate_NEW.sql

-- Realign all application sequences against the current max value in each table.
-- If a table is empty, keep the sequence ready to emit its configured START WITH.
SELECT setval('public.activity_seq', COALESCE((SELECT MAX(activity_id) FROM public.activity), 1), EXISTS (SELECT 1 FROM public.activity));
SELECT setval('public.auditor_candidate_decision_seq', COALESCE((SELECT MAX(id) FROM public.auditorcandidatedecision), 90000), EXISTS (SELECT 1 FROM public.auditorcandidatedecision));
SELECT setval('public.auditor_seq', COALESCE((SELECT MAX(auditor_id) FROM public.auditor), 1), EXISTS (SELECT 1 FROM public.auditor));
SELECT setval('public.candidate_countrylink_seq', COALESCE((SELECT MAX(id) FROM public.candidatecountrylink), 60000), EXISTS (SELECT 1 FROM public.candidatecountrylink));
SELECT setval('public.candidate_election_task_progress_seq', COALESCE((SELECT MAX(id) FROM public.candidate_election_task_progress), 95000), EXISTS (SELECT 1 FROM public.candidate_election_task_progress));
SELECT setval('public.candidate_question_seq', COALESCE((SELECT MAX(candidate_question_id) FROM public.candidate_question), 1), EXISTS (SELECT 1 FROM public.candidate_question));
SELECT setval('public.candidate_seq', COALESCE((SELECT MAX(candidate_id) FROM public.candidate), 1), EXISTS (SELECT 1 FROM public.candidate));
SELECT setval('public.commissioner_seq', COALESCE((SELECT MAX(commissioner_id) FROM public.commissioner), 1), EXISTS (SELECT 1 FROM public.commissioner));
SELECT setval('public.customization_seq', COALESCE((SELECT MAX(customization_id) FROM public.customization), 1), EXISTS (SELECT 1 FROM public.customization));
SELECT setval('public.election_event_seq', COALESCE((SELECT MAX(id) FROM public.electioncalendar), 80000), EXISTS (SELECT 1 FROM public.electioncalendar));
SELECT setval('public.election_restricted_country_seq', COALESCE((SELECT MAX(id) FROM public.electionrestrictedcountry), 85000), EXISTS (SELECT 1 FROM public.electionrestrictedcountry));
SELECT setval('public.election_seq', COALESCE((SELECT MAX(election_id) FROM public.election), 1), EXISTS (SELECT 1 FROM public.election));
SELECT setval('public.electionemailtemplate_seq', COALESCE((SELECT MAX(electionemailtemplate_id) FROM public.electionemailtemplate), 500), EXISTS (SELECT 1 FROM public.electionemailtemplate));
SELECT setval('public.email_seq', COALESCE((SELECT MAX(email_id) FROM public.email), 1), EXISTS (SELECT 1 FROM public.email));
SELECT setval('public.ipaccess_seq', COALESCE((SELECT MAX(ipaccess_id) FROM public.ipaccess), 1), EXISTS (SELECT 1 FROM public.ipaccess));
SELECT setval('public.jointelection_seq', COALESCE((SELECT MAX(jointelection_id) FROM public.jointelection), 1), EXISTS (SELECT 1 FROM public.jointelection));
SELECT setval('public.nomination_seq', COALESCE((SELECT MAX(id) FROM public.nomination), 10000), EXISTS (SELECT 1 FROM public.nomination));
SELECT setval('public.organization_seq', COALESCE((SELECT MAX(id) FROM public.organization), 90000), EXISTS (SELECT 1 FROM public.organization));
SELECT setval('public.sync_audit_seq', COALESCE((SELECT MAX(id) FROM public.sync_audit), 97000), EXISTS (SELECT 1 FROM public.sync_audit));
SELECT setval('public.sync_run_seq', COALESCE((SELECT MAX(id) FROM public.sync_run), 98000), EXISTS (SELECT 1 FROM public.sync_run));
SELECT setval('public.supportnomination_seq', COALESCE((SELECT MAX(id) FROM public.supportnomination), 70000), EXISTS (SELECT 1 FROM public.supportnomination));
SELECT setval('public.task_seq', COALESCE((SELECT MAX(id) FROM public.electiontask), 50000), EXISTS (SELECT 1 FROM public.electiontask));
SELECT setval('public.uservoter_seq', COALESCE((SELECT MAX(uservoter_id) FROM public.uservoter), 1), EXISTS (SELECT 1 FROM public.uservoter));
SELECT setval('public.vote_seq', COALESCE((SELECT MAX(vote_id) FROM public.vote), 1), EXISTS (SELECT 1 FROM public.vote));
SELECT setval('public.workorganization_seq', COALESCE((SELECT MAX(id) FROM public.candidateworkorganization), 60000), EXISTS (SELECT 1 FROM public.candidateworkorganization));
