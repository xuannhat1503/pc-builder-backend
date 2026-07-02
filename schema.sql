CREATE DATABASE IF NOT EXISTS pc_builder
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE pc_builder;

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `build_details`;
DROP TABLE IF EXISTS `reviews`;
DROP TABLE IF EXISTS `crawled_prices`;
DROP TABLE IF EXISTS `builds`;
DROP TABLE IF EXISTS `gpus`;
DROP TABLE IF EXISTS `psus`;
DROP TABLE IF EXISTS `rams`;
DROP TABLE IF EXISTS `mainboards`;
DROP TABLE IF EXISTS `cpus`;
DROP TABLE IF EXISTS `base_components`;
DROP TABLE IF EXISTS `users`;

SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE `users` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `email` VARCHAR(255) NOT NULL,
  `password_hash` VARCHAR(255) NOT NULL,
  `role` VARCHAR(50) NOT NULL DEFAULT 'USER',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_users_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `base_components` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(255) NOT NULL,
  `brand` VARCHAR(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `cpus` (
  `id` BIGINT NOT NULL,
  `socket_type` VARCHAR(100) NOT NULL,
  `tdp_wattage` INT NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_cpus_base_components`
    FOREIGN KEY (`id`) REFERENCES `base_components` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `mainboards` (
  `id` BIGINT NOT NULL,
  `socket_type` VARCHAR(100) NOT NULL,
  `ram_generation` VARCHAR(50) NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_mainboards_base_components`
    FOREIGN KEY (`id`) REFERENCES `base_components` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `rams` (
  `id` BIGINT NOT NULL,
  `ram_generation` VARCHAR(50) NOT NULL,
  `capacity_gb` INT NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_rams_base_components`
    FOREIGN KEY (`id`) REFERENCES `base_components` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `psus` (
  `id` BIGINT NOT NULL,
  `power_output_watt` INT NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_psus_base_components`
    FOREIGN KEY (`id`) REFERENCES `base_components` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `gpus` (
  `id` BIGINT NOT NULL,
  `vram_size_gb` INT NOT NULL,
  `tdp_wattage` INT NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_gpus_base_components`
    FOREIGN KEY (`id`) REFERENCES `base_components` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `builds` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `title` VARCHAR(255) DEFAULT NULL,
  `description` TEXT,
  `is_public` BOOLEAN DEFAULT FALSE,
  `approval_status` VARCHAR(20) DEFAULT 'DRAFT',
  `total_price` DOUBLE NOT NULL DEFAULT 0,
  `is_compatible` BOOLEAN NOT NULL DEFAULT TRUE,
  PRIMARY KEY (`id`),
  KEY `idx_builds_user_id` (`user_id`),
  CONSTRAINT `fk_builds_users`
    FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `build_details` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `build_id` BIGINT NOT NULL,
  `component_id` BIGINT NOT NULL,
  `quantity` INT NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`),
  KEY `idx_build_details_build_id` (`build_id`),
  KEY `idx_build_details_component_id` (`component_id`),
  CONSTRAINT `fk_build_details_builds`
    FOREIGN KEY (`build_id`) REFERENCES `builds` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_build_details_components`
    FOREIGN KEY (`component_id`) REFERENCES `base_components` (`id`)
    ON DELETE RESTRICT
    ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `reviews` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `component_id` BIGINT NOT NULL,
  `rating_star` INT NOT NULL,
  `comment_text` TEXT,
  PRIMARY KEY (`id`),
  KEY `idx_reviews_user_id` (`user_id`),
  KEY `idx_reviews_component_id` (`component_id`),
  CONSTRAINT `fk_reviews_users`
    FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_reviews_components`
    FOREIGN KEY (`component_id`) REFERENCES `base_components` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `chk_reviews_rating_star`
    CHECK (`rating_star` BETWEEN 1 AND 5)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `crawled_prices` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `product_id` BIGINT NOT NULL,
  `source_name` VARCHAR(255) NOT NULL,
  `price_value` DOUBLE NOT NULL,
  `crawled_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_crawled_prices_product_id` (`product_id`),
  CONSTRAINT `fk_crawled_prices_components`
    FOREIGN KEY (`product_id`) REFERENCES `base_components` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

UPDATE `crawled_prices`
SET `crawled_at` = CURRENT_TIMESTAMP
WHERE `crawled_at` IS NULL
   OR `crawled_at` = '0000-00-00 00:00:00'
   OR `crawled_at` = '0000-00-00 00:00:00.000000';
