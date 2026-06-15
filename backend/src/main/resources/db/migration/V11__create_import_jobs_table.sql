CREATE TABLE import_jobs (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    source_type VARCHAR(50) NOT NULL CHECK (source_type IN ('CSV_BANK','CSV_CARD','EXCEL_BANK','MANUAL')),
    file_name VARCHAR(255) NOT NULL,
    file_hash VARCHAR(64),
    import_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING','PROCESSING','COMPLETED','FAILED','PARTIAL')),
    total_rows INT DEFAULT 0,
    success_rows INT DEFAULT 0,
    error_rows INT DEFAULT 0,
    error_log TEXT
);

CREATE INDEX idx_import_jobs_user_id ON import_jobs(user_id);
