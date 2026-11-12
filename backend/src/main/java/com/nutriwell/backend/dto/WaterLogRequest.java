package com.nutriwell.backend.dto;

import java.time.LocalDate;

public record WaterLogRequest(Integer userId, Double amountMl, LocalDate loggedDate) { }
