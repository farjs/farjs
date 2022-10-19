
CREATE TABLE history_mkdirs (
  item          text PRIMARY KEY,
  updated_at    integer NOT NULL
);

CREATE INDEX idx_history_mkdirs_updated_at ON history_mkdirs (updated_at);
