ALTER TABLE reports
ADD CONSTRAINT fk_reports_resolved_by FOREIGN KEY (resolved_by) REFERENCES users (id) ON DELETE SET NULL;

ALTER TABLE items
ADD CONSTRAINT fk_items_moderated_by FOREIGN KEY (moderated_by) REFERENCES users (id) ON DELETE SET NULL;

ALTER TABLE blogs
ADD CONSTRAINT fk_blogs_moderated_by FOREIGN KEY (moderated_by) REFERENCES users (id) ON DELETE SET NULL;

ALTER TABLE reviews
ADD CONSTRAINT fk_reviews_moderated_by FOREIGN KEY (moderated_by) REFERENCES users (id) ON DELETE SET NULL;
