package com.nutriwell.backend.model;

import java.util.Objects;

public record Food(
        Integer foodId,
        String name,
        String unit,
        Nutrition nutrition, // nutrition values stored by portion of 100 g / ml
        String fdcId
) {
    public Food {
        Objects.requireNonNull(nutrition, "Nutrition must not be null.");
    }
}
