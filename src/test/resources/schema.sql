CREATE TABLE IF NOT EXISTS t_orders
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_number VARCHAR(255) UNIQUE NOT NULL,
    status       VARCHAR(50)  NOT NULL DEFAULT 'CREATED',
    email        VARCHAR(255) NOT NULL,
    first_name   VARCHAR(255),
    last_name    VARCHAR(255),

    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    version      BIGINT NOT NULL DEFAULT 0
);


CREATE TABLE IF NOT EXISTS t_order_items
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id     BIGINT         NOT NULL,
    sku          VARCHAR(255)   NOT NULL,
    product_name VARCHAR(255)   NOT NULL,
    price        DECIMAL(19, 2) NOT NULL,
    quantity     INT            NOT NULL,
    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES t_orders (id) ON DELETE CASCADE
);

CREATE TABLE order_outbox (
                              id BIGINT AUTO_INCREMENT PRIMARY KEY,

                              aggregate_id VARCHAR(255) NOT NULL,
                              event_type   VARCHAR(100) NOT NULL,
                              payload      MEDIUMBLOB NOT NULL,

                              status       VARCHAR(20) NOT NULL DEFAULT 'PENDING',
                              retry_count  INT NOT NULL DEFAULT 0,
                              next_retry_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                              created_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              processed_at TIMESTAMP NULL,

                              version BIGINT NOT NULL DEFAULT 0
);