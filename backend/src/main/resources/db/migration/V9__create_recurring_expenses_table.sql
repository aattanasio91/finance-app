CREATE TABLE recurring_expenses (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    category_id UUID REFERENCES categories(id),
    name VARCHAR(255) NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    day_of_month INT NOT NULL CHECK (day_of_month BETWEEN 1 AND 31),
    frequency VARCHAR(20) DEFAULT 'MONTHLY' CHECK (frequency IN ('MONTHLY','BIMONTHLY','QUARTERLY','YEARLY')),
    is_active BOOLEAN DEFAULT true,
    next_date DATE,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_recurring_expenses_user_id ON recurring_expenses(user_id);
