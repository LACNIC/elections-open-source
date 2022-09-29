ALTER TABLE public.uservoter ADD COLUMN version int4;
UPDATE public.uservoter set version=1;
INSERT INTO public.parameter
(key, value)
VALUES('AUDIT_REPORT_LINK', 'https://github.com/LACNIC/elections-open-source/blob/main/doc/auditReport.pdf');


