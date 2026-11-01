package com.nutriwell.backend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/** Selected USDA food details. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record FoodDetailsResponse(
        long fdcId,
        String description,
        String dataType,
        String brandName,
        String ingredients,
        String servingSizeUnit,
        List<FoodNutrient> foodNutrients) {
    public FoodDetailsResponse {
        foodNutrients = foodNutrients == null ? List.of() : List.copyOf(foodNutrients);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record FoodNutrient(Long id, Nutrient nutrient, Double amount) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Nutrient(Long id, String name, String number, String unitName) {
    }

}
