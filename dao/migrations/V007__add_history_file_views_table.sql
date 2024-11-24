
CREATE TABLE history_file_views (
  path          text NOT NULL,
  is_edit       boolean NOT NULL,
  encoding      text NOT NULL,
  position      double NOT NULL,
  wrap          boolean,
  column        integer,
  updated_at    integer NOT NULL,
  
  PRIMARY KEY (path, is_edit)
);

CREATE INDEX idx_history_file_views_updated_at ON history_file_views (updated_at);
