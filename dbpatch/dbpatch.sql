-- work_chain_item
ALTER TABLE work_chain_item ADD COLUMN auto_start_flag integer;
UPDATE work_chain_item SET auto_start_flag = 0;
