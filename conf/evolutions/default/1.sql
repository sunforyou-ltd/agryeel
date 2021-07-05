# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table account (
  account_id                varchar(255),
  password                  varchar(255),
  acount_name               varchar(255),
  acount_kana               varchar(255),
  part                      bigint,
  remark                    varchar(255),
  mail_address              varchar(255),
  birthday                  date,
  manager_role              integer,
  menu_role                 bigint,
  google_id                 varchar(255),
  farm_id                   float,
  account_picture           bytea,
  first_page                integer,
  login_count               float,
  input_count               float,
  clip_mode                 integer,
  heart_rate_up_limit       integer,
  heart_rate_down_limit     integer,
  field_id                  float,
  work_id                   float,
  work_start_time           timestamp not null)
;

create table attachment (
  attachment_id             float,
  attachement_name          varchar(255),
  katasiki                  varchar(255),
  attachment_kind           integer)
;

create table belto (
  belto_id                  float,
  belto_name                varchar(255))
;

create table belto_of_farm (
  belto_id                  float,
  farm_id                   float)
;

create table clip_group (
  clip_group_id             float,
  clip_group_name           varchar(255),
  farm_id                   float)
;

create table clip_group_list (
  clip_group_id             float,
  kukaku_id                 float)
;

create table clip_of_account (
  account_id                varchar(255),
  clip_group_id             float)
;

create table common (
  common_class              integer,
  common_seq                integer,
  common_name               varchar(255))
;

create table compartment (
  kukaku_id                 float,
  kukaku_name               varchar(255),
  farm_id                   float,
  field_id                  float,
  area                      float,
  soil_quality              bigint,
  frontage                  float,
  depth                     float)
;

create table compartment_status (
  kukaku_id                 float,
  work_year                 bigint,
  rotation_speed_of_year    integer,
  hinsyu_id                 float,
  crop_id                   float,
  hinsyu_name               varchar(255),
  hashu_date                date,
  seiiku_day_count          integer,
  now_end_work              varchar(255),
  final_disinfection_date   date,
  final_kansui_date         date,
  final_tuihi_date          date,
  shukaku_start_date        date,
  shukaku_end_date          date,
  total_disinfection_count  integer,
  total_kansui_count        integer,
  total_tuihi_count         integer,
  total_shukaku_count       float,
  total_solar_radiation     float,
  total_disinfection_number bigint,
  total_kansui_number       bigint,
  total_tuihi_number        bigint,
  total_shukaku_number      bigint,
  old_disinfection_count    integer,
  old_kansui_count          integer,
  old_tuihi_count           integer,
  old_shukaku_count         float,
  old_solar_radiation       float,
  now_work_mode             smallint,
  end_work_id               float,
  final_end_date            date,
  next_work_id              float,
  work_color                varchar(255),
  kataduke_date             date)
;

create table compartment_work_chain_status (
  kukaku_id                 float,
  work_chain_id             float,
  farm_id                   float,
  crop_id                   float,
  work_end_flag             integer)
;

create table crop (
  crop_id                   float,
  crop_name                 varchar(255),
  crop_color                varchar(255))
;

create table crop_group (
  crop_group_id             float,
  crop_group_name           varchar(255),
  farm_id                   float)
;

create table crop_group_list (
  crop_group_id             float,
  crop_id                   float)
;

create table farm (
  farm_id                   float,
  farm_name                 varchar(255),
  farm_group_id             float,
  representative_name       varchar(255),
  post_no                   varchar(255),
  prefectures               varchar(255),
  address                   varchar(255),
  tel                       varchar(255),
  responsible_mobile_tel    varchar(255),
  fax                       varchar(255),
  mail_address_pc           varchar(255),
  mail_address_mobile       varchar(255),
  url                       varchar(255),
  registration_code         varchar(255))
;

create table farm_group (
  farm_group_id             float,
  farm_group_name           varchar(255))
;

create table field (
  field_id                  float,
  field_name                varchar(255),
  farm_id                   float,
  landlord_id               float,
  post_no                   varchar(255),
  prefectures               varchar(255),
  address                   varchar(255),
  geography                 bigint,
  area                      float,
  soil_quality              bigint,
  contract_date             timestamp,
  contract_end_date         timestamp,
  contract_type             bigint,
  rent                      float)
;

create table field_group (
  field_group_id            float,
  field_group_name          varchar(255),
  field_group_color         varchar(255),
  farm_id                   float)
;

create table field_group_list (
  field_group_id            float,
  field_id                  float,
  sequence_id               bigint)
;

create table field_group_where (
  field_group_id            float,
  account_id                varchar(255),
  flag                      integer)
;

