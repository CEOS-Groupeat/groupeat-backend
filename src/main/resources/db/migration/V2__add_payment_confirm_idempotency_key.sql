ALTER TABLE payments
    ADD COLUMN confirm_idempotency_key varchar(300);

CREATE UNIQUE INDEX uk_payments_confirm_idempotency_key
    ON payments (confirm_idempotency_key)
    WHERE confirm_idempotency_key IS NOT NULL;
