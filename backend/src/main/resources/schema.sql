-- 创建数据库
CREATE DATABASE IF NOT EXISTS healthy_diet_test CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE healthy_diet_test;

-- 用户表
CREATE TABLE IF NOT EXISTS users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50),
    password VARCHAR(255),
    openid VARCHAR(100) UNIQUE COMMENT '微信用户唯一标识',
    nickname VARCHAR(100) COMMENT '微信昵称',
    avatar_url VARCHAR(500) COMMENT '微信头像',
    role VARCHAR(20) DEFAULT 'user' COMMENT '角色：user普通用户/admin管理员',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 健康信息表
CREATE TABLE IF NOT EXISTS health_info (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    height DECIMAL(5,2) NOT NULL COMMENT '身高(cm)',
    weight DECIMAL(5,2) NOT NULL COMMENT '体重(kg)',
    gender VARCHAR(10) NOT NULL,
    goal VARCHAR(20) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_user (user_id)
);

-- 食谱表
CREATE TABLE IF NOT EXISTS recipes (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    category VARCHAR(50) NOT NULL,
    image VARCHAR(500),
    description TEXT,
    calories INT NOT NULL,
    protein DECIMAL(6,2) NOT NULL,
    carbs DECIMAL(6,2) NOT NULL,
    fat DECIMAL(6,2) NOT NULL,
    user_id INT COMMENT '上传用户ID，NULL表示官方食谱',
    status VARCHAR(20) DEFAULT 'approved' COMMENT '状态：pending待审核/approved已通过/rejected已拒绝',
    reject_reason TEXT COMMENT '审核拒绝原因',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_category (category),
    INDEX idx_status (status),
    INDEX idx_user_id (user_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

-- 食谱食材表
CREATE TABLE IF NOT EXISTS recipe_ingredients (
    id INT PRIMARY KEY AUTO_INCREMENT,
    recipe_id INT NOT NULL,
    ingredient VARCHAR(100) NOT NULL,
    sort_order INT DEFAULT 0,
    FOREIGN KEY (recipe_id) REFERENCES recipes(id) ON DELETE CASCADE
);

-- 食谱标签表
CREATE TABLE IF NOT EXISTS recipe_tags (
    id INT PRIMARY KEY AUTO_INCREMENT,
    recipe_id INT NOT NULL,
    tag VARCHAR(50) NOT NULL,
    FOREIGN KEY (recipe_id) REFERENCES recipes(id) ON DELETE CASCADE
);

-- 食谱适用目标表
CREATE TABLE IF NOT EXISTS recipe_suitable_goals (
    id INT PRIMARY KEY AUTO_INCREMENT,
    recipe_id INT NOT NULL,
    goal VARCHAR(20) NOT NULL,
    FOREIGN KEY (recipe_id) REFERENCES recipes(id) ON DELETE CASCADE
);

-- 食谱制作步骤表
CREATE TABLE IF NOT EXISTS recipe_steps (
    id INT PRIMARY KEY AUTO_INCREMENT,
    recipe_id INT NOT NULL,
    step_number INT NOT NULL,
    description TEXT NOT NULL,
    FOREIGN KEY (recipe_id) REFERENCES recipes(id) ON DELETE CASCADE
);

-- 饮食记录表
CREATE TABLE IF NOT EXISTS diet_records (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    recipe_id INT,
    recipe_name VARCHAR(100) NOT NULL,
    meal_type VARCHAR(20) NOT NULL,
    portion DECIMAL(3,1) NOT NULL DEFAULT 1.0,
    calories INT NOT NULL,
    protein DECIMAL(6,2) NOT NULL,
    carbs DECIMAL(6,2) NOT NULL,
    fat DECIMAL(6,2) NOT NULL,
    record_date DATE NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (recipe_id) REFERENCES recipes(id) ON DELETE SET NULL
);

-- 用户行为表
CREATE TABLE IF NOT EXISTS user_behavior (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    recipe_id INT NOT NULL,
    behavior_type VARCHAR(20) NOT NULL COMMENT 'view/click/like/cook',
    weight INT NOT NULL COMMENT '行为权重：1/2/3/5',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_recipe_id (recipe_id),
    INDEX idx_create_time (create_time),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (recipe_id) REFERENCES recipes(id) ON DELETE CASCADE
);

-- 用户收藏表
CREATE TABLE IF NOT EXISTS user_favorites (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    recipe_id INT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY unique_user_recipe (user_id, recipe_id),
    INDEX idx_user_id (user_id),
    INDEX idx_recipe_id (recipe_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (recipe_id) REFERENCES recipes(id) ON DELETE CASCADE
);
