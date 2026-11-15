package com.nutriwell.backend.dto;

public record PersonalGoalsRequest(
        Integer userId,
        Double caloriesKcal,
        Double proteinG,
        Double carbohydratesG,
        Double totalFatG,
        Double fiberG,
        Double totalSugarG,
        Double sodiumMg,
        Double saturatedFatG,
        Double waterMl
) { }
