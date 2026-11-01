package com.nutriwell.backend.repository;

import com.nutriwell.backend.model.Food;
import com.nutriwell.backend.model.Nutrition;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public class FoodRepository {

    private static final String SELECT_FOODS = """
            SELECT food_id, name, unit, calories_kcal_per_100, protein_g_per_100,
                   carbohydrates_g_per_100, total_fat_g_per_100, fiber_g_per_100,
                   total_sugar_g_per_100, sodium_mg_per_100, saturated_fat_g_per_100,
                   fdc_id
            FROM Foods
            """;

    static final RowMapper<Food> FOOD_ROW_MAPPER = (rs, rowNum) -> new Food(
            rs.getInt("food_id"),
            rs.getString("name"),
            rs.getString("unit"),
            new Nutrition(
                    rs.getDouble("calories_kcal_per_100"),
                    rs.getDouble("protein_g_per_100"),
                    rs.getDouble("carbohydrates_g_per_100"),
                    rs.getDouble("total_fat_g_per_100"),
                    rs.getDouble("fiber_g_per_100"),
                    rs.getDouble("total_sugar_g_per_100"),
                    rs.getDouble("sodium_mg_per_100"),
                    rs.getDouble("saturated_fat_g_per_100")),
            rs.getString("fdc_id")
    );

    private final JdbcTemplate jdbcTemplate;

    public FoodRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /** Inserts a new food. Pass null for foodId so MySQL generates the local ID. */
    @Transactional
    public Food save(Food food) {
        Objects.requireNonNull(food, "Food must not be null.");
        if (food.foodId() != null) {
            throw new IllegalArgumentException("New food must have a null foodId.");
        }

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement("""
                    INSERT INTO Foods (
                        name, unit, calories_kcal_per_100, protein_g_per_100,
                        carbohydrates_g_per_100, total_fat_g_per_100, fiber_g_per_100,
                        total_sugar_g_per_100, sodium_mg_per_100, saturated_fat_g_per_100,
                        fdc_id
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, food.name());
            statement.setString(2, food.unit());
            Nutrition nutrition = food.nutrition();
            statement.setDouble(3, nutrition.caloriesKcal());
            statement.setDouble(4, nutrition.proteinG());
            statement.setDouble(5, nutrition.carbohydratesG());
            statement.setDouble(6, nutrition.totalFatG());
            statement.setDouble(7, nutrition.fiberG());
            statement.setDouble(8, nutrition.totalSugarG());
            statement.setDouble(9, nutrition.sodiumMg());
            statement.setDouble(10, nutrition.saturatedFatG());
            statement.setString(11, food.fdcId());
            return statement;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key == null) {
            throw new DataRetrievalFailureException("MySQL did not return a generated food ID.");
        }

        return findById(key.intValue()).orElseThrow(() ->
                new DataRetrievalFailureException("The inserted food could not be retrieved."));
    }

    public Optional<Food> findById(int foodId) {
        return jdbcTemplate.query(
                SELECT_FOODS + " WHERE food_id = ?",
                FOOD_ROW_MAPPER,
                foodId
        ).stream().findFirst();
    }

    public Optional<Food> findByFdcId(String fdcId) {
        return jdbcTemplate.query(
                SELECT_FOODS + " WHERE fdc_id = ?",
                FOOD_ROW_MAPPER,
                fdcId
        ).stream().findFirst();
    }

    public List<Food> findAll() {
        return jdbcTemplate.query(SELECT_FOODS + " ORDER BY name, food_id", FOOD_ROW_MAPPER);
    }
}
