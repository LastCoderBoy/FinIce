CREATE TABLE IF NOT EXISTS transactions (
    id BIGSERIAL PRIMARY KEY,
    transaction_id VARCHAR(50) NOT NULL,
    idempotency_key VARCHAR(100),
    reference VARCHAR(50),
    network_reference VARCHAR(100),
    transaction_type VARCHAR(20) NOT NULL,
    source_account_id BIGINT NOT NULL,
    destination_account_id BIGINT,
    sender_iban VARCHAR(34) NOT NULL,
    receiver_iban VARCHAR(34),
    receiver_name VARCHAR(100),
    transfer_scope VARCHAR(10),
    amount NUMERIC(19, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    status VARCHAR(20) NOT NULL,
    failure_reason VARCHAR(500),
    description VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    created_by BIGINT NOT NULL,
    CONSTRAINT uk_transaction_id UNIQUE (transaction_id),
    CONSTRAINT uk_reference UNIQUE (reference),
    CONSTRAINT uk_idempotency_key UNIQUE (idempotency_key),
    CONSTRAINT chk_transaction_type
        CHECK (transaction_type IN ('DEPOSIT', 'WITHDRAWAL', 'TRANSFER')),
    CONSTRAINT chk_status
        CHECK (status IN ('PENDING', 'COMPLETE', 'FAILED', 'CANCELLED')),
    CONSTRAINT chk_transfer_scope
        CHECK (transfer_scope IS NULL OR transfer_scope IN ('INTERNAL', 'EXTERNAL')),
    CONSTRAINT chk_transfer_requires_scope
        CHECK (
                transaction_type != 'TRANSFER'
                OR transfer_scope IN ('INTERNAL', 'EXTERNAL')
            ),
    CONSTRAINT chk_currency
        CHECK (currency IN ('USD', 'EUR', 'GBP', 'JPY', 'INR'))
);

CREATE INDEX IF NOT EXISTS idx_transaction_id ON transactions(transaction_id);
CREATE INDEX IF NOT EXISTS idx_source_account_id ON transactions(source_account_id);
CREATE INDEX IF NOT EXISTS idx_destination_account_id ON transactions(destination_account_id);
CREATE INDEX IF NOT EXISTS idx_status ON transactions(status);
CREATE INDEX IF NOT EXISTS idx_transaction_type ON transactions(transaction_type);
CREATE INDEX IF NOT EXISTS idx_created_at ON transactions(created_at);
CREATE INDEX IF NOT EXISTS idx_idempotency_key ON transactions(idempotency_key);
CREATE INDEX IF NOT EXISTS idx_created_by ON transactions(created_by);
CREATE INDEX IF NOT EXISTS idx_created_by_created_at ON transactions(created_by, created_at);
