package com.skillbridge.skillbridge_api.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.skillbridge.skillbridge_api.dto.LoginResponse;
import com.skillbridge.skillbridge_api.dto.LoginRequest;
import com.skillbridge.skillbridge_api.entity.User;
import com.skillbridge.skillbridge_api.repository.UserRepository;
import com.skillbridge.skillbridge_api.security.JwtUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public LoginResponse login(LoginRequest request) {
        boolean isSignup = "SIGNUP".equalsIgnoreCase(request.getMode());

        Optional<User> existingUser = userRepository.findByEmail(request.getEmail());

        if (existingUser.isPresent()) {
            // User exists — validate password
            User user = existingUser.get();
            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                throw new RuntimeException("Invalid email or password");
            }
            String token = jwtUtil.generateToken(user.getId(), user.getEmail());
            return new LoginResponse(token);
        }

        // User does NOT exist
        if (isSignup) {
            // Create user and return JWT
            User user = new User();
            user.setEmail(request.getEmail());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setCreatedAt(LocalDateTime.now());
            userRepository.save(user);

            String token = jwtUtil.generateToken(user.getId(), user.getEmail());
            return new LoginResponse(token);
        }

        // Login mode + user not found → 404
        throw new UserNotFoundException("USER_NOT_FOUND");
    }

    public static class UserNotFoundException extends RuntimeException {
        public UserNotFoundException(String message) {
            super(message);
        }
    }
}
