
INSERT INTO history_kinds (id, name) VALUES (1, 'farjs.mkdirs');
INSERT INTO history_kinds (id, name) VALUES (2, 'farjs.folders');
INSERT INTO history_kinds (id, name) VALUES (3, 'farjs.selectPatterns');
INSERT INTO history_kinds (id, name) VALUES (4, 'farjs.copyItems');

INSERT INTO history (kind_id, item, params, updated_at)
  SELECT 1, item, NULL, updated_at FROM history_mkdirs;

INSERT INTO history (kind_id, item, params, updated_at)
  SELECT 2, item, NULL, updated_at FROM history_folders;

INSERT INTO history (kind_id, item, params, updated_at)
  SELECT 3, item, NULL, updated_at FROM history_select_patterns;

INSERT INTO history (kind_id, item, params, updated_at)
  SELECT 4, item, NULL, updated_at FROM history_copy_items;
