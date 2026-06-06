package com.hotelos.authservice.model;

public record LoginRequest (
        String login,
        String password
){
}
