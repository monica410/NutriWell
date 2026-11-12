package com.nutriwell.backend.service;

import com.nutriwell.backend.dto.FoodLogRequest;
import com.nutriwell.backend.dto.FoodLogAmountRequest;
import com.nutriwell.backend.dto.FoodLogResponse;
import com.nutriwell.backend.model.Food;
import com.nutriwell.backend.model.FoodLog;
import com.nutriwell.backend.model.Nutrition;
import com.nutriwell.backend.repository.FoodLogRepository;
import com.nutriwell.backend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@Service
public class FoodLogService {

    private final FoodService foodService;
    private final FoodLogRepository foodLogRepository;
    private final UserRepository userRepository;

    public FoodLogService(FoodService foodService, FoodLogRepository foodLogRepository, UserRepository userRepository) {
        this.foodService = foodService;
        this.foodLogRepository = foodLogRepository;
        this.userRepository = userRepository;
    }

    public FoodLogResponse logFood(FoodLogRequest request) {
        if (request == null || request.fdcId() == null || request.fdcId() <= 0) {
            throw badRequest("A positive USDA fdcId is required.");
        }

        if (request.loggedDate() == null)
            throw badRequest("null date");

        if (request.amount() == null)
            throw badRequest("null amount");
        double amount = request.amount();

        String unit = request.unit() == null ? "" : request.unit().trim().toLowerCase();
        if (unit.isEmpty()) {
            throw badRequest("no unit");
        }

        validateUser(request.userId());

        Food food = foodService.getOrImportFood(request.fdcId());
        if (!unit.equals(food.unit())) {
            throw badRequest("This food must be logged in " + food.unit() + ".");
        }
        FoodLog log = foodLogRepository.save(request.userId(), food, amount, request.loggedDate());
        return response(log);
    }

    public List<FoodLogResponse> getFoodLogs(int userId, LocalDate date) {
        if (date == null)
            throw badRequest("null date");

        validateUser(userId);
        return foodLogRepository.findByUserIdAndLoggedDate(userId, date).stream().map(this::response).toList();
    }

    public void updateAmount(int foodLogId, FoodLogAmountRequest request) {
        if (foodLogId <= 0 || request == null) {
            throw badRequest("A positive food log ID and amount are required.");
        }
        Double amount = request.amount();
        if (amount == null || amount <= 0 || amount > 9999999) {
            throw badRequest("Enter a positive amount up to 9999999.");
        }
        validateUser(request.userId());
        if (!foodLogRepository.updateAmount(foodLogId, request.userId(), amount)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Food log entry was not found.");
        }
    }

    public void deleteFoodLog(int foodLogId, int userId) {
        if (foodLogId <= 0) {
            throw badRequest("A positive food log ID is required.");
        }
        validateUser(userId);
        if (!foodLogRepository.deleteByIdAndUserId(foodLogId, userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Food log entry was not found.");
        }
    }

    private FoodLogResponse response(FoodLog log) {
        Food food = log.food();
        Double amount = log.amount();
        Nutrition consumed = food.nutrition().scaleAmount(amount);
        return new FoodLogResponse(log.foodLogId(), log.userId(), food.foodId(), food.fdcId(),
                food.name(), amount, food.unit(), log.loggedDate(), consumed);
    }

    private void validateUser(Integer userId) {
        if (userId == null || userId <= 0) {
            throw badRequest("A positive userId is required.");
        }
        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User was not found.");
        }
    }

    private static ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}
