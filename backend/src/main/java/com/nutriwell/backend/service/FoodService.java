package com.nutriwell.backend.service;

import com.nutriwell.backend.dto.FoodDetailsResponse;
import com.nutriwell.backend.dto.FoodResponse;
import com.nutriwell.backend.model.Food;
import com.nutriwell.backend.model.Nutrition;
import com.nutriwell.backend.repository.FoodRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class FoodService {

    private final UsdaFoodService usdaFoodService;
    private final FoodRepository foodRepository;

    public FoodService(UsdaFoodService usdaFoodService, FoodRepository foodRepository) {
        this.usdaFoodService = usdaFoodService;
        this.foodRepository = foodRepository;
    }

    public FoodResponse importFood(long fdcId) {
        Food food = getOrImportFood(fdcId);
        return new FoodResponse(food.foodId(), food.fdcId(), food.name(), food.unit(), food.nutrition());
    }

    public Food getOrImportFood(long fdcId) {
        if (fdcId <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "FDC ID must be positive.");
        }
        String externalId = Long.toString(fdcId);
        return foodRepository.findByFdcId(externalId).orElseGet(() -> {
            FoodDetailsResponse detailsResponse = usdaFoodService.getFood(fdcId);
            if (detailsResponse.fdcId() != fdcId) {
                throw invalidUsdaFood("USDA returned a different food ID.");
            }
            Food food = toFood(detailsResponse);
            try {
                return foodRepository.save(food);
            } catch (DuplicateKeyException exception) {
                return foodRepository.findByFdcId(externalId).orElseThrow(() -> exception);
            }
        });
    }

    static Food toFood(FoodDetailsResponse detailsResponse) {
        if (detailsResponse.fdcId() <= 0 || detailsResponse.description() == null) {
            throw invalidUsdaFood("USDA returned an invalid food description or ID.");
        }
        return new Food(null, detailsResponse.description().trim(), foodUnit(detailsResponse),
                new Nutrition(
                        // last paremeter is usda nutrient values
                        nutrient(detailsResponse, "kcal", 2048, 2047, 1008, 1062),
                        nutrient(detailsResponse, "g", 1003),
                        nutrient(detailsResponse, "g", 1005),
                        nutrient(detailsResponse, "g", 1004),
                        nutrient(detailsResponse, "g", 1079),
                        nutrient(detailsResponse, "g", 2000, 1063),
                        nutrient(detailsResponse, "mg", 1093),
                        nutrient(detailsResponse, "g", 1258)),
                Long.toString(detailsResponse.fdcId()));
    }

    private static String foodUnit(FoodDetailsResponse detailsResponse) {
        if ("Branded".equals(detailsResponse.dataType())) {
            String unit = detailsResponse.servingSizeUnit() == null ? ""
                    : detailsResponse.servingSizeUnit().trim().toLowerCase();
            if (unit.equals("g") || unit.equals("ml")) {
                return unit;
            }
            throw invalidUsdaFood("This branded food does not provide a supported g or ml nutrition basis.");
        }
        if ("Foundation".equals(detailsResponse.dataType()) || "Survey (FNDDS)".equals(detailsResponse.dataType())) {
            return "g";
        }
        throw invalidUsdaFood("This USDA food data type is not supported.");
    }

    private static Double nutrient(FoodDetailsResponse detailsResponse, String targetUnit, long... nutrientIds) {
        for (long nutrientId : nutrientIds) {
            for (var entry : detailsResponse.foodNutrients()) {
                if (entry == null || entry.nutrient() == null || entry.nutrient().id() == null
                        || entry.nutrient().id() != nutrientId || entry.amount() == null) {
                    continue;
                }
                String unit = entry.nutrient().unitName();
                if (!Double.isFinite(entry.amount()) || entry.amount() < 0 || unit == null) {
                    throw invalidUsdaFood("USDA returned an invalid nutrient amount or unit.");
                }
                double amount = entry.amount();
                unit = unit.trim().toLowerCase();
                if (targetUnit.equals(unit)) {
                    return amount;
                }
                if (targetUnit.equals("g") && unit.equals("mg")) {
                    return amount / 1000;
                }
                if (targetUnit.equals("mg") && unit.equals("g")) {
                    return amount * 1000;
                }
                if (targetUnit.equals("kcal") && unit.equals("kj")) {
                    return amount / 4.184;
                }
                throw invalidUsdaFood("USDA returned an unsupported nutrient unit.");
            }
        }
        return null;
    }

    private static ResponseStatusException invalidUsdaFood(String message) {
        return new ResponseStatusException(HttpStatus.BAD_GATEWAY, message);
    }
}
