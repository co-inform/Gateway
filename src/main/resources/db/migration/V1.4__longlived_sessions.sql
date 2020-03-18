CREATE TABLE IF NOT EXISTS `sessiontoken` (
                                          `id` bigint unsigned not null auto_increment unique PRIMARY KEY,
                                          `session_token` varchar(44),
                                          `created_at` datetime,
                                          `user_id` bigint unsigned not null unique
);

ALTER TABLE `sessiontoken` ADD FOREIGN KEY (`user_id`) REFERENCES `user` (`id`);

CREATE UNIQUE INDEX sessiontoken_index ON `sessiontoken`(session_token);
