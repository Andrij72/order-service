CREATE TABLE t_orders (
                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                          order_number VARCHAR(255) UNIQUE NOT NULL,
                          status VARCHAR(50) NOT NULL DEFAULT 'CREATED',
                          email VARCHAR(255) NOT NULL,
                          first_name VARCHAR(255),
                          last_name VARCHAR(255),
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);


CREATE TABLE t_order_items (
                               id BIGINT AUTO_INCREMENT PRIMARY KEY,
                               order_id BIGINT NOT NULL,
                               sku VARCHAR(255) NOT NULL,
                               price DECIMAL(19,2) NOT NULL,
                               quantity INT NOT NULL,
                               CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES t_orders(id) ON DELETE CASCADE
);
