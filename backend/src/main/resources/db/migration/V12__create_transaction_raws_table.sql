CREATE TABLE transaction_raws (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    import_job_id UUID NOT NULL REFERENCES import_jobs(id),
    original_description VARCHAR(500) NOT NULL,
    original_amount VARCHAR(100),
    original_date VARCHAR(100),
    raw_data JSONB,
    parsed_amount DECIMAL(15,2),
    parsed_date DATE,
    status VARCHAR(20) DEFAULT 'PENDING' CHECK (status IN ('PENDING','PARSED','ERROR','DUPLICATE','IMPORTED')),
    error_message TEXT
);

CREATE INDEX idx_transaction_raws_import_job_id ON transaction_raws(import_job_id);
