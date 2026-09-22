ALTER TABLE images DROP CONSTRAINT fk_image_item;

ALTER TABLE images
ADD CONSTRAINT fk_image_item FOREIGN KEY (item_id) REFERENCES items (id) ON DELETE CASCADE;


