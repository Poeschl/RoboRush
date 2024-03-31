CREATE SEQUENCE robot_id_seq;
CREATE TABLE robots
(
  id      BIGINT UNIQUE DEFAULT nextval('robot_id_seq'::regclass) NOT NULL,
  color   VARCHAR(12)                                             NOT NULL,
  user_id BIGINT                                                  NOT NULL,
  PRIMARY KEY (id),
  FOREIGN KEY (user_id) REFERENCES users
);

CREATE INDEX index_robots_user_id ON robots (user_id);
