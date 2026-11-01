package com.nutriwell.backend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FoodSearchResponse(
        long totalHits,
        int currentPage,
        int totalPages,
        List<FoodSearchItem> foods ) {
    public FoodSearchResponse {
        foods = foods == null ? List.of() : List.copyOf(foods);
    }
}
