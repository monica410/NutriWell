package com.nutriwell.backend.model;

import java.time.LocalDate;

/** The amount is in food.unit(), matching the food's stored per-100 basis. */
public record FoodLog(
        Integer foodLogId,
        int userId,
        Food food,
        Double amount,
        LocalDate loggedDate
) {
}
