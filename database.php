<?php
    $server = '127.0.0.1';
    $user = 'aps';
    $pass = 'aps';
    $database = 'aps';

    try {
        $conn = new PDO('mysql:dbname='.$database.';host='.$server, $user, $pass);
        $conn->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);

        $sql = "CREATE TABLE IF NOT EXISTS `notices` (
            `id` INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
            `username` VARCHAR(100) NOT NULL,
            `useremail` VARCHAR(100) NOT NULL,
            `description` TEXT NOT NULL,
            `image` VARCHAR(100) NULL,
            `lat` DOUBLE(12,10) NOT NULL,
            `lng` DOUBLE(12,10) NOT NULL,
            `created` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        ) ENGINE=INNODB DEFAULT CHARACTER SET = utf8;";

        $conn->exec($sql);
    } catch (PDOException $e) {
        die($e->getMessage());
    }