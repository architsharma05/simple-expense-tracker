package com.aifinancecopilot.auth.dto;

import com.aifinancecopilot.user.dto.UserResponse;

public record AuthResponse(String tokenType, String accessToken, long expiresInSeconds, UserResponse user) {}
