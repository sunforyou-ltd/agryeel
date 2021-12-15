-- work
INSERT INTO public.work(
	work_id, work_name, farm_id, work_template_id, item1, item2, item3, item4, item5, item6, item7, item8, item9, item10, numeric_item1, numeric_item2, numeric_item3, numeric_item4, numeric_item5, numeric_item6, numeric_item7, numeric_item8, numeric_item9, numeric_item10, work_picture, work_color, work_english, worning_per, note_per, danger_per, delete_flag)
	VALUES (209, '肥料散布(作付連携)', 3, 2, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, '0288d1', 'Hiryosanpu', 25, 50, 75, 0);
-- work_chain_item
ALTER TABLE work_chain_item ADD COLUMN auto_start_flag integer;
UPDATE work_chain_item SET auto_start_flag = 0;
INSERT INTO public.work_chain_item(
	work_chain_id, sequence_id, work_id, sanpu_combi_id, on_use_kiki_kind, work_mode, next_sequence_id, nouhi_kind, delete_flag, auto_start_flag)
	VALUES (3, 1, 209, 0, '1,2,4,5', 1, 2, 2, 0, 1);
INSERT INTO public.work_chain_item(
	work_chain_id, sequence_id, work_id, sanpu_combi_id, on_use_kiki_kind, work_mode, next_sequence_id, nouhi_kind, delete_flag, auto_start_flag)
	VALUES (15, 1, 209, 0, '1,2,4,5', 1, 2, 2, 0, 1);
INSERT INTO public.work_chain_item(
	work_chain_id, sequence_id, work_id, sanpu_combi_id, on_use_kiki_kind, work_mode, next_sequence_id, nouhi_kind, delete_flag, auto_start_flag)
	VALUES (16, 1, 209, 0, '1,2,4,5', 1, 2, 2, 0, 1);