create table hinsyu (
  hinsyu_id                 float,
  hinsyu_name               varchar(255),
  crop_id                   float)
;

create table hinsyu_of_farm (
  hinsyu_id                 float,
  farm_id                   float)
;

create table kiki (
  kiki_id                   float,
  kiki_name                 varchar(255),
  katasiki                  varchar(255),
  maker                     varchar(255),
  kiki_kind                 integer,
  on_use_attachment_id      varchar(255))
;

create table kiki_of_farm (
  kiki_id                   float,
  farm_id                   float,
  on_use_attachment_id      varchar(255))
;

create table landlord (
  landlord_id               float,
  landlord_name             varchar(255),
  post_no                   varchar(255),
  prefectures               varchar(255),
  address                   varchar(255),
  tel                       varchar(255),
  responsible_mobile_tel    varchar(255),
  fax                       varchar(255),
  mail_address_pc           varchar(255),
  mail_address_mobile       varchar(255),
  bank_name                 varchar(255),
  account_type              bigint,
  account_number            float,
  payment_date              timestamp)
;

create table message_of_account (
  account_id                varchar(255),
  update_time               timestamp not null)
;

create table motocho_base (
  kukaku_id                 float,
  kukaku_name               varchar(255),
  kukaku_group_color        varchar(255),
  work_year                 integer,
  rotation_speed_of_year    integer,
  hinsyu_id                 float,
  hinsyu_name               varchar(255),
  hashu_date                date,
  seiiku_day_count          integer,
  shukaku_ryo               float,
  shukaku_start_date        date,
  shukaku_end_date          date,
  crop_id                   float,
  crop_name                 varchar(255),
  work_start_day            date,
  work_end_day              date,
  cliping1                  smallint,
  cliping2                  smallint,
  cliping3                  smallint,
  cliping4                  smallint,
  cliping5                  smallint,
  motocho_flag              smallint)
;

create table motocho_hiryo (
  kukaku_id                 float,
  work_year                 integer,
  rotation_speed_of_year    integer,
  nouhi_id                  float,
  hiryo_no                  float,
  nouhi_name                varchar(255),
  nouhi_group_id            float,
  nouhi_group_name          varchar(255),
  sanpu_date                date,
  bairitu                   float,
  sanpuryo                  float,
  sanpu_method              integer,
  yushi_seibun              varchar(255),
  g_unit_value              varchar(255),
  n                         float,
  p                         float,
  k                         float,
  unit                      varchar(255))
;

create table motocho_nouyaku (
  kukaku_id                 float,
  work_year                 integer,
  rotation_speed_of_year    integer,
  nouhi_id                  float,
  nouyaku_no                float,
  nouhi_name                varchar(255),
  nouhi_group_id            float,
  nouhi_group_name          varchar(255),
  sanpu_date                date,
  bairitu                   float,
  sanpuryo                  float,
  sanpu_method              integer,
  yushi_seibun              varchar(255),
  g_unit_value              varchar(255),
  n                         float,
  p                         float,
  k                         float,
  unit                      varchar(255))
;

create table nouhi (
  nouhi_id                  float,
  nouhi_name                varchar(255),
  nouhi_kind                integer,
  bairitu                   float,
  sanpuryo                  float,
  farm_id                   float,
  unit_kind                 integer,
  n                         float,
  p                         float,
  k                         float,
  lower                     integer,
  upper                     integer,
  final_day                 integer,
  sanpu_count               float,
  use_when                  float)
;

create table nouhi_of_crop (
  work_chain_id             float,
  crop_id                   float,
  nouhi_id                  float)
;

create table sanpu_combi (
  sanpu_combi_id            float,
  farm_id                   float)
;

create table sanpu_combi_item (
  sanpu_combi_id            float,
  sequence_id               integer,
  kiki_id                   float,
  attachment_id             float,
  nouhi_id                  float)
;

create table search_where (
  account_id                varchar(255),
  work_id                   float,
  tantou_id                 float,
  flag                      integer)
;

create table sequence (
  sequence_id               integer,
  sequence_value            float)
;

create table sri_account_d (
  account_id                varchar(255),
  work_date                 date,
  total_shukaku_count       integer)
;

create table sri_account_m (
  account_id                varchar(255),
  work_year_month           date,
  total_shukaku_count       integer)
;

create table sri_account_s (
  account_id                varchar(255),
  work_year                 integer,
  rotation_speed_of_year    integer,
  total_shukaku_count       integer)
;

create table sri_account_y (
  account_id                varchar(255),
  work_year                 integer,
  total_shukaku_count       integer)
;

create table sri_hill_d (
  hill_id                   float,
  work_date                 date,
  total_shukaku_count       integer)
