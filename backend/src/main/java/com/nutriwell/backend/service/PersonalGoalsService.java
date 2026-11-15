package com.nutriwell.backend.service;

import com.nutriwell.backend.dto.PersonalGoalsRequest;
import com.nutriwell.backend.model.PersonalGoals;
import com.nutriwell.backend.repository.PersonalGoalsRepository;
import com.nutriwell.backend.repository.UserRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PersonalGoalsService {
    private final PersonalGoalsRepository goalsRepository;
    private final UserRepository userRepository;

    public PersonalGoalsService(PersonalGoalsRepository goalsRepository, UserRepository userRepository) {
        this.goalsRepository = goalsRepository;
        this.userRepository = userRepository;
    }

    public PersonalGoals createGoals(PersonalGoalsRequest request) {
        validateRequest(request);
        try {
            return goalsRepository.save(request);
        } catch (DuplicateKeyException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Goals already exist for this user. Update the existing goals instead.");
        }
    }

    public PersonalGoals getGoals(int userId) {
        validateUser(userId);
        return findGoals(userId);
    }

    public PersonalGoals updateGoals(PersonalGoalsRequest request) {
        validateRequest(request);
        if (!goalsRepository.update(request)) throw notFound();
        return findGoals(request.userId());
    }

    public void deleteTarget(int userId, String targetName) {
        validateUser(userId);
        try {
            if (!goalsRepository.clearTarget(userId, targetName)) throw notFound();
        } catch (IllegalArgumentException exception) {
            throw badRequest(exception.getMessage());
        }
    }

    public void deleteGoals(int userId) {
        validateUser(userId);
        if (!goalsRepository.deleteByUserId(userId)) throw notFound();
    }

    private PersonalGoals findGoals(int userId) {
        return goalsRepository.findByUserId(userId).orElseThrow(PersonalGoalsService::notFound);
    }

    private void validateRequest(PersonalGoalsRequest request) {
        if (request == null) throw badRequest("Null request.");
        Double[] targets = {request.caloriesKcal(), request.proteinG(), request.carbohydratesG(),
                request.totalFatG(), request.fiberG(), request.totalSugarG(), request.sodiumMg(),
                request.saturatedFatG(), request.waterMl()};
        boolean hasTarget = false;
        for (Double target : targets) {
            if (target == null) continue;
            hasTarget = true;
            if (!Double.isFinite(target) || target < 0 || target > 9999999.999) {
                throw badRequest("Goal targets must positive number below 9999999.999.");
            }
        }
        if (!hasTarget) throw badRequest("At least one goal target is required.");
        validateUser(request.userId());
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

    private static ResponseStatusException notFound() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Personal goals were not found.");
    }
}
