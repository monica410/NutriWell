package com.nutriwell.backend.dto;

import com.nutriwell.backend.model.Nutrition;

/** A locally saved USDA food, with nutrition per 100 g or 100 ml. */
public record FoodResponse(
        int foodId,
        String fdcId,
        String name,
        String unit,
        Nutrition nutrition
) { }
