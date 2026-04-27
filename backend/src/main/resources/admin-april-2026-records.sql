USE healthy_diet_test;

SET @admin_user_id = (
    SELECT id
    FROM users
    WHERE username = 'admin'
    LIMIT 1
);

DELETE FROM diet_records
WHERE user_id = @admin_user_id
  AND record_date BETWEEN '2026-04-01' AND '2026-04-25';

INSERT INTO diet_records (
    user_id,
    recipe_id,
    recipe_name,
    meal_type,
    portion,
    calories,
    protein,
    carbs,
    fat,
    record_date
)
WITH RECURSIVE date_series AS (
    SELECT DATE('2026-04-01') AS record_date
    UNION ALL
    SELECT DATE_ADD(record_date, INTERVAL 1 DAY)
    FROM date_series
    WHERE record_date < '2026-04-25'
),
days AS (
    SELECT
        record_date,
        DATEDIFF(record_date, '2026-04-01') AS day_index
    FROM date_series
),
meals AS (
    SELECT '早餐' AS meal_type
    UNION ALL
    SELECT '午餐'
    UNION ALL
    SELECT '晚餐'
)
SELECT
    @admin_user_id AS user_id,
    NULL AS recipe_id,
    CASE meals.meal_type
        WHEN '早餐' THEN ELT(MOD(days.day_index, 3) + 1, '燕麦牛奶粥', '全麦三明治', '南瓜粥')
        WHEN '午餐' THEN ELT(MOD(days.day_index, 5) + 1, '鸡胸肉沙拉', '牛肉西兰花', '烤鸡胸肉配蔬菜', '番茄炒蛋', '虾仁豆腐汤')
        WHEN '晚餐' THEN ELT(MOD(days.day_index, 5) + 1, '清蒸鲈鱼', '糙米饭配蔬菜', '虾仁滑蛋', '清蒸虾', '菠菜炒鸡蛋')
    END AS recipe_name,
    meals.meal_type,
    1.0 AS portion,
    CASE meals.meal_type
        WHEN '早餐' THEN ELT(MOD(days.day_index, 3) + 1, 220, 350, 150)
        WHEN '午餐' THEN ELT(MOD(days.day_index, 5) + 1, 280, 350, 320, 220, 150)
        WHEN '晚餐' THEN ELT(MOD(days.day_index, 5) + 1, 180, 260, 280, 120, 180)
    END AS calories,
    CASE meals.meal_type
        WHEN '早餐' THEN ELT(MOD(days.day_index, 3) + 1, 10.0, 20.0, 4.0)
        WHEN '午餐' THEN ELT(MOD(days.day_index, 5) + 1, 32.0, 35.0, 38.0, 15.0, 20.0)
        WHEN '晚餐' THEN ELT(MOD(days.day_index, 5) + 1, 28.0, 8.0, 25.0, 25.0, 12.0)
    END AS protein,
    CASE meals.meal_type
        WHEN '早餐' THEN ELT(MOD(days.day_index, 3) + 1, 38.0, 45.0, 30.0)
        WHEN '午餐' THEN ELT(MOD(days.day_index, 5) + 1, 15.0, 12.0, 10.0, 18.0, 8.0)
        WHEN '晚餐' THEN ELT(MOD(days.day_index, 5) + 1, 5.0, 50.0, 12.0, 0.0, 8.0)
    END AS carbs,
    CASE meals.meal_type
        WHEN '早餐' THEN ELT(MOD(days.day_index, 3) + 1, 6.0, 12.0, 2.0)
        WHEN '午餐' THEN ELT(MOD(days.day_index, 5) + 1, 10.0, 18.0, 12.0, 12.0, 5.0)
        WHEN '晚餐' THEN ELT(MOD(days.day_index, 5) + 1, 6.0, 4.0, 15.0, 1.0, 10.0)
    END AS fat,
    days.record_date
FROM days
CROSS JOIN meals
WHERE @admin_user_id IS NOT NULL
ORDER BY days.record_date, FIELD(meals.meal_type, '早餐', '午餐', '晚餐');
