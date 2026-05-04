CREATE TABLE IF NOT EXISTS accounts (
    id BIGSERIAL PRIMARY KEY,
    iban VARCHAR(34) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    account_type VARCHAR(20) NOT NULL,
    account_nick_name VARCHAR(100),
    currency VARCHAR(3) NOT NULL,
    balance NUMERIC(19, 2) NOT NULL DEFAULT 0,
    available_balance NUMERIC(19, 2) NOT NULL DEFAULT 0,
    hold_amount NUMERIC(19, 2) NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    closed_at TIMESTAMP,
    closed_reason VARCHAR(500),
    daily_withdrawal_limit NUMERIC(19, 2),
    daily_transfer_limit NUMERIC(19, 2),
    interest_rate NUMERIC(5, 2),
    last_interest_calculated_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_transaction_at TIMESTAMP,
    CONSTRAINT chk_account_type
        CHECK (account_type IN ('SAVINGS', 'CURRENT')),
    CONSTRAINT chk_account_status
        CHECK (status IN ('ACTIVE', 'CLOSED')),
    CONSTRAINT chk_account_currency
        CHECK (currency IN ('USD', 'EUR', 'GBP', 'JPY', 'INR'))
);

CREATE INDEX IF NOT EXISTS idx_iban ON accounts(iban);
CREATE INDEX IF NOT EXISTS idx_user_id ON accounts(user_id);
CREATE INDEX IF NOT EXISTS idx_account_type ON accounts(account_type);

CREATE TABLE IF NOT EXISTS cards (
    id BIGSERIAL PRIMARY KEY,
    card_number VARCHAR(16) NOT NULL UNIQUE,
    cvv VARCHAR(3) NOT NULL,
    expires_at DATE NOT NULL,
    card_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    card_type VARCHAR(20) NOT NULL,
    account_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_used_at TIMESTAMP,
    CONSTRAINT chk_card_status
        CHECK (card_status IN ('ACTIVE', 'BLOCKED', 'FROZEN', 'EXPIRED')),
    CONSTRAINT chk_card_type
        CHECK (card_type IN ('DEBIT', 'CREDIT', 'VIRTUAL')),
    CONSTRAINT fk_cards_account
        FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_card_number ON cards(card_number);
CREATE INDEX IF NOT EXISTS idx_card_account_id ON cards(account_id);
CREATE INDEX IF NOT EXISTS idx_card_status ON cards(card_status);
