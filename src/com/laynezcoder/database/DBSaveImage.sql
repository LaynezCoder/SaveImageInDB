CREATE DATABASE DBSaveImage;
USE DBSaveImage;

CREATE TABLE Images (
	id INT NOT NULL AUTO_INCREMENT,
    nameImage VARCHAR(200) NOT NULL,
    image LONGBLOB NOT NULL,
    PRIMARY KEY (id)
);
