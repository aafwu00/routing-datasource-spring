CREATE TABLE IF NOT EXISTS todo (
  id    INTEGER AUTO_INCREMENT,
  title VARCHAR(200),
  PRIMARY KEY (id),
  UNIQUE KEY uk_title(title)
);

INSERT INTO todo(title) VALUES ('master1');
