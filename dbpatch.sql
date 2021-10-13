-- V02R2202021XXXX DBPATCH STATEMENT
-- Table: motocho_base
ALTER TABLE motocho_base ALTER COLUMN work_start_day TYPE  timestamp without time zone;
ALTER TABLE motocho_base ALTER COLUMN work_end_day TYPE  timestamp without time zone;

-- Table: compartment_status
ALTER TABLE compartment_status ALTER COLUMN kataduke_date TYPE  timestamp without time zone;
ALTER TABLE compartment_status ALTER COLUMN final_end_date TYPE  timestamp without time zone;

