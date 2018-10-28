CREATE TABLE IF NOT EXISTS display (
  id    INTEGER AUTO_INCREMENT,
  title VARCHAR(200),
  PRIMARY KEY (id),
  UNIQUE KEY uk_display_title(title)
);

INSERT INTO display(title) VALUES ('main');


CREATE TABLE IF NOT EXISTS product (
  id    INTEGER AUTO_INCREMENT,
  title VARCHAR(200),
  PRIMARY KEY (id),
  UNIQUE KEY uk_product_title(title)
);

INSERT INTO product(title) VALUES ('main');
