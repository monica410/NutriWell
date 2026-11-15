package com.nutriwell.backend.model;

public record PersonalGoals(
        int userId,
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
