
-- non-transactional
PRAGMA foreign_keys = ON;

CREATE TABLE history_folders (
  path          text PRIMARY KEY,
  updated_at    integer NOT NULL
);

CREATE INDEX idx_history_folders_updated_at ON history_folders (updated_at);
