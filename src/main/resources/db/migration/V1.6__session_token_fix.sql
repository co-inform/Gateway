ALTER TABLE `sessiontoken` ADD `counter` MEDIUMINT UNSIGNED AFTER `user_id`;

ALTER TABLE `user` DROP COLUMN `counter`;
