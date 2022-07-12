CREATE USER canal IDENTIFIED BY 'canal';  
GRANT SELECT, REPLICATION SLAVE, REPLICATION CLIENT ON *.* TO 'canal'@'%';
FLUSH PRIVILEGES;

CREATE DATABASE `ad_index_db`;
CREATE TABLE `ad_index_db`.`campaign` (
  `adId` int NOT NULL AUTO_INCREMENT,
  `adName` varchar(256) DEFAULT NULL,
  `status` TINYINT NOT NULL,
  `targets` json DEFAULT NULL,
  PRIMARY KEY (`adId`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4;
INSERT INTO `ad_index_db`.`campaign` VALUES(1,"ad1",0,null);
INSERT INTO `ad_index_db`.`campaign` VALUES(2,"ad2",0,null);
UPDATE `ad_index_db`.`campaign` SET adName="adName1" WHERE adId=1;

