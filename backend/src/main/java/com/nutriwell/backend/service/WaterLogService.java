package com.nutriwell.backend.service;

import com.nutriwell.backend.dto.WaterLogAmountRequest;
import com.nutriwell.backend.dto.WaterLogRequest;
import com.nutriwell.backend.model.WaterLog;
import com.nutriwell.backend.repository.UserRepository;
import com.nutriwell.backend.repository.WaterLogRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

@Service
public class WaterLogService {
    private final WaterLogRepository waterLogRepository;
    private final UserRepository userRepository;

    public WaterLogService(WaterLogRepository waterLogRepository, UserRepository userRepository) {
        this.waterLogRepository = waterLogRepository;
        this.userRepository = userRepository;
    }

    public WaterLog logWater(WaterLogRequest request) {
        if (request == null || request.loggedDate() == null) {
            throw badRequest("A logged date is required.");
        }
        validateAmount(request.amountMl());
        validateUser(request.userId());
        if (waterLogRepository.findByUserIdAndLoggedDate(request.userId(), request.loggedDate()).isPresent()) {
            throw dailyLogExists();
        }
        try {
            return waterLogRepository.save(request.userId(), request.amountMl(), request.loggedDate());
        } catch (DuplicateKeyException exception) {
            throw dailyLogExists();
        }
    }

    public WaterLog getWaterLog(int userId, LocalDate date) {
        if (date == null) throw badRequest("A date is required.");
        validateUser(userId);
        return waterLogRepository.findByUserIdAndLoggedDate(userId, date).orElseThrow(WaterLogService::notFound);
    }

    public void updateAmount(int waterLogId, WaterLogAmountRequest request) {
        if (waterLogId <= 0) throw badRequest("A positive water log ID is required.");
        if (request == null) throw badRequest("An amount and userId are required.");
        validateAmount(request.amountMl());
        validateUser(request.userId());
        if (!waterLogRepository.updateAmount(waterLogId, request.userId(), request.amountMl())) {
            throw notFound();
        }
    }

    public void deleteWaterLog(int waterLogId, int userId) {
        if (waterLogId <= 0) throw badRequest("A positive water log ID is required.");
        validateUser(userId);
        if (!waterLogRepository.deleteByIdAndUserId(waterLogId, userId)) throw notFound();
    }

    private void validateUser(Integer userId) {
        if (userId == null || userId <= 0) throw badRequest("A positive userId is required.");
        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User was not found.");
        }
    }

    private static void validateAmount(Double amount) {
        if (amount == null || amount <= 0 || amount > 9999999) {
            throw badRequest("Enter a positive number up to 9999999.");
        }
    }

    private static ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    private static ResponseStatusException notFound() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Water log entry was not found.");
    }

    private static ResponseStatusException dailyLogExists() {
        return new ResponseStatusException(HttpStatus.CONFLICT,
                "Water is already logged for this day. Edit the existing amount instead.");
    }
}
