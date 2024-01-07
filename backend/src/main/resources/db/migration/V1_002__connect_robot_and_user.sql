ALTER TABLE robots
  ADD user_id BIGINT NOT NULL;

ALTER TABLE robots
  ADD CONSTRAINT user_id
    FOREIGN KEY (user_id) REFERENCES users;

CREATE INDEX index_robots_user_id ON robots (user_id);