;

create table sri_hill_m (
  hill_id                   float,
  work_year_month           date,
  total_shukaku_count       integer)
;

create table sri_hill_s (
  hill_id                   float,
  work_year                 integer,
  rotation_speed_of_year    integer,
  total_shukaku_count       integer)
;

create table sri_hill_y (
  hill_id                   float,
  work_year                 integer,
  total_shukaku_count       integer)
;

create table sri_kukaku_d (
  kukaku_id                 float,
  work_date                 date,
  total_shukaku_count       integer)
;

create table sri_kukaku_m (
  kukaku_id                 float,
  work_year_month           date,
  total_shukaku_count       integer)
;

create table sri_kukaku_s (
  kukaku_id                 float,
  work_year                 integer,
  rotation_speed_of_year    integer,
  total_shukaku_count       integer)
;

create table sri_kukaku_y (
  kukaku_id                 float,
  work_year                 integer,
  total_shukaku_count       integer)
;

create table sri_line_d (
  line_id                   float,
  work_date                 date,
  total_shukaku_count       integer)
;

create table sri_line_m (
  line_id                   float,
  work_year_month           date,
  total_shukaku_count       integer)
;

create table sri_line_s (
  line_id                   float,
  work_year                 integer,
  rotation_speed_of_year    integer,
  total_shukaku_count       integer)
;

create table sri_line_y (
  line_id                   float,
  work_year                 integer,
  total_shukaku_count       integer)
;

create table sri_stock_d (
  stock_id                  float,
  work_date                 date,
  total_shukaku_count       integer)
;

create table sri_stock_m (
  stock_id                  float,
  work_year_month           date,
  total_shukaku_count       integer)
;

create table sri_stock_s (
  stock_id                  float,
  work_year                 integer,
  rotation_speed_of_year    integer,
  total_shukaku_count       integer)
;

create table sri_stock_y (
  stock_id                  float,
  work_year                 integer,
  total_shukaku_count       integer)
;

create table system_message (
  release_date              timestamp,
  message                   varchar(255),
  update_time               timestamp not null)
;

create table time_line (
  time_line_id              float,
  update_time               timestamp,
  work_date                 timestamp,
  message                   varchar(255),
  work_diary_id             float,
  time_line_color           varchar(255),
  work_id                   float,
  work_name                 varchar(255),
  kukaku_id                 float,
  kukaku_name               varchar(255),
  account_id                varchar(255),
  account_name              varchar(255),
  farm_id                   float,
  number_of_steps           bigint,
  distance                  float,
  calorie                   integer,
  heart_rate                integer,
  work_start_time           timestamp not null,
  work_end_time             timestamp not null)
;

create table updown_limit (
  work_id                   float,
  item_numeric_n            integer,
  up_limit                  float,
  down_limit                float)
;

create table work (
  work_id                   float,
  work_name                 varchar(255),
  farm_id                   float,
  work_template_id          float,
  item1                     varchar(255),
  item2                     varchar(255),
  item3                     varchar(255),
  item4                     varchar(255),
  item5                     varchar(255),
  item6                     varchar(255),
  item7                     varchar(255),
  item8                     varchar(255),
  item9                     varchar(255),
  item10                    varchar(255),
  numeric_item1             varchar(255),
  numeric_item2             varchar(255),
  numeric_item3             varchar(255),
  numeric_item4             varchar(255),
  numeric_item5             varchar(255),
  numeric_item6             varchar(255),
  numeric_item7             varchar(255),
  numeric_item8             varchar(255),
  numeric_item9             varchar(255),
  numeric_item10            varchar(255),
  work_picture              bytea,
  work_color                varchar(255),
  work_english              varchar(255))
;

create table work_chain (
  work_chain_id             float,
  work_chain_name           varchar(255),
  farm_id                   float)
;

create table work_chain_item (
  work_chain_id             float,
  sequence_id               integer,
  work_id                   float,
  sanpu_combi_id            float,
  on_use_kiki_kind          varchar(255),
  work_mode                 integer)
;

create table work_compartment (
  account_id                varchar(255),
  kukaku_id                 float,
  work_target               integer)
;

