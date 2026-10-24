package com.nutriwell.backend.dto;

public record SignupRequest(
        String username,
        String email,
        String password
) {
}
