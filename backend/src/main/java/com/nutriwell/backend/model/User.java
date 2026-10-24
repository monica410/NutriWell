package com.nutriwell.backend.model;

public record User(
        Integer userId,
        String username,
        String email,
        String passwordHash
) {
}
