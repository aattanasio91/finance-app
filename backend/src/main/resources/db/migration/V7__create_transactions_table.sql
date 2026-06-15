CREATE TABLE transactions (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    account_id UUID NOT NULL REFERENCES accounts(id),
    category_id UUID REFERENCES categories(id),
    merchant_id UUID REFERENCES merchants(id),
    import_job_id UUID,
    parent_transaction_id UUID REFERENCES transactions(id),
    amount DECIMAL(15,2) NOT NULL,
    original_amount DECIMAL(15,2),
    description VARCHAR(500) NOT NULL,
    date DATE NOT NULL,
    type VARCHAR(20) NOT NULL CHECK (type IN ('INCOME','EXPENSE','TRANSFER')),
    currency VARCHAR(3) NOT NULL DEFAULT 'ARS',
    is_manual BOOLEAN DEFAULT false,
    is_recurring BOOLEAN DEFAULT false,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX idx_transactions_user_id ON transactions(user_id);
CREATE INDEX idx_transactions_account_id ON transactions(account_id);
CREATE INDEX idx_transactions_category_id ON transactions(category_id);
CREATE INDEX idx_transactions_date ON transactions(user_id, date);
CREATE INDEX idx_transactions_type ON transactions(type);
