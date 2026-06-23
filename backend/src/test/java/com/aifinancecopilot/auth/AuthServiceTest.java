package com.aifinancecopilot.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.aifinancecopilot.auth.dto.LoginRequest;
import com.aifinancecopilot.auth.dto.RegisterRequest;
import com.aifinancecopilot.security.JwtService;
import com.aifinancecopilot.user.User;
import com.aifinancecopilot.user.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    private PasswordEncoder passwordEncoder;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        authService = new AuthService(userRepository, passwordEncoder, jwtService);
    }

    @Test
    void registerNormalizesEmailHashesPasswordAndReturnsToken() {
        when(userRepository.existsByEmailIgnoreCase("user@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token");
        when(jwtService.getExpirationSeconds()).thenReturn(3600L);

        var response = authService.register(new RegisterRequest(" User@Example.com ", "password123"));

        assertThat(response.accessToken()).isEqualTo("jwt-token");
        assertThat(response.user().email()).isEqualTo("user@example.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void loginRejectsInvalidPassword() {
        User user = new User("user@example.com", passwordEncoder.encode("password123"));
        when(userRepository.findByEmailIgnoreCase("user@example.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.login(new LoginRequest("user@example.com", "wrong-password")))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid email or password");
    }
}
