CREATE TABLE pending_image_uploads (
    id UUID PRIMARY KEY,
    owner_id UUID NOT NULL,
    object_key VARCHAR(512) NOT NULL UNIQUE,
    original_name VARCHAR(255) NOT NULL,
    mime_type VARCHAR(100) NOT NULL,
    size BIGINT NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    status VARCHAR(20) NOT NULL,
    CONSTRAINT fk_pending_image_owner FOREIGN KEY (owner_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_pending_image_uploads_owner ON pending_image_uploads (owner_id);

CREATE INDEX idx_pending_image_uploads_expiry ON pending_image_uploads (expires_at);
