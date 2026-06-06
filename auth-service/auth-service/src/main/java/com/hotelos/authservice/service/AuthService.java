package com.hotelos.authservice.service;

import com.hotelos.authservice.model.LoginRequest;
import com.hotelos.authservice.model.User;
import com.hotelos.authservice.repository.UserRepository;
import com.hotelos.authservice.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.apache.juli.logging.Log;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    public User register(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public String login(LoginRequest request) {
        User user = userRepository.findByUsername(request.login())
                .orElseThrow(() -> new RuntimeException("Foydalanuvchi topilmadi!"));

        if (passwordEncoder.matches(request.password(), user.getPassword())) {
            return tokenProvider.createToken(user.getUsername(), user.getRole());
        }
        throw new RuntimeException("Parol noto'g'ri!");
    }
}