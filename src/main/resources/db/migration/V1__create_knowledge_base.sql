CREATE TABLE IF NOT EXISTS `knowledge_base` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `keyword` VARCHAR(255) NOT NULL,
  `category` VARCHAR(100) NOT NULL,
  `title` VARCHAR(255) NOT NULL,
  `content` LONGTEXT NOT NULL,
  `source_url` VARCHAR(2048) DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `search_count` INT NOT NULL DEFAULT 0,
  `last_access` DATETIME DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_knowledge_base_keyword` (`keyword`),
  KEY `idx_knowledge_base_category` (`category`),
  KEY `idx_knowledge_base_keyword_category` (`keyword`, `category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;