create table work_diary (
  work_diary_id             float,
  work_id                   float,
  kukaku_id                 float,
  hill_id                   float,
  line_id                   float,
  stock_id                  float,
  account_id                varchar(255),
  work_date                 date,
  work_time                 integer,
  shukaku_ryo               float,
  detail_setting_kind       smallint,
  combi_id                  float,
  kiki_id                   float,
  attachment_id             float,
  hinsyu_id                 float,
  belto_id                  float,
  kabuma                    float,
  joukan                    float,
  jousu                     float,
  hukasa                    float,
  kansui_part               integer,
  kansui_space              float,
  kansui_method             integer,
  kansui_ryo                float,
  syukaku_nisugata          integer,
  syukaku_sitsu             integer,
  syukaku_size              integer,
  kukaku_status_update      integer,
  motocho_update            integer,
  work_remark               varchar(255),
  number_of_steps           bigint,
  distance                  float,
  calorie                   integer,
  heart_rate                integer,
  work_start_time           timestamp not null,
  work_end_time             timestamp not null)
;

create table work_diary_sanpu (
  work_diary_id             float,
  work_diary_sequence       float,
  sanpu_method              integer,
  kiki_id                   float,
  attachment_id             float,
  nouhi_id                  float,
  bairitu                   float,
  sanpuryo                  float,
  kukaku_status_update      integer,
  motocho_update            integer)
;

create table work_histry_base (
  farm_id                   float,
  work_id                   float,
  crop_id                   float,
  work_histry_sequence      float,
  sanpu_method              integer,
  kiki_id                   float,
  attachment_id             float,
  nouhi_id                  float,
  bairitu                   float,
  sanpuryo                  float)
;

create table work_last_time (
  farm_id                   float,
  work_id                   float,
  work_time                 integer,
  kansui_ryo                float,
  shukaku_ryo               float,
  kabuma                    float,
  joukan                    float,
  jousu                     float,
  hukasa                    float,
  kiki_id                   float,
  attachment_id             float,
  hinsyu_id                 float,
  belto_id                  float,
  kansui_part               integer,
  kansui_space              float,
  kansui_method             integer,
  syukaku_nisugata          integer,
  syukaku_sitsu             integer,
  syukaku_size              integer)
;

create table work_template (
  work_template_id          float,
  work_template_name        varchar(255))
;




# --- !Downs

drop table if exists account cascade;

drop table if exists attachment cascade;

drop table if exists belto cascade;

drop table if exists belto_of_farm cascade;

drop table if exists clip_group cascade;

drop table if exists clip_group_list cascade;

drop table if exists clip_of_account cascade;

drop table if exists common cascade;

drop table if exists compartment cascade;

drop table if exists compartment_status cascade;

drop table if exists compartment_work_chain_status cascade;

drop table if exists crop cascade;

drop table if exists crop_group cascade;

drop table if exists crop_group_list cascade;

drop table if exists farm cascade;

drop table if exists farm_group cascade;

drop table if exists field cascade;

drop table if exists field_group cascade;

drop table if exists field_group_list cascade;

drop table if exists field_group_where cascade;

drop table if exists hinsyu cascade;

drop table if exists hinsyu_of_farm cascade;

drop table if exists kiki cascade;

drop table if exists kiki_of_farm cascade;

drop table if exists landlord cascade;

drop table if exists message_of_account cascade;

drop table if exists motocho_base cascade;

drop table if exists motocho_hiryo cascade;

drop table if exists motocho_nouyaku cascade;

drop table if exists nouhi cascade;

drop table if exists nouhi_of_crop cascade;

drop table if exists sanpu_combi cascade;

drop table if exists sanpu_combi_item cascade;

drop table if exists search_where cascade;

drop table if exists sequence cascade;

drop table if exists sri_account_d cascade;

drop table if exists sri_account_m cascade;

drop table if exists sri_account_s cascade;

drop table if exists sri_account_y cascade;

drop table if exists sri_hill_d cascade;

drop table if exists sri_hill_m cascade;

drop table if exists sri_hill_s cascade;

drop table if exists sri_hill_y cascade;

drop table if exists sri_kukaku_d cascade;

drop table if exists sri_kukaku_m cascade;

drop table if exists sri_kukaku_s cascade;

drop table if exists sri_kukaku_y cascade;

drop table if exists sri_line_d cascade;

drop table if exists sri_line_m cascade;

drop table if exists sri_line_s cascade;

drop table if exists sri_line_y cascade;

drop table if exists sri_stock_d cascade;

drop table if exists sri_stock_m cascade;

drop table if exists sri_stock_s cascade;

drop table if exists sri_stock_y cascade;

drop table if exists system_message cascade;

drop table if exists time_line cascade;

drop table if exists updown_limit cascade;

drop table if exists work cascade;

drop table if exists work_chain cascade;

drop table if exists work_chain_item cascade;

drop table if exists work_compartment cascade;

drop table if exists work_diary cascade;

drop table if exists work_diary_sanpu cascade;

drop table if exists work_histry_base cascade;

drop table if exists work_last_time cascade;

drop table if exists work_template cascade;

