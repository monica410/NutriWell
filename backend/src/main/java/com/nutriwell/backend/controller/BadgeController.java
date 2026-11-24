package com.nutriwell.backend.controller;

import com.nutriwell.backend.dto.StreakResponse;
import com.nutriwell.backend.dto.BadgeResponse;
import com.nutriwell.backend.service.BadgeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/badges")
@CrossOrigin(origins = "http://localhost:5173")
public class BadgeController {
    private final BadgeService badgeService;

    public BadgeController(BadgeService badgeService) {
        this.badgeService = badgeService;
    }

    @GetMapping("/streaks")
    public StreakResponse getStreaks(@RequestParam int userId) {
        return badgeService.getStreaks(userId);
    }

    @GetMapping
    public List<BadgeResponse> getBadges(@RequestParam int userId) {
        return badgeService.getBadges(userId);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> handleError(ResponseStatusException exception) {
        return ResponseEntity.status(exception.getStatusCode()).body(Map.of("error",
                exception.getReason() == null ? "Unable to load badges or streaks." : exception.getReason()));
    }
}
