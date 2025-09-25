CREATE TABLE IF NOT EXISTS t_orders
(
    `id`         BIGINT(20) AUTO_INCREMENT PRIMARY KEY,
    `order_nbr`  VARCHAR(255) DEFAULT NULL,
    `sku_code`   VARCHAR(255),
    `price`      DECIMAL(19, 2),
    `quantity`   INT(11),
    `email`      VARCHAR(255),
    `first_name` VARCHAR(255),
    `last_name`  VARCHAR(255)
);
