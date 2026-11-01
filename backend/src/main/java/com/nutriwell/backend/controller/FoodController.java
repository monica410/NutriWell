package com.nutriwell.backend.controller;

import com.nutriwell.backend.dto.FoodDetailsResponse;
import com.nutriwell.backend.dto.FoodResponse;
import com.nutriwell.backend.dto.FoodSearchResponse;
import com.nutriwell.backend.service.FoodService;
import com.nutriwell.backend.service.UsdaFoodService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/foods")
@CrossOrigin(origins = "http://localhost:5173")
public class FoodController {

    private final UsdaFoodService usdaFoodService;
    private final FoodService foodService;

    public FoodController(UsdaFoodService usdaFoodService, FoodService foodService) {
        this.usdaFoodService = usdaFoodService;
        this.foodService = foodService;
    }

    @GetMapping("/search")
    public FoodSearchResponse searchFoods(
            @RequestParam String query,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(defaultValue = "1") int pageNumber) {
        return usdaFoodService.searchFoods(query, pageSize, pageNumber);
    }

    @GetMapping("/{fdcId}")
    public FoodDetailsResponse getFood(@PathVariable long fdcId) {
        return usdaFoodService.getFood(fdcId);
    }

    @PostMapping("/{fdcId}/import")
    public FoodResponse importFood(@PathVariable long fdcId) {
        return foodService.importFood(fdcId);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> handleFoodError(ResponseStatusException exception) {
        return ResponseEntity.status(exception.getStatusCode())
                .body(Map.of("error", exception.getReason() != null ? exception.getReason() : "Unable to process the request."));
    }
}
