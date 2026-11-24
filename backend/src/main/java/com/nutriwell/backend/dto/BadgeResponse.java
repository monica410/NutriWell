package com.nutriwell.backend.dto;

import java.time.LocalDate;

public record BadgeResponse(int badgeId, String name, String type, int number, LocalDate earnedDate) { }
