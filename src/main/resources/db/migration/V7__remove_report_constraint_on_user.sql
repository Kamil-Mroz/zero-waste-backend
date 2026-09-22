ALTER TABLE reports DROP CONSTRAINT fk_report_reporter;

ALTER TABLE reports
ADD CONSTRAINT fk_report_reporter FOREIGN KEY (reporter_id) REFERENCES users (id) ON DELETE CASCADE;
