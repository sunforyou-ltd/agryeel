-- Table: account_ikubyo_status

-- DROP TABLE account_ikubyo_status;

CREATE TABLE account_ikubyo_status
(
  account_id text NOT NULL,
  select_start_date timestamp without time zone,
  select_end_date timestamp without time zone,
  select_work_id text,
  select_account_id text,
  select_crop_id character varying(1000),
  select_hinsyu_id character varying(1000),
  select_working integer,
  ssn_crop character varying(1000),
  ssn_hinsyu character varying(1000),
  ssn_seiiku_f bigint,
  ssn_seiiku_t bigint,
  delete_flag integer,
  CONSTRAINT account_ikubyo_status_pkey PRIMARY KEY (account_id)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE account_ikubyo_status
  OWNER TO agryell;

INSERT INTO account_ikubyo_status(
    account_id, select_start_date, select_end_date, select_work_id, select_account_id, select_crop_id, select_hinsyu_id, select_working, ssn_crop, ssn_hinsyu, ssn_seiiku_f, ssn_seiiku_t, delete_flag)
    SELECT account_id, null, null, '', '', '', '', 0, '', '', 0, 0, 0 FROM account	

-- Table: ikubyo_diary

-- DROP TABLE ikubyo_diary;

CREATE TABLE ikubyo_diary
(
  ikubyo_diary_id double precision NOT NULL,
  work_id double precision,
  nae_no text,
  account_id character varying(255),
  work_date date,
  work_time integer,
  detail_setting_kind smallint,
  combi_id double precision,
  kiki_id double precision,
  attachment_id double precision,
  hinsyu_id text COLLATE pg_catalog."default",
  belto_id double precision,
  kansui_part integer,
  kansui_space double precision,
  kansui_method integer,
  kansui_ryo double precision,
  nae_status_update integer,
  work_remark character varying(384) COLLATE pg_catalog."default",
  work_start_time timestamp without time zone,
  work_end_time timestamp without time zone,
  number_of_steps bigint,
  distance double precision,
  calorie integer,
  heart_rate integer,
  nae_suryo double precision,
  kosu double precision,
  youki_id double precision,
  baido_id double precision,
  baido_suryo double precision,
  fukudo_id double precision,
  fukudo_suryo double precision,
  senteiHeight double precision,
  haiki_ryo double precision,
  CONSTRAINT ikubyo_diary_pkey PRIMARY KEY (ikubyo_diary_id)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE ikubyo_diary
  OWNER TO agryell;

-- Table: ikubyo_diary_detail

-- DROP TABLE ikubyo_diary_detail;

CREATE TABLE ikubyo_diary_detail
(
  ikubyo_diary_id double precision NOT NULL,
  ikubyo_diary_sequence double precision NOT NULL,
  work_detail_kind integer,
  nae_no text,
  CONSTRAINT ikubyo_diary_detail_pkey PRIMARY KEY (ikubyo_diary_id, ikubyo_diary_sequence)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE ikubyo_diary_detail
  OWNER TO agryell;

-- Table: ikubyo_diary_sanpu

-- DROP TABLE ikubyo_diary_sanpu;

CREATE TABLE ikubyo_diary_sanpu
(
  ikubyo_diary_id double precision NOT NULL,
  ikubyo_diary_sequence double precision NOT NULL,
  sanpu_method integer,
  kiki_id double precision,
  attachment_id double precision,
  nouhi_id double precision,
  bairitu double precision,
  sanpuryo double precision,
  nae_status_update integer,
  CONSTRAINT ikubyo_diary_sanpu_pkey PRIMARY KEY (ikubyo_diary_id, ikubyo_diary_sequence)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE ikubyo_diary_sanpu
  OWNER TO agryell;

-- Table: ikubyo_line

-- DROP TABLE ikubyo_line;

CREATE TABLE ikubyo_line
(
  ikubyo_diary_id double precision NOT NULL,
  update_time timestamp without time zone,
  work_date date,
  message character varying(2000),
  time_line_color character varying(255) COLLATE pg_catalog."default",
  work_id double precision,
  work_name character varying(255) COLLATE pg_catalog."default",
  nae_no text,
  account_id character varying(255) COLLATE pg_catalog."default",
  account_name character varying(255) COLLATE pg_catalog."default",
  farm_id double precision,
  work_start_time timestamp without time zone,
  work_end_time timestamp without time zone,
  number_of_steps bigint,
  distance double precision,
  calorie integer,
  heart_rate integer,
  CONSTRAINT ikubyo_line_pkey PRIMARY KEY (ikubyo_diary_id)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE ikubyo_line
  OWNER TO agryell;

CREATE INDEX ikubyo_line_idx1
    ON ikubyo_line USING btree
    (account_id ASC NULLS LAST)
    TABLESPACE pg_default;

CREATE INDEX ikubyo_line_idx2
    ON ikubyo_line USING btree
    (farm_id ASC NULLS LAST)
    TABLESPACE pg_default;

-- Table: youki

-- DROP TABLE youki;

CREATE TABLE youki
(
  youki_id double precision NOT NULL,
  youki_name character varying(255),
  youki_kind integer,
  farm_id double precision,
  unit_kind integer,
  kosu double precision,
  kingaku double precision,
  delete_flag integer,
  CONSTRAINT youki_pkey PRIMARY KEY (youki_id)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE youki
  OWNER TO agryell;

CREATE INDEX youki_idx1
    ON youki USING btree
    (farm_id ASC NULLS LAST)
    TABLESPACE pg_default;

-- Table: soil

-- DROP TABLE soil;

CREATE TABLE soil
(
  soil_id double precision NOT NULL,
  soil_name character varying(255),
  soil_kind integer,
  farm_id double precision,
  unit_kind integer,
  kingaku double precision,
  delete_flag integer,
  CONSTRAINT soil_pkey PRIMARY KEY (soil_id)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE soil
  OWNER TO agryell;

CREATE INDEX soil_idx1
    ON soil USING btree
    (farm_id ASC NULLS LAST)
    TABLESPACE pg_default;

-- Table: nae_no_manage

-- DROP TABLE nae_no_manage;

CREATE TABLE nae_no_manage
(
  farm_id double precision NOT NULL,
  sequence_value double precision,
  CONSTRAINT nae_no_manage_pkey PRIMARY KEY (farm_id)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE nae_no_manage
  OWNER TO agryell;

-- Table: nae_status

-- DROP TABLE nae_status;

CREATE TABLE nae_status
(
  nae_no character varying(255) NOT NULL,
  hinsyu_id double precision,
  crop_id double precision,
  hinsyu_name character varying(255),
  hashu_date date,
  seiiku_day_count integer,
  zaiko_suryo double precision,
  kosu double precision,
  now_end_work character varying(255),
  final_disinfection_date date,
  final_kansui_date date,
  final_tuihi_date date,
  total_disinfection_count integer,
  total_kansui_count integer,
  total_tuihi_count integer,
  total_solar_radiation double precision,
  total_disinfection_number bigint,
  total_kansui_number bigint,
  total_tuihi_number bigint,
  end_work_id double precision,
  final_end_date date,
  next_work_id double precision,
  work_color character varying(255) COLLATE pg_catalog."default",
  kataduke_date date,
  pest_id double precision,
  pest_generation integer,
  pest_integrated_kion double precision,
  prev_calc_date date,
  pest_predict_date date,
  target_sanpu_date date,
  delete_flag smallint,
  CONSTRAINT nae_status_pkey PRIMARY KEY (nae_no)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE nae_status
  OWNER TO agryell;

CREATE INDEX nae_status_idx1
    ON nae_status USING btree
    (hinsyu_id ASC NULLS LAST)
    TABLESPACE pg_default;

CREATE INDEX nae_status_idx2
    ON nae_status USING btree
    (crop_id ASC NULLS LAST)
    TABLESPACE pg_default;
