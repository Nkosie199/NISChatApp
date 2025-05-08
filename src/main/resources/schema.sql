CREATE TABLE rooms (
  room_id VARCHAR(50) NOT NULL,
  created_by VARCHAR(30) NOT NULL,
  room_description TEXT,
  link_to_room VARCHAR(100) NOT NULL,
  room_privacy VARCHAR(10) NOT NULL,
  room_password VARCHAR(30) DEFAULT NULL,
  room_start VARCHAR(30) NOT NULL,
  room_size INT NOT NULL DEFAULT 0,
  PRIMARY KEY (room_id)
);

CREATE TABLE messages (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  title VARCHAR(50) NOT NULL,
  content TEXT NOT NULL,
  author VARCHAR(100) NOT NULL,
  link_to_content VARCHAR(500) NOT NULL,
  time_of_upload INT NOT NULL,
  type_of_content VARCHAR(50) NOT NULL,
  size_of_content INT NOT NULL,
  recipient VARCHAR(100) NOT NULL,
  underrated INT NOT NULL DEFAULT 0,
  rated INT NOT NULL DEFAULT 0,
  overrated INT NOT NULL DEFAULT 0,
  general_audiences INT NOT NULL DEFAULT 0,
  parental_guidance_suggested INT NOT NULL DEFAULT 0,
  restricted INT NOT NULL DEFAULT 0
);

CREATE TABLE posts (
  username VARCHAR(50) NOT NULL,
  room_id VARCHAR(50) NOT NULL,
  content VARCHAR(500) NOT NULL,
  created_at INT NOT NULL
);

CREATE TABLE users (
  userid BIGINT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(100) NOT NULL,
  firstname VARCHAR(100) NOT NULL,
  surname VARCHAR(100) NOT NULL,
  email VARCHAR(100) NOT NULL,
  UNIQUE (username)
);

CREATE TABLE words (
  username VARCHAR(50) NOT NULL,
  room_id VARCHAR(50) NOT NULL,
  new_word VARCHAR(50) NOT NULL,
  created_at VARCHAR(30) NOT NULL
);

CREATE TABLE AUTH_USER_GROUP (
  AUTH_USER_GROUP_ID BIGINT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(50) NOT NULL,
  AUTH_GROUP VARCHAR(128) NOT NULL,
  CONSTRAINT USER_AUTH_USER_GROUP_FK FOREIGN KEY (username) REFERENCES users(username),
  UNIQUE (username, AUTH_GROUP)
);
