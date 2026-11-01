CREATE TABLE IF NOT EXISTS Users (
    user_id         INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    username        VARCHAR(255) NOT NULL UNIQUE,
    email           VARCHAR(255) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL
);

-- USDA stores nutrition by portion of 100 g / ml
CREATE TABLE IF NOT EXISTS Foods (
    food_id                     INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name                        VARCHAR(255) NOT NULL,
    unit                        VARCHAR(2) NOT NULL,
    calories_kcal_per_100       DECIMAL(10, 3),
    protein_g_per_100           DECIMAL(10, 3),
    carbohydrates_g_per_100     DECIMAL(10, 3),
    total_fat_g_per_100         DECIMAL(10, 3),
    fiber_g_per_100             DECIMAL(10, 3),
    total_sugar_g_per_100       DECIMAL(10, 3),
    sodium_mg_per_100           DECIMAL(10, 3),
    saturated_fat_g_per_100     DECIMAL(10, 3),
    source                      VARCHAR(50),
    external_food_id            VARCHAR(100),
    CONSTRAINT chk_foods_unit CHECK (unit IN ('g', 'ml')),
    CONSTRAINT unique_foods_source UNIQUE (source, external_food_id),
    CONSTRAINT chk_foods_source CHECK (
        (source IS NULL AND external_food_id IS NULL)
        OR (source IS NOT NULL AND external_food_id IS NOT NULL)
    ),
    CONSTRAINT chk_foods_nutrition CHECK (
        calories_kcal_per_100 >= 0 AND protein_g_per_100 >= 0
        AND carbohydrates_g_per_100 >= 0 AND total_fat_g_per_100 >= 0
        AND fiber_g_per_100 >= 0 AND total_sugar_g_per_100 >= 0
        AND sodium_mg_per_100 >= 0 AND saturated_fat_g_per_100 >= 0
    )
);

CREATE TABLE IF NOT EXISTS FoodLogs (
    food_log_id     INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id         INT NOT NULL,
    food_id         INT NOT NULL,
    amount          DECIMAL(10, 3) NOT NULL,
    logged_date     DATE NOT NULL,
    CONSTRAINT fk_food_logs_user FOREIGN KEY (user_id)
        REFERENCES Users (user_id) ON DELETE CASCADE,
    CONSTRAINT fk_food_logs_food FOREIGN KEY (food_id)
        REFERENCES Foods (food_id) ON DELETE RESTRICT,
    CONSTRAINT chk_food_logs_amount CHECK (amount > 0),
    INDEX idx_food_logs_user_date (user_id, logged_date)
);

CREATE TABLE IF NOT EXISTS WaterLogs (
    water_log_id    INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id         INT NOT NULL,
    amount_ml       DECIMAL(10, 3) NOT NULL,
    logged_date     DATE NOT NULL,
    CONSTRAINT fk_water_logs_user FOREIGN KEY (user_id)
        REFERENCES Users (user_id) ON DELETE CASCADE,
    CONSTRAINT chk_water_logs_amount CHECK (amount_ml > 0),
    INDEX idx_water_logs_user_date (user_id, logged_date)
);

CREATE TABLE IF NOT EXISTS PersonalGoals (
    user_id             INT NOT NULL PRIMARY KEY,
    calories_kcal       DECIMAL(10, 3),
    protein_g           DECIMAL(10, 3),
    carbohydrates_g     DECIMAL(10, 3),
    total_fat_g         DECIMAL(10, 3),
    fiber_g             DECIMAL(10, 3),
    total_sugar_g       DECIMAL(10, 3),
    sodium_mg           DECIMAL(10, 3),
    saturated_g         DECIMAL(10, 3),
    water_ml            DECIMAL(10, 3),
    CONSTRAINT fk_personal_goals_user FOREIGN KEY (user_id)
        REFERENCES Users (user_id) ON DELETE CASCADE,
    CONSTRAINT chk_personal_goals_targets CHECK (
        calories_kcal >= 0 AND protein_g >= 0
        AND carbohydrates_g >= 0 AND total_fat_g >= 0
        AND fiber_g >= 0 AND total_sugar_g >= 0
        AND sodium_mg >= 0 AND saturated_g >= 0
        AND water_ml >= 0
    )
);

