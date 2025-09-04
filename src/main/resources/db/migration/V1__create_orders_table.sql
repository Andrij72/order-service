CREATE TABLE IF NOT EXISTS t_orders
(
    `id`          BIGINT(20) NOT NULL AUTO_INCREMENT,
    `order_nbr`   VARCHAR(255) DEFAULT NULL,
    `customer_id` VARCHAR(255),
    `price`       DECIMAL(19, 2),
    `quantity`    INT(11),
    PRIMARY KEY (`id`)
);
