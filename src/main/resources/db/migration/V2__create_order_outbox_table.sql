CREATE TABLE order_outbox (
                              id BIGINT AUTO_INCREMENT PRIMARY KEY,

                              aggregate_id VARCHAR(255) NOT NULL,
                              event_type   VARCHAR(100) NOT NULL,
                              payload      JSON NOT NULL,

                              status       VARCHAR(20) NOT NULL DEFAULT 'PENDING',
                              retry_count  INT NOT NULL DEFAULT 0,
                              next_retry_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                              created_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              processed_at TIMESTAMP NULL,

                              version BIGINT NOT NULL DEFAULT 0
);