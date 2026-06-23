package com.aifinancecopilot.ai.dto;

public record CategorizeTransactionResponse(String category, double confidence, String reason, boolean generatedByAi) {}
