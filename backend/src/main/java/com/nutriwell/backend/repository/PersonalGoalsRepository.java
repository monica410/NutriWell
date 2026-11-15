package com.nutriwell.backend.repository;

import com.nutriwell.backend.dto.PersonalGoalsRequest;
import com.nutriwell.backend.model.PersonalGoals;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class PersonalGoalsRepository {
    private final JdbcTemplate jdbcTemplate;

    public PersonalGoalsRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public PersonalGoals save(PersonalGoalsRequest request) {
        jdbcTemplate.update("""
                INSERT INTO PersonalGoals (calories_kcal, protein_g, carbohydrates_g, total_fat_g,
                    fiber_g, total_sugar_g, sodium_mg, saturated_g, water_ml, user_id)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, parameters(request));
        return new PersonalGoals(request.userId(), request.caloriesKcal(), request.proteinG(),
                request.carbohydratesG(), request.totalFatG(), request.fiberG(), request.totalSugarG(),
                request.sodiumMg(), request.saturatedFatG(), request.waterMl());
    }

    public Optional<PersonalGoals> findByUserId(int userId) {
        return jdbcTemplate.query("""
                SELECT user_id, calories_kcal, protein_g, carbohydrates_g, total_fat_g,
                    fiber_g, total_sugar_g, sodium_mg, saturated_g, water_ml
                FROM PersonalGoals WHERE user_id = ?
                """, (rs, rowNum) -> new PersonalGoals(
                rs.getInt("user_id"),
                rs.getObject("calories_kcal", Double.class),
                rs.getObject("protein_g", Double.class),
                rs.getObject("carbohydrates_g", Double.class),
                rs.getObject("total_fat_g", Double.class),
                rs.getObject("fiber_g", Double.class),
                rs.getObject("total_sugar_g", Double.class),
                rs.getObject("sodium_mg", Double.class),
                rs.getObject("saturated_g", Double.class),
                rs.getObject("water_ml", Double.class)), userId).stream().findFirst();
    }

    public boolean update(PersonalGoalsRequest request) {
        return jdbcTemplate.update("""
                UPDATE PersonalGoals SET
                    calories_kcal = COALESCE(?, calories_kcal),
                    protein_g = COALESCE(?, protein_g),
                    carbohydrates_g = COALESCE(?, carbohydrates_g),
                    total_fat_g = COALESCE(?, total_fat_g),
                    fiber_g = COALESCE(?, fiber_g),
                    total_sugar_g = COALESCE(?, total_sugar_g),
                    sodium_mg = COALESCE(?, sodium_mg),
                    saturated_g = COALESCE(?, saturated_g),
                    water_ml = COALESCE(?, water_ml)
                WHERE user_id = ?
                """, parameters(request)) > 0;
    }

    public boolean clearTarget(int userId, String targetName) {
        String column = switch (targetName) {
            case "caloriesKcal" -> "calories_kcal";
            case "proteinG" -> "protein_g";
            case "carbohydratesG" -> "carbohydrates_g";
            case "totalFatG" -> "total_fat_g";
            case "fiberG" -> "fiber_g";
            case "totalSugarG" -> "total_sugar_g";
            case "sodiumMg" -> "sodium_mg";
            case "saturatedFatG" -> "saturated_g";
            case "waterMl" -> "water_ml";
            case null, default -> throw new IllegalArgumentException(
                    "Unknown goal target. Use a target name such as proteinG or waterMl.");
        };
        return jdbcTemplate.update("UPDATE PersonalGoals SET " + column + " = NULL WHERE user_id = ?",
                userId) > 0;
    }

    public boolean deleteByUserId(int userId) {
        return jdbcTemplate.update("DELETE FROM PersonalGoals WHERE user_id = ?", userId) > 0;
    }

    private Object[] parameters(PersonalGoalsRequest request) {
        return new Object[] {request.caloriesKcal(), request.proteinG(), request.carbohydratesG(),
                request.totalFatG(), request.fiberG(), request.totalSugarG(), request.sodiumMg(),
                request.saturatedFatG(), request.waterMl(), request.userId()};
    }
}
