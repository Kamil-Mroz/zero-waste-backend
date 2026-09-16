ALTER TABLE items
ADD COLUMN owner_visibility VARCHAR(50) NOT NULL DEFAULT 'VISIBLE';

ALTER TABLE blogs
ADD COLUMN author_visibility VARCHAR(50) NOT NULL DEFAULT 'VISIBLE';

ALTER TABLE offers
ADD COLUMN buyer_visibility VARCHAR(50) NOT NULL DEFAULT 'VISIBLE';

ALTER TABLE reviews
ADD COLUMN reviewer_visibility VARCHAR(50) NOT NULL DEFAULT 'VISIBLE';

ALTER TABLE reviews
ADD COLUMN reviewee_visibility VARCHAR(50) NOT NULL DEFAULT 'VISIBLE';

CREATE INDEX idx_items_public_visibility ON items (
    moderation_status,
    owner_visibility
);

CREATE INDEX idx_blogs_public_visibility ON blogs (
    moderation_status,
    author_visibility
);

CREATE INDEX idx_offers_buyer_visibility ON offers (buyer_visibility);

CREATE INDEX idx_reviews_public_visibility ON reviews (
    moderation_status,
    reviewer_visibility,
    reviewee_visibility
);
