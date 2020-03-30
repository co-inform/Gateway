CREATE TABLE IF NOT EXISTS `verified` (
    `id` bigint unsigned not null auto_increment unique PRIMARY KEY,
    `token` varchar(36),
    `expiry_date` datetime,
    `user_id` bigint unsigned not null
);

ALTER TABLE `user` ADD `enabled` bool after `id`;
ALTER TABLE `verified` ADD FOREIGN KEY (`user_id`) REFERENCES `user` (`id`);

CREATE UNIQUE INDEX verified_index ON `verified`(`token`);

