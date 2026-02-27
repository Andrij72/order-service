ALTER TABLE order_outbox
    ADD COLUMN retry_count INT DEFAULT 0,
    ADD COLUMN next_retry_at TIMESTAMP NULL;
