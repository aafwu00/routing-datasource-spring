CREATE TABLE product (
  id    INTEGER AUTO_INCREMENT,
  title VARCHAR(200),
  PRIMARY KEY (id),
  UNIQUE KEY uk_product_title(title)
);

INSERT INTO product(title) VALUES ('product');
