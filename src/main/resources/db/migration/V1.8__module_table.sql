CREATE TABLE IF NOT EXISTS `moduleinfo` (
    `id` bigint unsigned not null auto_increment unique PRIMARY KEY,
    `modulename` varchar(20),
    `user_id` bigint unsigned not null unique,
    `fail_time` datetime
);

ALTER TABLE `moduleinfo` ADD FOREIGN KEY (`user_id`) REFERENCES `user` (`id`);

CREATE UNIQUE INDEX module_index ON `moduleinfo`(modulename);