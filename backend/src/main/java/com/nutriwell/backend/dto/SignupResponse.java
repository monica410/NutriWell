package com.nutriwell.backend.dto;

public record SignupResponse(
        Integer userId,
        String username,
        String email,
        String message
) {
}
