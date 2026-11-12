package com.nutriwell.backend.controller;

import com.nutriwell.backend.dto.WaterLogAmountRequest;
import com.nutriwell.backend.dto.WaterLogRequest;
import com.nutriwell.backend.model.WaterLog;
import com.nutriwell.backend.service.WaterLogService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/water-logs")
@CrossOrigin(origins = "http://localhost:5173")
public class WaterLogController {
    private final WaterLogService waterLogService;

    public WaterLogController(WaterLogService waterLogService) {
        this.waterLogService = waterLogService;
    }

    @PostMapping
    public ResponseEntity<WaterLog> logWater(@RequestBody WaterLogRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(waterLogService.logWater(request));
    }

    @GetMapping
    public WaterLog getWaterLog(@RequestParam int userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return waterLogService.getWaterLog(userId, date);
    }

    @PatchMapping("/{waterLogId}")
    public ResponseEntity<Void> updateAmount(@PathVariable int waterLogId,
            @RequestBody WaterLogAmountRequest request) {
        waterLogService.updateAmount(waterLogId, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{waterLogId}")
    public ResponseEntity<Void> deleteWaterLog(@PathVariable int waterLogId, @RequestParam int userId) {
        waterLogService.deleteWaterLog(waterLogId, userId);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> handleWaterLogError(ResponseStatusException exception) {
        return ResponseEntity.status(exception.getStatusCode()).body(Map.of("error",
                exception.getReason() != null ? exception.getReason() : "Unable to process the request."));
    }
}
