package com.nutriwell.backend.repository;

import com.nutriwell.backend.model.Food;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class FrequentFoodRepository {
    private final JdbcTemplate jdbcTemplate;

    public FrequentFoodRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void save(int userId, int foodId) {
        jdbcTemplate.update("""
                INSERT INTO FrequentFoods (user_id, food_id) VALUES (?, ?)
                ON DUPLICATE KEY UPDATE food_id = FrequentFoods.food_id
                """, userId, foodId);
    }

    public boolean delete(int userId, int foodId) {
        return jdbcTemplate.update("DELETE FROM FrequentFoods WHERE user_id = ? AND food_id = ?",
                userId, foodId) > 0;
    }

    public List<Food> findByUserId(int userId) {
        return jdbcTemplate.query("""
                SELECT f.* FROM FrequentFoods ff JOIN Foods f ON f.food_id = ff.food_id
                WHERE ff.user_id = ? ORDER BY f.name, f.food_id
                """, FoodRepository.FOOD_ROW_MAPPER, userId);
    }
}
