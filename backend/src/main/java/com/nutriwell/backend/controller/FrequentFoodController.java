package com.nutriwell.backend.controller;

import com.nutriwell.backend.dto.FoodResponse;
import com.nutriwell.backend.dto.FrequentFoodRequest;
import com.nutriwell.backend.service.FrequentFoodService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/frequent-foods")
@CrossOrigin(origins = "http://localhost:5173")
public class FrequentFoodController {
    private final FrequentFoodService frequentFoodService;

    public FrequentFoodController(FrequentFoodService frequentFoodService) {
        this.frequentFoodService = frequentFoodService;
    }

    @PostMapping
    public FoodResponse addFrequentFood(@RequestBody FrequentFoodRequest request) {
        return frequentFoodService.addFrequentFood(request);
    }

    @GetMapping
    public List<FoodResponse> getFrequentFoods(@RequestParam int userId) {
        return frequentFoodService.getFrequentFoods(userId);
    }

    @DeleteMapping("/{foodId}")
    public ResponseEntity<Void> removeFrequentFood(@PathVariable int foodId, @RequestParam int userId) {
        frequentFoodService.removeFrequentFood(userId, foodId);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> handleError(ResponseStatusException exception) {
        return ResponseEntity.status(exception.getStatusCode()).body(Map.of("error",
                exception.getReason() == null ? "Unable to process frequent foods." : exception.getReason()));
    }
}
