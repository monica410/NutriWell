package com.nutriwell.backend.repository;

import com.nutriwell.backend.model.WaterLog;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.Optional;

@Repository
public class WaterLogRepository {
    private final JdbcTemplate jdbcTemplate;

    public WaterLogRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public WaterLog save(int userId, Double amount, LocalDate loggedDate) {
        var keys = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            var statement = connection.prepareStatement(
                    "INSERT INTO WaterLogs (user_id, amount_ml, logged_date) VALUES (?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            statement.setInt(1, userId);
            statement.setDouble(2, amount);
            statement.setDate(3, Date.valueOf(loggedDate));
            return statement;
        }, keys);
        Number key = keys.getKey();
        if (key == null) {
            throw new DataRetrievalFailureException("MySQL did not return a generated water log ID.");
        }
        return new WaterLog(key.intValue(), userId, amount, loggedDate);
    }

    public Optional<WaterLog> findByUserIdAndLoggedDate(int userId, LocalDate date) {
        return jdbcTemplate.query("""
                SELECT water_log_id, user_id, amount_ml, logged_date
                FROM WaterLogs
                WHERE user_id = ? AND logged_date = ?
                """, (rs, rowNum) -> new WaterLog(
                        rs.getInt("water_log_id"),
                        rs.getInt("user_id"),
                        rs.getDouble("amount_ml"),
                        rs.getDate("logged_date").toLocalDate()),
                userId, Date.valueOf(date)).stream().findFirst();
    }

    public boolean updateAmount(int waterLogId, int userId, Double amount) {
        return jdbcTemplate.update("UPDATE WaterLogs SET amount_ml = ? WHERE water_log_id = ? AND user_id = ?",
                amount, waterLogId, userId) > 0;
    }

    public boolean deleteByIdAndUserId(int waterLogId, int userId) {
        return jdbcTemplate.update("DELETE FROM WaterLogs WHERE water_log_id = ? AND user_id = ?",
                waterLogId, userId) > 0;
    }
}
