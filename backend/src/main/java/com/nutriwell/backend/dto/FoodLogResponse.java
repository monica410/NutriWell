package com.nutriwell.backend.dto;

import com.nutriwell.backend.model.Nutrition;

import java.time.LocalDate;

public record FoodLogResponse(
        int foodLogId,
        int userId,
        int foodId,
        String fdcId,
        String foodName,
        Double amount,
        String unit,
        LocalDate loggedDate,
        Nutrition nutrition
) { }
