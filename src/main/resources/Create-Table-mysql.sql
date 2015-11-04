CREATE TABLE IF NOT EXISTS `%1s` (
  `id` int(255) NOT NULL AUTO_INCREMENT,
  `uuid` varchar(36) NOT NULL,
  `balance` double(64,2) NOT NULL,
  `status` int(2) NOT NULL DEFAULT '0',
  UNIQUE KEY `uuid` (`uuid`),
  PRIMARY KEY `id` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;