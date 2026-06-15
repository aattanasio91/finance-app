CREATE TABLE categories (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id UUID REFERENCES users(id),
    name VARCHAR(100) NOT NULL,
    type VARCHAR(20) NOT NULL CHECK (type IN ('INCOME','EXPENSE')),
    icon VARCHAR(50),
    color_hex VARCHAR(7),
    is_system BOOLEAN DEFAULT false,
    UNIQUE(user_id, name)
);

CREATE INDEX idx_categories_user_id ON categories(user_id);
CREATE INDEX idx_categories_type ON categories(type);
