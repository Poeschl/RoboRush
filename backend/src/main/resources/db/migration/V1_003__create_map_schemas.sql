CREATE SEQUENCE map_id_seq;
CREATE TABLE map
(
  id                       BIGINT UNIQUE DEFAULT nextval('map_id_seq'::regclass) NOT NULL,
  map_name                 TEXT                                                  NOT NULL,
  size                     VARCHAR(9)                                            NOT NULL,
  possible_start_positions TEXT                                                  NOT NULL,
  target_position          VARCHAR(9)                                            NOT NULL,
  active                   BOOLEAN       DEFAULT FALSE                           NOT NULL,
  max_robot_fuel           INTEGER                                               NOT NULL,
  solar_charge_rate        FLOAT                                                 NOT NULL,
  PRIMARY KEY (id)
);

CREATE SEQUENCE tile_id_seq INCREMENT 100;
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

CREATE SEQUENCE played_games_id_seq;
CREATE TABLE played_games
(
  id              BIGINT UNIQUE DEFAULT nextval('played_games_id_seq'::regclass) NOT NULL,
  winner_robot_id BIGINT,
  turns_taken     INT                                                            NOT NULL,
  ended_at        TIMESTAMP                                                      NOT NULL,
  PRIMARY KEY (id),
  FOREIGN KEY (winner_robot_id) REFERENCES robots
);

CREATE INDEX index_played_games_winner_robot ON played_games (winner_robot_id);
