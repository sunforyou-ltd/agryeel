-- common
INSERT INTO common(
            common_class, common_seq, common_name)
    VALUES (65, 0, '表示しない'),
           (65, 1, '表示する');
-- farm_status
ALTER TABLE farm_status ADD COLUMN yuko_seibun_input integer;
UPDATE farm_status SET yuko_seibun_input = 0;
UPDATE farm_status SET yuko_seibun_input = 1 WHERE farm_id = 3;

-- work_diary_sanpu
ALTER TABLE work_diary_sanpu ADD COLUMN yuko_seibun text;
UPDATE work_diary_sanpu SET yuko_seibun = '';

-- work_plan_sanpu
ALTER TABLE work_plan_sanpu ADD COLUMN yuko_seibun text;
UPDATE work_plan_sanpu SET yuko_seibun = '';

-- ikubyo_diary_sanpu
ALTER TABLE ikubyo_diary_sanpu ADD COLUMN yuko_seibun text;
UPDATE ikubyo_diary_sanpu SET yuko_seibun = '';

-- ikubyo_plan_sanpu
ALTER TABLE ikubyo_plan_sanpu ADD COLUMN yuko_seibun text;
UPDATE ikubyo_plan_sanpu SET yuko_seibun = '';
