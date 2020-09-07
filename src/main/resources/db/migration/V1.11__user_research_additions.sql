ALTER TABLE `user` ADD `research` BOOL AFTER `enabled`;
ALTER TABLE `user` ADD `communication` BOOL AFTER `research`;

UPDATE user SET research = false WHERE research IS NULL;
UPDATE user SET communication = false WHERE communication IS NULL;
