package com.nutriwell.backend.model;

import java.util.Map;

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
        Double waterMl,
        Map<String, Boolean> maxGoals
) { }
