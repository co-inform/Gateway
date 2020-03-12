CREATE TABLE IF NOT EXISTS `verified` (
    `user_id` bigint unsigned not null unique PRIMARY KEY,
    `token` varchar(44),
    `expiry_date` datetime
);

ALTER TABLE `user` ADD `enabled` bool after `id`;
ALTER TABLE `verified` ADD FOREIGN KEY (`user_id`) REFERENCES `user` (`id`);

CREATE UNIQUE INDEX verified_index ON `verified`(`token`);

