
CREATE TABLE history_copy_items (
  item          text PRIMARY KEY,
  updated_at    integer NOT NULL
);

CREATE INDEX idx_history_copy_items_updated_at
  ON history_copy_items (updated_at);
