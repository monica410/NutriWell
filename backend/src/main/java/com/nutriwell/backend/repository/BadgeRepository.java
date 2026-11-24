package com.nutriwell.backend.repository;

import com.nutriwell.backend.dto.BadgeResponse;
import com.nutriwell.backend.model.FoodLog;
import com.nutriwell.backend.model.WaterLog;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

@Repository
public class BadgeRepository {
    private final JdbcTemplate jdbcTemplate;

    public BadgeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<FoodLog> findFoodLogsByDate(int userId, LocalDate date) {
        return jdbcTemplate.query("""
                SELECT f.*, fl.food_log_id, fl.user_id, fl.amount, fl.logged_date
                FROM FoodLogs fl JOIN Foods f ON f.food_id = fl.food_id
                WHERE fl.user_id = ? AND fl.logged_date = ?
                ORDER BY fl.logged_date, fl.food_log_id
                """, (rs, rowNum) -> new FoodLog(
                rs.getInt("food_log_id"), rs.getInt("user_id"),
                FoodRepository.FOOD_ROW_MAPPER.mapRow(rs, rowNum),
                rs.getDouble("amount"), rs.getDate("logged_date").toLocalDate()),
                userId, Date.valueOf(date));
    }

    public List<WaterLog> findWaterLogsByDate(int userId, LocalDate date) {
        return jdbcTemplate.query("""
                SELECT water_log_id, user_id, amount_ml, logged_date FROM WaterLogs
                WHERE user_id = ? AND logged_date = ?
                """, (rs, rowNum) -> new WaterLog(rs.getInt("water_log_id"), rs.getInt("user_id"),
                rs.getDouble("amount_ml"), rs.getDate("logged_date").toLocalDate()),
                userId, Date.valueOf(date));
    }

    public void awardStreakBadges(int userId, String type, int streak, LocalDate through) {
        if (streak <= 0) return;
        jdbcTemplate.update("""
                INSERT INTO UserBadges (user_id, badge_id, earned_date)
                SELECT ?, badge_id, DATE_SUB(?, INTERVAL (? - number) DAY)
                FROM Badges
                WHERE type = ? AND number <= ?
                ON DUPLICATE KEY UPDATE user_id = UserBadges.user_id
                """, userId, Date.valueOf(through), streak, type, streak);
    }

    public List<BadgeResponse> findBadgesByUserId(int userId) {
        return jdbcTemplate.query("""
                SELECT b.badge_id, b.name, b.type, b.number, ub.earned_date
                FROM UserBadges ub JOIN Badges b ON b.badge_id = ub.badge_id
                WHERE ub.user_id = ?
                ORDER BY ub.earned_date DESC, b.type ASC
                """, (rs, rowNum) -> new BadgeResponse(rs.getInt("badge_id"), rs.getString("name"),
                rs.getString("type"), rs.getInt("number"), rs.getDate("earned_date").toLocalDate()),
                userId);
    }
}
