package com.nutriwell.backend.controller;

import com.nutriwell.backend.dto.FoodLogRequest;
import com.nutriwell.backend.dto.FoodLogResponse;
import com.nutriwell.backend.service.FoodLogService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/food-logs")
@CrossOrigin(origins = "http://localhost:5173")
public class FoodLogController {

    private final FoodLogService foodLogService;

    public FoodLogController(FoodLogService foodLogService) {
        this.foodLogService = foodLogService;
    }

    @PostMapping
    public ResponseEntity<FoodLogResponse> logFood(@RequestBody FoodLogRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(foodLogService.logFood(request));
    }

    @GetMapping
    public List<FoodLogResponse> getFoodLogs(
            @RequestParam int userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return foodLogService.getFoodLogs(userId, date);
    }

    @DeleteMapping("/{foodLogId}")
    public ResponseEntity<Void> deleteFoodLog(@PathVariable int foodLogId, @RequestParam int userId) {
        foodLogService.deleteFoodLog(foodLogId, userId);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> handleFoodLogError(ResponseStatusException exception) {
        return ResponseEntity.status(exception.getStatusCode())
                .body(Map.of("error", exception.getReason() != null ? exception.getReason() : "Unable to process the request."));
    }
}
