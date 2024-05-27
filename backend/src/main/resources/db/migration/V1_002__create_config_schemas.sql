CREATE SEQUENCE config_id_seq;
CREATE TABLE config
(
  id    BIGINT UNIQUE DEFAULT nextval('config_id_seq'::regclass) NOT NULL,
  key   VARCHAR(64)                                              NOT NULL,
  type  VARCHAR(64)                                              NOT NULL,
  value VARCHAR(128)                                             NOT NULL,
  PRIMARY KEY (id)
);

INSERT INTO config (key, type, value) values ('TIMEOUT_WAIT_FOR_PLAYERS', 'DURATION', 'PT1M');
INSERT INTO config (key, type, value) values ('TIMEOUT_WAIT_FOR_ACTION', 'DURATION', 'PT1S');
INSERT INTO config (key, type, value) values ('TIMEOUT_GAME_END', 'DURATION', 'PT2M');
INSERT INTO config (key, type, value) values ('THRESHOLD_NO_ROBOT_ACTION_END_GAME', 'INT', '3');
INSERT INTO config (key, type, value) values ('TARGET_POSITION_IN_GAMEINFO', 'BOOLEAN', 'true');
INSERT INTO config (key, type, value) values ('USE_FOG_OF_WAR', 'BOOLEAN', 'true');
