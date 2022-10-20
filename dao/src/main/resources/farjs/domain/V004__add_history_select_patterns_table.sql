
CREATE TABLE history_select_patterns (
  item          text PRIMARY KEY,
  updated_at    integer NOT NULL
);

CREATE INDEX idx_history_select_patterns_updated_at
  ON history_select_patterns (updated_at);
