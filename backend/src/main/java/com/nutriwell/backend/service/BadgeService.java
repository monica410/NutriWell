package com.nutriwell.backend.service;

import com.nutriwell.backend.dto.BadgeResponse;
import com.nutriwell.backend.dto.StreakResponse;
import com.nutriwell.backend.model.Nutrition;
import com.nutriwell.backend.model.PersonalGoals;
import com.nutriwell.backend.repository.PersonalGoalsRepository;
import com.nutriwell.backend.repository.BadgeRepository;
import com.nutriwell.backend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@Service
public class BadgeService {
    private static final String[] NAMES = {"caloriesKcal", "proteinG", "carbohydratesG", "totalFatG",
            "fiberG", "totalSugarG", "sodiumMg", "saturatedFatG", "waterMl"};
    private final PersonalGoalsRepository goalsRepository;
    private final BadgeRepository badgeRepository;
    private final UserRepository userRepository;

    public BadgeService(PersonalGoalsRepository goalsRepository, BadgeRepository badgeRepository,
                         UserRepository userRepository) {
        this.goalsRepository = goalsRepository;
        this.badgeRepository = badgeRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public StreakResponse getStreaks(int userId) {
        validateUser(userId);

        LocalDate through = LocalDate.now().minusDays(1);
        PersonalGoals goals = goalsRepository.findByUserId(userId).orElse(null);
        if (goals == null) return new StreakResponse(through, 0, 0);
        Double[] targets = {goals.caloriesKcal(), goals.proteinG(), goals.carbohydratesG(), goals.totalFatG(),
                goals.fiberG(), goals.totalSugarG(), goals.sodiumMg(), goals.saturatedFatG(), goals.waterMl()};
        boolean[] maxGoals = new boolean[targets.length];
        if (goals.maxGoals() != null) {
            for (int i = 0; i < targets.length; i++) {
                maxGoals[i] = Boolean.TRUE.equals(goals.maxGoals().get(NAMES[i]));
            }
        }

        int anyStreak = 0;
        int allStreak = 0;
        boolean allContinues = true;
        for (LocalDate date = through; ; date = date.minusDays(1)) {
            Nutrition nutrition = badgeRepository.findFoodLogsByDate(userId, date).stream()
                    .map(log -> log.food().nutrition().scaleAmount(log.amount()))
                    .reduce(Nutrition::plus).orElse(null);
            Double[] totals = nutrition == null ? new Double[9] : new Double[]{
                    nutrition.caloriesKcal(), nutrition.proteinG(), nutrition.carbohydratesG(), nutrition.totalFatG(),
                    nutrition.fiberG(), nutrition.totalSugarG(), nutrition.sodiumMg(), nutrition.saturatedFatG(), null};
            totals[8] = badgeRepository.findWaterLogsByDate(userId, date).stream()
                    .map(log -> log.amountMl()).findFirst().orElse(null);

            int g = 0;
            int met = 0;
            for (int i = 0; i < targets.length; i++) {
                if (targets[i] == null)
                    continue;
                g++;
                Double total = totals[i];
                if (total != null && Double.isFinite(total) &&
                        (maxGoals[i] ? total <= targets[i] : total >= targets[i]))
                    met++;
            }

            if (met == 0)
                break;
            anyStreak++;

            if (allContinues && g > 0 && met == g)
                allStreak++;
            else
                allContinues = false;
        }

        badgeRepository.awardStreakBadges(userId, "ANY_GOAL_STREAK", anyStreak, through);
        badgeRepository.awardStreakBadges(userId, "ALL_GOALS_STREAK", allStreak, through);
        return new StreakResponse(through, anyStreak, allStreak);
    }

    @Transactional(readOnly = true)
    public List<BadgeResponse> getBadges(int userId) {
        validateUser(userId);
        return badgeRepository.findBadgesByUserId(userId);
    }

    private void validateUser(int userId) {
        if (userId <= 0)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A positive userId is required.");
        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User was not found.");
        }
    }
}
