CREATE SEQUENCE map_id_seq;
CREATE TABLE map
(
  id                       BIGINT UNIQUE DEFAULT nextval('map_id_seq'::regclass) NOT NULL,
  map_name                 TEXT                                                  NOT NULL,
  size                     VARCHAR(9)                                            NOT NULL,
  possible_start_positions TEXT                                                  NOT NULL,
  target_position          VARCHAR(9)                                            NOT NULL,
  active                   BOOLEAN       DEFAULT FALSE                           NOT NULL,
  PRIMARY KEY (id)
);

CREATE SEQUENCE tile_id_seq;
CREATE TABLE tile
(
  id       BIGINT UNIQUE DEFAULT nextval('tile_id_seq'::regclass) NOT NULL,
  map_id   BIGINT                                                 NOT NULL,
  position VARCHAR(9)                                             NOT NULL,
  height   INTEGER                                                NOT NULL,
  type     VARCHAR(20)                                            NOT NULL,
  PRIMARY KEY (id),
  FOREIGN KEY (map_id) REFERENCES map
);

CREATE INDEX index_tile_user_id ON tile (map_id);

-- TODO: Add example maps
