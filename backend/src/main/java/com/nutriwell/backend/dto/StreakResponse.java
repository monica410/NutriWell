package com.nutriwell.backend.dto;

import java.time.LocalDate;

public record StreakResponse(LocalDate throughDate, int anyGoal, int allGoals) { }
