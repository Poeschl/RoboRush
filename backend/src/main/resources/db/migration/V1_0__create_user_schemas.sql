CREATE SEQUENCE user_id_seq;
CREATE TABLE users
(
  id            BIGINT UNIQUE DEFAULT nextval('user_id_seq'::regclass) NOT NULL,
  username      VARCHAR(255) UNIQUE                                    NOT NULL,
  password      VARCHAR(60)                                            NOT NULL,
  registered_at TIMESTAMP                                              NOT NULL,
  PRIMARY KEY (id)
);
CREATE INDEX index_users_username ON users (username);
