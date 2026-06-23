package com.aifinancecopilot.auth;

import com.aifinancecopilot.auth.dto.AuthResponse;
import com.aifinancecopilot.auth.dto.LoginRequest;
import com.aifinancecopilot.auth.dto.RegisterRequest;
import com.aifinancecopilot.security.JwtService;
import com.aifinancecopilot.user.User;
import com.aifinancecopilot.user.UserRepository;
import com.aifinancecopilot.user.dto.UserResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("Email is already registered");
        }
        User user = userRepository.save(new User(email, passwordEncoder.encode(request.password())));
        return authResponse(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmailIgnoreCase(normalizeEmail(request.email()))
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email or password");
        }
        return authResponse(user);
    }

    private AuthResponse authResponse(User user) {
        return new AuthResponse("Bearer", jwtService.generateToken(user), jwtService.getExpirationSeconds(), UserResponse.from(user));
    }

    private static String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }
}
