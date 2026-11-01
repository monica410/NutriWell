package com.nutriwell.backend.model;

/** A nutrient value is null when it is unknown (not 0). */
public record Nutrition(
        Double caloriesKcal,
        Double proteinG,
        Double carbohydratesG,
        Double totalFatG,
        Double fiberG,
        Double totalSugarG,
        Double sodiumMg,
        Double saturatedFatG
) {

    /** Scale values per 100 */
    public Nutrition scaleAmount(Double amount) {
        double factor = amount / 100;
        return new Nutrition(scale(caloriesKcal, factor), scale(proteinG, factor),
                scale(carbohydratesG, factor), scale(totalFatG, factor),
                scale(fiberG, factor), scale(totalSugarG, factor),
                scale(sodiumMg, factor), scale(saturatedFatG, factor));
    }

    public Nutrition plus(Nutrition other) {
        return new Nutrition(add(caloriesKcal, other.caloriesKcal), add(proteinG, other.proteinG),
                add(carbohydratesG, other.carbohydratesG), add(totalFatG, other.totalFatG),
                add(fiberG, other.fiberG), add(totalSugarG, other.totalSugarG),
                add(sodiumMg, other.sodiumMg), add(saturatedFatG, other.saturatedFatG));
    }

    private static Double scale(Double value, double factor) {
        return value == null ? null : value * factor;
    }

    private static Double add(Double first, Double second) {
        return first == null || second == null ? null : first + second;
    }
}
