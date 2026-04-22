USE healthy_diet_test;

-- 检查users表是否有role字段，如果没有则添加
SET @dbname = DATABASE();
SET @tablename = 'users';
SET @columnname = 'role';
SET @prepared = CONCAT(
    'SELECT COUNT(*) INTO @exists ',
    'FROM information_schema.COLUMNS ',
    'WHERE TABLE_SCHEMA = ''', @dbname, ''' ',
    'AND TABLE_NAME = ''', @tablename, ''' ',
    'AND COLUMN_NAME = ''', @columnname, ''''
);
PREPARE stmt FROM @prepared;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @prepared = IF(@exists = 0,
    CONCAT(
        'ALTER TABLE ', @tablename, ' ',
        'ADD COLUMN ', @columnname, ' VARCHAR(20) DEFAULT ''user'' COMMENT ''角色：user普通用户/admin管理员'''
    ),
    'SELECT ''Column role already exists.'' AS message'
);
PREPARE stmt FROM @prepared;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 检查recipes表是否有user_id字段，如果没有则添加
SET @tablename = 'recipes';
SET @columnname = 'user_id';
SET @prepared = CONCAT(
    'SELECT COUNT(*) INTO @exists ',
    'FROM information_schema.COLUMNS ',
    'WHERE TABLE_SCHEMA = ''', @dbname, ''' ',
    'AND TABLE_NAME = ''', @tablename, ''' ',
    'AND COLUMN_NAME = ''', @columnname, ''''
);
PREPARE stmt FROM @prepared;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @prepared = IF(@exists = 0,
    CONCAT(
        'ALTER TABLE ', @tablename, ' ',
        'ADD COLUMN ', @columnname, ' INT COMMENT ''上传用户ID，NULL表示官方食谱'''
    ),
    'SELECT ''Column user_id already exists.'' AS message'
);
PREPARE stmt FROM @prepared;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 检查recipes表是否有status字段，如果没有则添加
SET @columnname = 'status';
SET @prepared = CONCAT(
    'SELECT COUNT(*) INTO @exists ',
    'FROM information_schema.COLUMNS ',
    'WHERE TABLE_SCHEMA = ''', @dbname, ''' ',
    'AND TABLE_NAME = ''', @tablename, ''' ',
    'AND COLUMN_NAME = ''', @columnname, ''''
);
PREPARE stmt FROM @prepared;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @prepared = IF(@exists = 0,
    CONCAT(
        'ALTER TABLE ', @tablename, ' ',
        'ADD COLUMN ', @columnname, ' VARCHAR(20) DEFAULT ''approved'' COMMENT ''状态：pending待审核/approved已通过/rejected已拒绝'''
    ),
    'SELECT ''Column status already exists.'' AS message'
);
PREPARE stmt FROM @prepared;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 检查recipes表是否有reject_reason字段，如果没有则添加
SET @columnname = 'reject_reason';
SET @prepared = CONCAT(
    'SELECT COUNT(*) INTO @exists ',
    'FROM information_schema.COLUMNS ',
    'WHERE TABLE_SCHEMA = ''', @dbname, ''' ',
    'AND TABLE_NAME = ''', @tablename, ''' ',
    'AND COLUMN_NAME = ''', @columnname, ''''
);
PREPARE stmt FROM @prepared;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @prepared = IF(@exists = 0,
    CONCAT(
        'ALTER TABLE ', @tablename, ' ',
        'ADD COLUMN ', @columnname, ' TEXT COMMENT ''审核拒绝原因'''
    ),
    'SELECT ''Column reject_reason already exists.'' AS message'
);
PREPARE stmt FROM @prepared;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 检查是否存在外键约束fk_recipe_user，如果不存在则添加
SET @constraintname = 'fk_recipe_user';
SET @prepared = CONCAT(
    'SELECT COUNT(*) INTO @exists ',
    'FROM information_schema.TABLE_CONSTRAINTS ',
    'WHERE CONSTRAINT_SCHEMA = ''', @dbname, ''' ',
    'AND TABLE_NAME = ''', @tablename, ''' ',
    'AND CONSTRAINT_NAME = ''', @constraintname, ''''
);
PREPARE stmt FROM @prepared;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @prepared = IF(@exists = 0,
    CONCAT(
        'ALTER TABLE ', @tablename, ' ',
        'ADD CONSTRAINT ', @constraintname, ' ',
        'FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL'
    ),
    'SELECT ''Constraint fk_recipe_user already exists.'' AS message'
);
PREPARE stmt FROM @prepared;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
