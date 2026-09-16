ALTER TABLE reviews DROP CONSTRAINT fk_review_offer;

ALTER TABLE reviews
ADD CONSTRAINT fk_review_offer FOREIGN KEY (offer_id) REFERENCES offers (id) ON DELETE CASCADE;

ALTER TABLE reviews DROP CONSTRAINT fk_review_reviewer;

ALTER TABLE reviews
ADD CONSTRAINT fk_review_reviewer FOREIGN KEY (reviewer_id) REFERENCES users (id) ON DELETE CASCADE;

ALTER TABLE reviews DROP CONSTRAINT fk_review_reviewee;

ALTER TABLE reviews
ADD CONSTRAINT fk_review_reviewee FOREIGN KEY (reviewee_id) REFERENCES users (id) ON DELETE CASCADE;

ALTER TABLE blogs DROP CONSTRAINT fk_blog_author;

ALTER TABLE blogs
ADD CONSTRAINT fk_blog_author FOREIGN KEY (author_id) REFERENCES users (id) ON DELETE CASCADE;
