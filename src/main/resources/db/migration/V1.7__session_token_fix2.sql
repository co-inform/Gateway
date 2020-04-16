ALTER TABLE `sessiontoken` DROP FOREIGN KEY `sessiontoken_ibfk_1`;
DROP INDEX `user_id` ON `sessiontoken`;
CREATE INDEX `user_id` ON `sessiontoken`(`user_id`);
ALTER TABLE `sessiontoken` ADD FOREIGN KEY (`user_id`) REFERENCES `user` (`id`);
