ALTER TABLE `user` ADD `research` BOOL AFTER `enabled`;
ALTER TABLE `user` ADD `communication` BOOL AFTER `research`;

UPDATE USER SET research = false WHERE research IS NULL;
UPDATE USER SET communication = false WHERE communication IS NULL;
