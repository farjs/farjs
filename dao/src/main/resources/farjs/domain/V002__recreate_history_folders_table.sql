
DROP TABLE history_folders;

CREATE TABLE history_folders (
  item          text PRIMARY KEY,
  updated_at    integer NOT NULL
);

CREATE INDEX idx_history_folders_updated_at ON history_folders (updated_at);
