package com.nutriwell.backend.dto;

import java.time.LocalDate;

public record FoodLogRequest(
        Integer userId,
        Long fdcId,
        Double amount,
        String unit,
        LocalDate loggedDate
) { }
