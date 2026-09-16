DROP INDEX idx_reviews_public_visibility;

ALTER TABLE reviews DROP COLUMN reviewee_visibility;

CREATE INDEX idx_reviews_public_visibility ON reviews (
    moderation_status,
    reviewer_visibility
);
