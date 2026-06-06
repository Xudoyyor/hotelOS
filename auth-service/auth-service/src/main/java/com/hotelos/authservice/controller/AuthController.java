
package com.hotelos.authservice.controller;

import com.hotelos.authservice.model.LoginRequest;
import com.hotelos.authservice.model.RegisterRequest;
import com.hotelos.authservice.model.User;
import com.hotelos.authservice.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {

        User user = User.builder()
                .username(request.username())
                .password(request.password())
                .role(String.valueOf(request.role()))
                .build();

        return ResponseEntity.ok(authService.register(user));
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}