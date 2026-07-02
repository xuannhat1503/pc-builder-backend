USE pc_builder;

SET FOREIGN_KEY_CHECKS = 0;

DELETE FROM build_details;
DELETE FROM reviews;
DELETE FROM crawled_prices;
DELETE FROM builds;
DELETE FROM gpus;
DELETE FROM psus;
DELETE FROM rams;
DELETE FROM mainboards;
DELETE FROM cpus;
DELETE FROM base_components;
DELETE FROM users;

SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO users (id, email, password_hash, role) VALUES
(1, 'admin@pcbuilder.local', '$2a$10$dummyadminhash', 'ADMIN'),
(2, 'longnhat@pcbuilder.local', '$2a$10$dummymemberhash1', 'USER'),
(3, 'reviewer@pcbuilder.local', '$2a$10$dummymemberhash2', 'USER');

INSERT INTO base_components (id, name, brand) VALUES
(1, 'Ryzen 7 7800X3D', 'AMD'),
(2, 'Core i5-13600K', 'Intel'),
(3, 'Ryzen 5 7600', 'AMD'),
(4, 'TUF Gaming X670E-PLUS WIFI', 'ASUS'),
(5, 'PRO B760M-A WIFI', 'MSI'),
(6, 'B650M DS3H', 'Gigabyte'),
(7, 'Fury Beast 32GB DDR5', 'Kingston'),
(8, 'Vengeance 32GB DDR4', 'Corsair'),
(9, 'Thor 16GB DDR5', 'Lexar'),
(10, 'Focus GX-750', 'Seasonic'),
(11, 'MWE 650 Bronze', 'Cooler Master'),
(12, 'Dual RTX 4070 SUPER', 'ASUS'),
(13, 'RX 7800 XT Nitro+', 'Sapphire'),
(14, '990 PRO 1TB', 'Samsung');

INSERT INTO cpus (id, socket_type, tdp_wattage) VALUES
(1, 'AM5', 120),
(2, 'LGA1700', 125),
(3, 'AM5', 65);

INSERT INTO mainboards (id, socket_type, ram_generation) VALUES
(4, 'AM5', 'DDR5'),
(5, 'LGA1700', 'DDR5'),
(6, 'AM5', 'DDR5');

INSERT INTO rams (id, ram_generation, capacity_gb) VALUES
(7, 'DDR5', 32),
(8, 'DDR4', 32),
(9, 'DDR5', 16);

INSERT INTO psus (id, power_output_watt) VALUES
(10, 750),
(11, 650);

INSERT INTO gpus (id, vram_size_gb, tdp_wattage) VALUES
(12, 12, 220),
(13, 16, 263);

INSERT INTO builds (id, user_id, title, description, is_public, approval_status, total_price, is_compatible) VALUES
(1, 1, 'Gaming 25tr', 'Build gaming 2K, GPU mạnh, PSU dư headroom.', TRUE, 'APPROVED', 25990000, TRUE),
(2, 2, 'Office 15tr', 'Cấu hình học tập - văn phòng tiết kiệm điện.', FALSE, 'PENDING', 14990000, TRUE),
(3, 3, 'Workstation AI', 'Build cho tác vụ nặng, cần kiểm tra PSU và RAM.', FALSE, 'DRAFT', 34990000, FALSE),
(4, 1, 'Streaming 30tr', 'Build thiên về stream và edit video.', TRUE, 'APPROVED', 30990000, TRUE);

INSERT INTO build_details (id, build_id, component_id, quantity) VALUES
(1, 1, 1, 1),
(2, 1, 4, 1),
(3, 1, 7, 1),
(4, 1, 12, 1),
(5, 1, 10, 1),
(6, 1, 14, 1),
(7, 2, 3, 1),
(8, 2, 6, 1),
(9, 2, 9, 1),
(10, 2, 13, 1),
(11, 2, 11, 1),
(12, 2, 14, 1),
(13, 3, 2, 1),
(14, 3, 5, 1),
(15, 3, 8, 1),
(16, 3, 12, 1),
(17, 3, 11, 1),
(18, 3, 14, 1),
(19, 4, 1, 1),
(20, 4, 4, 1),
(21, 4, 7, 1),
(22, 4, 13, 1),
(23, 4, 10, 1),
(24, 4, 14, 1);

INSERT INTO reviews (id, user_id, component_id, rating_star, comment_text) VALUES
(1, 2, 1, 5, 'CPU mát, hiệu năng tốt cho gaming.'),
(2, 3, 4, 5, 'Mainboard nhiều tính năng, BIOS ổn.'),
(3, 2, 7, 4, 'RAM chạy ổn, RGB vừa đủ.'),
(4, 3, 12, 5, 'GPU đẩy FPS tốt ở 2K.'),
(5, 2, 10, 5, 'PSU chạy êm, công suất dư dả.');

INSERT INTO crawled_prices (id, product_id, source_name, price_value) VALUES
(1, 1, 'gearvn.com', 7990000),
(2, 1, 'hacom.vn', 7890000),
(3, 1, 'anphatpc.com', 8050000),
(4, 4, 'gearvn.com', 6990000),
(5, 4, 'tinphong.vn', 6890000),
(6, 7, 'gearvn.com', 2390000),
(7, 7, 'hacom.vn', 2290000),
(8, 10, 'gearvn.com', 2790000),
(9, 10, 'anphatpc.com', 2690000),
(10, 12, 'gearvn.com', 16490000),
(11, 12, 'hacom.vn', 16290000),
(12, 13, 'gearvn.com', 14990000),
(13, 13, 'anphatpc.com', 14790000),
(14, 14, 'gearvn.com', 2690000),
(15, 14, 'hacom.vn', 2590000);