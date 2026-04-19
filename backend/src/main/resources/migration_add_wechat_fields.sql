-- 为 users 表添加微信登录相关字段
-- 执行此 SQL 来更新现有数据库

USE healthy_diet_test;

ALTER TABLE users
MODIFY COLUMN username VARCHAR(50),
MODIFY COLUMN password VARCHAR(255),
ADD COLUMN openid VARCHAR(100) UNIQUE COMMENT '微信用户唯一标识' AFTER password,
ADD COLUMN nickname VARCHAR(100) COMMENT '微信昵称' AFTER openid,
ADD COLUMN avatar_url VARCHAR(500) COMMENT '微信头像' AFTER nickname;
