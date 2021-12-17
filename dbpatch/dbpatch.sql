-- common
INSERT INTO common(
            common_class, common_seq, common_name)
    VALUES (64, 0, '圃場グループ'),
           (64, 1, '圃場');
-- farm_status
ALTER TABLE farm_status ADD COLUMN kukaku_select_method integer;
UPDATE farm_status SET kukaku_select_method = 0;
UPDATE farm_status SET kukaku_select_method = 1 WHERE farm_id = 3;
