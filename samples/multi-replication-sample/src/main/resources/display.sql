CREATE TABLE display (
  id    INTEGER AUTO_INCREMENT,
  title VARCHAR(200),
  PRIMARY KEY (id),
  UNIQUE KEY uk_display_title(title)
);

INSERT INTO display(title) VALUES('display');

