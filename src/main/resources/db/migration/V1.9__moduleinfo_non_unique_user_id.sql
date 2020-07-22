ALTER TABLE moduleinfo DROP CONSTRAINT moduleinfo_ibfk_1;
ALTER TABLE moduleinfo DROP CONSTRAINT user_id;
ALTER TABLE moduleinfo ADD FOREIGN KEY (user_id) REFERENCES user(id);
