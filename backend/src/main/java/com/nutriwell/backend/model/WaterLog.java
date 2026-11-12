package com.nutriwell.backend.model;

import java.time.LocalDate;

public record WaterLog(int waterLogId, int userId, Double amountMl, LocalDate loggedDate) { }
