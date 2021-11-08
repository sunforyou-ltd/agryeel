 -- Table: motocho_base
 ALTER TABLE motocho_base ALTER COLUMN work_start_day TYPE  timestamp without time zone;
 ALTER TABLE motocho_base ALTER COLUMN work_end_day TYPE  timestamp without time zone;

 -- Table: compartment_status
 ALTER TABLE compartment_status ALTER COLUMN kataduke_date TYPE  timestamp without time zone;
 ALTER TABLE compartment_status ALTER COLUMN final_end_date TYPE  timestamp without time zone;

 -- Table: syukka

 -- DROP TABLE syukka;

 CREATE TABLE syukka
 (
   syukka_no text NOT NULL,
   syukka_date date,
   syukka_saki_id double precision,
   farm_id double precision,
   crop_id double precision,
   syohin_name text,
   origin text,
   place text,
   nisugata_id double precision,
   size_id double precision,
   jyu_ryo double precision,
   irisu double precision,
   tanka double precision,
   syukka_ryo double precision,
   kingaku double precision,
   CONSTRAINT syukka_pkey PRIMARY KEY (syukka_no)
 )
 WITH (
   OIDS=FALSE
 );
 ALTER TABLE syukka
   OWNER TO agryell;

 -- Table: syukka_saki

 -- DROP TABLE syukka_saki;

 CREATE TABLE syukka_saki
 (
   syukka_saki_id double precision,
   syukka_saki_name text,
   farm_id double precision,
   delete_flag smallint,
   CONSTRAINT syukka_saki_pkey PRIMARY KEY (syukka_saki_id)
 )
 WITH (
   OIDS=FALSE
 );
 ALTER TABLE syukka_saki
   OWNER TO agryell;

-- Table: account_status
update account_status
set work_date_auto_set = 2;
