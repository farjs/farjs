
CREATE TABLE history_kinds (
  id            integer PRIMARY KEY,
  name          text NOT NULL,
  
  UNIQUE (name)
);

CREATE TABLE history (
  kind_id       integer NOT NULL,
  item          text NOT NULL,
  params        text,
  updated_at    integer NOT NULL,
  
  PRIMARY KEY (kind_id, item),
  FOREIGN KEY (kind_id) REFERENCES history_kinds (id)
);

CREATE INDEX idx_history_kind_id_updated_at ON history (kind_id, updated_at);
