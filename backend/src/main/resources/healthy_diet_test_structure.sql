CREATE DATABASE IF NOT EXISTS healthy_diet_test CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE healthy_diet_test;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

CREATE TABLE IF NOT EXISTS users (
  id INT NOT NULL AUTO_INCREMENT,
  username VARCHAR(50) NULL DEFAULT NULL,
  password VARCHAR(255) NULL DEFAULT NULL,
  openid VARCHAR(100) NULL DEFAULT NULL COMMENT '微信用户唯一标识',
  nickname VARCHAR(100) NULL DEFAULT NULL COMMENT '微信昵称',
  avatar_url VARCHAR(500) NULL DEFAULT NULL COMMENT '微信头像',
  created_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  role VARCHAR(20) NULL DEFAULT 'user' COMMENT '角色：user普通用户/admin管理员',
  PRIMARY KEY (id),
  UNIQUE KEY username (username),
  UNIQUE KEY openid (openid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS health_info (
  id INT NOT NULL AUTO_INCREMENT,
  user_id INT NOT NULL,
  height DECIMAL(5, 2) NOT NULL COMMENT '身高(cm)',
  weight DECIMAL(5, 2) NOT NULL COMMENT '体重(kg)',
  gender VARCHAR(10) NOT NULL,
  goal VARCHAR(20) NOT NULL,
  dietary_preferences VARCHAR(500) NULL DEFAULT NULL COMMENT '忌口/过敏/疾病标签，逗号分隔',
  created_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY unique_user (user_id),
  CONSTRAINT health_info_ibfk_1 FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS recipes (
  id INT NOT NULL AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL,
  category VARCHAR(50) NOT NULL,
  image VARCHAR(500) NULL DEFAULT NULL,
  description TEXT NULL,
  calories INT NOT NULL,
  protein DECIMAL(6, 2) NOT NULL,
  carbs DECIMAL(6, 2) NOT NULL,
  fat DECIMAL(6, 2) NOT NULL,
  created_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  user_id INT NULL DEFAULT NULL COMMENT '上传用户ID，NULL表示官方食谱',
  status VARCHAR(20) NULL DEFAULT 'approved' COMMENT '状态：pending待审核/approved已通过/rejected已拒绝',
  reject_reason TEXT NULL COMMENT '审核拒绝原因',
  PRIMARY KEY (id),
  KEY idx_category (category),
  KEY idx_status (status),
  KEY idx_user_id (user_id),
  CONSTRAINT fk_recipe_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS recipe_ingredients (
  id INT NOT NULL AUTO_INCREMENT,
  recipe_id INT NOT NULL,
  ingredient VARCHAR(100) NOT NULL,
  sort_order INT NULL DEFAULT 0,
  PRIMARY KEY (id),
  KEY recipe_id (recipe_id),
  CONSTRAINT recipe_ingredients_ibfk_1 FOREIGN KEY (recipe_id) REFERENCES recipes (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS recipe_steps (
  id INT NOT NULL AUTO_INCREMENT,
  recipe_id INT NOT NULL,
  step_number INT NOT NULL,
  description TEXT NOT NULL,
  PRIMARY KEY (id),
  KEY recipe_id (recipe_id),
  CONSTRAINT recipe_steps_ibfk_1 FOREIGN KEY (recipe_id) REFERENCES recipes (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS recipe_suitable_goals (
  id INT NOT NULL AUTO_INCREMENT,
  recipe_id INT NOT NULL,
  goal VARCHAR(20) NOT NULL,
  PRIMARY KEY (id),
  KEY recipe_id (recipe_id),
  CONSTRAINT recipe_suitable_goals_ibfk_1 FOREIGN KEY (recipe_id) REFERENCES recipes (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS recipe_tags (
  id INT NOT NULL AUTO_INCREMENT,
  recipe_id INT NOT NULL,
  tag VARCHAR(50) NOT NULL,
  PRIMARY KEY (id),
  KEY recipe_id (recipe_id),
  CONSTRAINT recipe_tags_ibfk_1 FOREIGN KEY (recipe_id) REFERENCES recipes (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS diet_records (
  id INT NOT NULL AUTO_INCREMENT,
  user_id INT NOT NULL,
  recipe_id INT NULL DEFAULT NULL,
  recipe_name VARCHAR(100) NOT NULL,
  meal_type VARCHAR(20) NOT NULL,
  portion DECIMAL(3, 1) NOT NULL DEFAULT 1.0,
  calories INT NOT NULL,
  protein DECIMAL(6, 2) NOT NULL,
  carbs DECIMAL(6, 2) NOT NULL,
  fat DECIMAL(6, 2) NOT NULL,
  record_date DATE NOT NULL,
  created_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY user_id (user_id),
  KEY recipe_id (recipe_id),
  KEY idx_record_user_date (user_id, record_date),
  CONSTRAINT diet_records_ibfk_1 FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
  CONSTRAINT diet_records_ibfk_2 FOREIGN KEY (recipe_id) REFERENCES recipes (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS user_behavior (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id INT NOT NULL,
  recipe_id INT NOT NULL,
  behavior_type VARCHAR(20) NOT NULL COMMENT 'view/click/like/cook',
  weight INT NOT NULL COMMENT '行为权重：1/2/3/5',
  create_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_user_id (user_id),
  KEY idx_recipe_id (recipe_id),
  KEY idx_create_time (create_time),
  CONSTRAINT user_behavior_ibfk_1 FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
  CONSTRAINT user_behavior_ibfk_2 FOREIGN KEY (recipe_id) REFERENCES recipes (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS user_favorites (
  id INT NOT NULL AUTO_INCREMENT,
  user_id INT NOT NULL,
  recipe_id INT NOT NULL,
  created_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY unique_user_recipe (user_id, recipe_id),
  KEY idx_user_id (user_id),
  KEY idx_recipe_id (recipe_id),
  CONSTRAINT user_favorites_ibfk_1 FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
  CONSTRAINT user_favorites_ibfk_2 FOREIGN KEY (recipe_id) REFERENCES recipes (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

DROP PROCEDURE IF EXISTS add_column_if_missing;
DELIMITER //
CREATE PROCEDURE add_column_if_missing(IN p_table VARCHAR(64), IN p_column VARCHAR(64), IN p_definition TEXT)
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = p_table AND COLUMN_NAME = p_column
  ) THEN
    SET @sql = CONCAT('ALTER TABLE `', p_table, '` ADD COLUMN ', p_definition);
    PREPARE stmt FROM @sql;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
  END IF;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS add_index_if_missing;
DELIMITER //
CREATE PROCEDURE add_index_if_missing(IN p_table VARCHAR(64), IN p_index VARCHAR(64), IN p_definition TEXT)
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = p_table AND INDEX_NAME = p_index
  ) THEN
    SET @sql = CONCAT('ALTER TABLE `', p_table, '` ADD ', p_definition);
    PREPARE stmt FROM @sql;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
  END IF;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS add_fk_if_missing;
DELIMITER //
CREATE PROCEDURE add_fk_if_missing(IN p_table VARCHAR(64), IN p_constraint VARCHAR(64), IN p_definition TEXT)
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.TABLE_CONSTRAINTS
    WHERE CONSTRAINT_SCHEMA = DATABASE() AND TABLE_NAME = p_table AND CONSTRAINT_NAME = p_constraint
  ) THEN
    SET @sql = CONCAT('ALTER TABLE `', p_table, '` ADD CONSTRAINT `', p_constraint, '` ', p_definition);
    PREPARE stmt FROM @sql;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
  END IF;
END//
DELIMITER ;

CALL add_column_if_missing('users', 'openid', '`openid` VARCHAR(100) NULL DEFAULT NULL COMMENT ''微信用户唯一标识'' AFTER `password`');
CALL add_column_if_missing('users', 'nickname', '`nickname` VARCHAR(100) NULL DEFAULT NULL COMMENT ''微信昵称'' AFTER `openid`');
CALL add_column_if_missing('users', 'avatar_url', '`avatar_url` VARCHAR(500) NULL DEFAULT NULL COMMENT ''微信头像'' AFTER `nickname`');
CALL add_column_if_missing('users', 'role', '`role` VARCHAR(20) NULL DEFAULT ''user'' COMMENT ''角色：user普通用户/admin管理员'' AFTER `updated_at`');

CALL add_column_if_missing('health_info', 'dietary_preferences', '`dietary_preferences` VARCHAR(500) NULL DEFAULT NULL COMMENT ''忌口/过敏/疾病标签，逗号分隔'' AFTER `goal`');

CALL add_column_if_missing('recipes', 'user_id', '`user_id` INT NULL DEFAULT NULL COMMENT ''上传用户ID，NULL表示官方食谱'' AFTER `updated_at`');
CALL add_column_if_missing('recipes', 'status', '`status` VARCHAR(20) NULL DEFAULT ''approved'' COMMENT ''状态：pending待审核/approved已通过/rejected已拒绝'' AFTER `user_id`');
CALL add_column_if_missing('recipes', 'reject_reason', '`reject_reason` TEXT NULL COMMENT ''审核拒绝原因'' AFTER `status`');

CALL add_index_if_missing('users', 'openid', 'UNIQUE KEY `openid` (`openid`)');
CALL add_index_if_missing('recipes', 'idx_category', 'KEY `idx_category` (`category`)');
CALL add_index_if_missing('recipes', 'idx_status', 'KEY `idx_status` (`status`)');
CALL add_index_if_missing('recipes', 'idx_user_id', 'KEY `idx_user_id` (`user_id`)');
CALL add_index_if_missing('diet_records', 'idx_record_user_date', 'KEY `idx_record_user_date` (`user_id`, `record_date`)');

CALL add_fk_if_missing('recipes', 'fk_recipe_user', 'FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE SET NULL');

DROP PROCEDURE IF EXISTS add_column_if_missing;
DROP PROCEDURE IF EXISTS add_index_if_missing;
DROP PROCEDURE IF EXISTS add_fk_if_missing;

SET FOREIGN_KEY_CHECKS = 1;
