package com.nutriwell.backend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** One food in the USDA search results (before fetching its details). */
@JsonIgnoreProperties(ignoreUnknown = true)
public record FoodSearchItem(
        long fdcId,
        String description,
        String dataType,
        String brandName
) { }
