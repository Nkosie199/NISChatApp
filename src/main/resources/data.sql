INSERT INTO rooms (created_by, room_id, room_description, link_to_room, room_privacy, room_password, room_start, room_size) VALUES
('may', '1', '', 'chatroom_', '', '', '1545595319', 0);

INSERT INTO messages (id, title, content, author, link_to_content, time_of_upload, type_of_content, size_of_content, recipient, underrated, rated, overrated, general_audiences, parental_guidance_suggested, restricted) VALUES
(1, 'reeeeg', 'ergtbtr trbhybh tbhyt', 'whenhesaidthat', 'digital_space_universe_4k_8k-3840x2160.jpg', 1545422241, 'image/jpeg', 877151, '', 0, 0, 0, 0, 0, 0),
(2, 'fbfdbdgbdg', 'dfbdffdbdf', 'thrt', 'hero-commercial-excellence.jpg', 1545497740, 'image/jpeg', 77831, '', 0, 0, 0, 0, 0, 0);

INSERT INTO posts (username, room_id, content, created_at) VALUES
('may', '', 'coming home', 1545672425),
('whenhesaidthat', '', 'yengena', 1545422166),
('whenhesaidthat', '', 'super cold', 1545422336),
('whenhesaidthat', '', 'yeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeey', 1545422385),
('whenhesaidthat', '', 'hahaha lol', 1545423085),
('soframatic', '', 'ujhuihui iuhijoouihijii9joikjplk', 1545490374),
('missme', '', 'hello', 1545499196),
('wow', '', 'hello world lol', 1545591499);

INSERT INTO users (username, firstname, surname, email) VALUES
('whenhesaidthat', 'james', 'brown', 'jbrown@email.com'),
('soframatic', 'james', 'brown', 'jbrown@email.com'),
('fedb', 'james', 'brown', 'jbrown@email.com'),
('may', 'james', 'brown', 'jbrown@email.com'),
('fpmoles', 'james', 'brown', 'jbrown@email.com'),
('jdoe', 'james', 'brown', 'jbrown@email.com');

INSERT INTO words (username, room_id, new_word, created_at) VALUES
('may', '', 'home', '1545672425'),
('may', '', 'coming', '1545672425'),
('whenhesaidthat', '', 'yengena', '1545422166'),
('whenhesaidthat', '', 'cold', '1545422336'),
('whenhesaidthat', '', 'super', '1545422336'),
('whenhesaidthat', '', 'yeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeey', '1545422385'),
('whenhesaidthat', '', 'hahaha', '1545423085'),
('whenhesaidthat', '', 'lol', '1545423085'),
('soframatic', '', 'uihijii9joikjplk', '1545490374'),
('soframatic', '', 'ujhuihui', '1545490374'),
('soframatic', '', 'iuhijoo', '1545490374'),
('missme', '', 'hello', '1545499196'),
('wow', '', 'lol', '1545591499'),
('wow', '', 'hello', '1545591499'),
('wow', '', 'world', '1545591499');

INSERT INTO AUTH_USER_GROUP (username, AUTH_GROUP) VALUES
('fpmoles', 'USER'),
('fpmoles', 'ADMIN'),
('jdoe', 'USER');

COMMIT;
