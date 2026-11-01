package com.nutriwell.backend.dto;

public record LoginResponse(Integer userId, String username, String email, String message) {
}
