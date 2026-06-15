CREATE TABLE credit_cards (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    account_id UUID NOT NULL REFERENCES accounts(id),
    name VARCHAR(255) NOT NULL,
    brand VARCHAR(50) NOT NULL CHECK (brand IN ('VISA','MASTERCARD','AMEX','NARANJA','CABAL')),
    closing_day INT NOT NULL CHECK (closing_day BETWEEN 1 AND 31),
    due_day INT NOT NULL CHECK (due_day BETWEEN 1 AND 31),
    credit_limit DECIMAL(15,2),
    color_hex VARCHAR(7),
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_credit_cards_user_id ON credit_cards(user_id);
