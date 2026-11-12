package com.nutriwell.backend.repository;

import com.nutriwell.backend.model.Food;
import com.nutriwell.backend.model.FoodLog;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;

@Repository
public class FoodLogRepository {

    private final JdbcTemplate jdbcTemplate;

    public FoodLogRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public FoodLog save(int userId, Food food, Double amount, LocalDate loggedDate) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            var statement = connection.prepareStatement("""
                    INSERT INTO FoodLogs (user_id, food_id, amount, logged_date)
                    VALUES (?, ?, ?, ?)
                    """, Statement.RETURN_GENERATED_KEYS);
            statement.setInt(1, userId);
            statement.setInt(2, food.foodId());
            statement.setDouble(3, amount);
            statement.setDate(4, Date.valueOf(loggedDate));
            return statement;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key == null) {
            throw new DataRetrievalFailureException("MySQL did not return a generated food log ID.");
        }
        return new FoodLog(key.intValue(), userId, food, amount, loggedDate);
    }

    public boolean updateAmount(int foodLogId, int userId, double amount) {
        return jdbcTemplate.update("UPDATE FoodLogs SET amount = ? WHERE food_log_id = ? AND user_id = ?",
                amount, foodLogId, userId) > 0;
    }

    public boolean deleteByIdAndUserId(int foodLogId, int userId) {
        return jdbcTemplate.update("DELETE FROM FoodLogs WHERE food_log_id = ? AND user_id = ?",
                foodLogId, userId) > 0;
    }

    public List<FoodLog> findByUserIdAndLoggedDate(int userId, LocalDate loggedDate) {
        return jdbcTemplate.query("""
                SELECT fl.food_log_id, fl.user_id, fl.amount, fl.logged_date,
                       f.food_id, f.name, f.unit, f.calories_kcal_per_100, f.protein_g_per_100,
                       f.carbohydrates_g_per_100, f.total_fat_g_per_100, f.fiber_g_per_100,
                       f.total_sugar_g_per_100, f.sodium_mg_per_100, f.saturated_fat_g_per_100,
                       f.fdc_id
                FROM FoodLogs fl
                JOIN Foods f ON f.food_id = fl.food_id
                WHERE fl.user_id = ? AND fl.logged_date = ?
                ORDER BY fl.food_log_id
                """, (rs, rowNum) -> new FoodLog(
                rs.getInt("food_log_id"), rs.getInt("user_id"),
                FoodRepository.FOOD_ROW_MAPPER.mapRow(rs, rowNum),
                rs.getDouble("amount"), rs.getDate("logged_date").toLocalDate()
        ), userId, Date.valueOf(loggedDate));
    }
}
