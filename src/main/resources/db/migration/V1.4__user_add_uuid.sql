alter table `user` ADD `uuid` varchar(40) after `counter`;
update user set uuid = UUID() where uuid is null;