ALTER TABLE public.election ADD COLUMN closed boolean NOT NULL DEFAULT false;
ALTER TABLE public.election ADD COLUMN closeddate timestamp without time zone;
ALTER TABLE public.vote ALTER COLUMN uservoter_id DROP NOT NULL;