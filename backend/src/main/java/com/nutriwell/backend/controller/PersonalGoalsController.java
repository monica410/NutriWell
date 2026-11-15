package com.nutriwell.backend.controller;

import com.nutriwell.backend.dto.PersonalGoalsRequest;
import com.nutriwell.backend.model.PersonalGoals;
import com.nutriwell.backend.service.PersonalGoalsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/goals")
@CrossOrigin(origins = "http://localhost:5173")
public class PersonalGoalsController {
    private final PersonalGoalsService goalsService;

    public PersonalGoalsController(PersonalGoalsService goalsService) {
        this.goalsService = goalsService;
    }

    @PostMapping
    public ResponseEntity<PersonalGoals> createGoals(@RequestBody PersonalGoalsRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(goalsService.createGoals(request));
    }

    @GetMapping
    public PersonalGoals getGoals(@RequestParam int userId) {
        return goalsService.getGoals(userId);
    }

    @PatchMapping
    public PersonalGoals updateGoals(@RequestBody PersonalGoalsRequest request) {
        return goalsService.updateGoals(request);
    }

    @DeleteMapping("/{target}")
    public ResponseEntity<Void> deleteTarget(@PathVariable String target, @RequestParam int userId) {
        goalsService.deleteTarget(userId, target);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteGoals(@RequestParam int userId) {
        goalsService.deleteGoals(userId);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> handleGoalsError(ResponseStatusException exception) {
        return ResponseEntity.status(exception.getStatusCode()).body(Map.of("error",
                exception.getReason() != null ? exception.getReason() : "Unable to process the request."));
    }
}
