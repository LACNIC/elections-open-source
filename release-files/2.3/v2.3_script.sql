ALTER TABLE public.uservoter ADD COLUMN version int4;
UPDATE public.uservoter set version=1;