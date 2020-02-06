CREATE TABLE `user` (
                        `id` bigint unsigned not null auto_increment unique PRIMARY KEY,
                        `created_at` datetime
);

CREATE TABLE `password_auth` (
                                 `id` bigint unsigned not null unique PRIMARY KEY,
                                 `email` varchar(320) unique not null,
                                 `password` varchar(60) not null
);

CREATE TABLE `role` (
    `id` bigint unsigned not null auto_increment unique PRIMARY KEY,
    `user_id` bigint unsigned not null,
    `role` enum('ADMIN', 'USER')
);

ALTER TABLE `password_auth` ADD FOREIGN KEY (`id`) REFERENCES `user` (`id`);
ALTER TABLE `role` ADD FOREIGN KEY (`user_id`) REFERENCES `user` (`id`);

create unique index username_index on `password_auth`(`email`);
create index user_index on `role`(`user_id`);
