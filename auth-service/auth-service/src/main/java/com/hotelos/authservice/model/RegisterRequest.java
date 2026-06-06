package com.hotelos.authservice.model;

public record RegisterRequest(
        String username,
        String password,
        Role role
) {
}
