ALTER TABLE robots
  DROP COLUMN score;

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
