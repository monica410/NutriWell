package com.nutriwell.backend.service;

import com.nutriwell.backend.dto.FrequentFoodRequest;
import com.nutriwell.backend.dto.FoodResponse;
import com.nutriwell.backend.model.Food;
import com.nutriwell.backend.repository.FrequentFoodRepository;
import com.nutriwell.backend.repository.FoodRepository;
import com.nutriwell.backend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class FrequentFoodService {
    private final FrequentFoodRepository frequentFoodRepository;
    private final FoodRepository foodRepository;
    private final UserRepository userRepository;

    public FrequentFoodService(FrequentFoodRepository frequentFoodRepository, FoodRepository foodRepository,
                               UserRepository userRepository) {
        this.frequentFoodRepository = frequentFoodRepository;
        this.foodRepository = foodRepository;
        this.userRepository = userRepository;
    }

    public FoodResponse addFrequentFood(FrequentFoodRequest request) {
        if (request == null) throw badRequest("A frequent food request is required.");
        validateUser(request.userId());
        if (request.foodId() == null || request.foodId() <= 0) {
            throw badRequest("A positive foodId is required.");
        }
        Food food = foodRepository.findById(request.foodId()).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Food was not found."));
        frequentFoodRepository.save(request.userId(), food.foodId());
        return response(food);
    }

    public List<FoodResponse> getFrequentFoods(int userId) {
        validateUser(userId);
        return frequentFoodRepository.findByUserId(userId).stream().map(this::response).toList();
    }

    public void removeFrequentFood(int userId, int foodId) {
        validateUser(userId);
        if (foodId <= 0) throw badRequest("A positive foodId is required.");
        if (!frequentFoodRepository.delete(userId, foodId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Frequent food was not found.");
        }
    }

    private FoodResponse response(Food food) {
        return new FoodResponse(food.foodId(), food.fdcId(), food.name(), food.unit(), food.nutrition());
    }

    private void validateUser(Integer userId) {
        if (userId == null || userId <= 0) throw badRequest("A positive userId is required.");
        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User was not found.");
        }
    }

    private static ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}
