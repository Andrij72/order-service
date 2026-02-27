CREATE TABLE order_outbox (
                              id BIGINT AUTO_INCREMENT PRIMARY KEY,
                              aggregate_id VARCHAR(255) NOT NULL,
                              event_type VARCHAR(100) NOT NULL,
                              payload JSON NOT NULL,
                              processed BOOLEAN DEFAULT FALSE,
                              created_at TIMESTAMP NOT NULL
);
