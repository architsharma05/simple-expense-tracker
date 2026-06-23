package com.aifinancecopilot.ai.dto;

import java.time.Instant;

public record AiAnswerResponse(String answer, boolean generatedByAi, Instant generatedAt) {}
