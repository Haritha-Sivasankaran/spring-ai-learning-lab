CREATE TABLE usage_quotas (
    id VARCHAR(255) PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    quota_date DATE NOT NULL,
    call_count INTEGER NOT NULL DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uq_user_date UNIQUE (user_id, quota_date)
);
