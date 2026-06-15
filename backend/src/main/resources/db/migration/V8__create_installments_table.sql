CREATE TABLE installments (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    transaction_id UUID NOT NULL REFERENCES transactions(id) ON DELETE CASCADE,
    current_installment INT NOT NULL,
    total_installments INT NOT NULL,
    due_date DATE NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    is_paid BOOLEAN DEFAULT false,
    paid_date DATE,
    CHECK (current_installment <= total_installments)
);

CREATE INDEX idx_installments_transaction_id ON installments(transaction_id);
CREATE INDEX idx_installments_due_date ON installments(due_date);
