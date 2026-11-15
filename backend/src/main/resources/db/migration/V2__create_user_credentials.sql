
-- ============================================================
-- GameTest Hub
-- Migration: V2
-- Purpose: Secure local authentication credentials
-- ============================================================

CREATE TABLE user_credentials (
    user_id UUID PRIMARY KEY,

    password_hash VARCHAR(255) NOT NULL,

    password_changed_at TIMESTAMPTZ NOT NULL
        DEFAULT CURRENT_TIMESTAMP,

    created_at TIMESTAMPTZ NOT NULL
        DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_user_credentials_user
        FOREIGN KEY (user_id)
        REFERENCES app_users(id)
        ON DELETE CASCADE,

    CONSTRAINT chk_user_credentials_password_hash
        CHECK (length(trim(password_hash)) > 0)
);

CREATE INDEX idx_user_credentials_password_changed_at
    ON user_credentials(password_changed_at);

-- ============================================================
-- End of V2 migration
-- ============================================================
