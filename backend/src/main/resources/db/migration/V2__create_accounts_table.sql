CREATE TABLE accounts (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL CHECK (type IN ('CHECKING','SAVINGS','CREDIT_CARD','CASH','INVESTMENT')),
    currency VARCHAR(3) NOT NULL DEFAULT 'ARS',
    balance DECIMAL(15,2) DEFAULT 0,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    UNIQUE(user_id, name)
);

CREATE INDEX idx_accounts_user_id ON accounts(user_id);
