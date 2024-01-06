CREATE SEQUENCE robot_id_seq;
CREATE TABLE robots
(
  id    BIGINT UNIQUE DEFAULT nextval('robot_id_seq'::regclass) NOT NULL,
  color VARCHAR(12)                                             NOT NULL,
  PRIMARY KEY (id)
);
