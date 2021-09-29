--add mail column to candidate table
ALTER TABLE candidate
ADD COLUMN mail  character varying(255) COLLATE pg_catalog."default" 