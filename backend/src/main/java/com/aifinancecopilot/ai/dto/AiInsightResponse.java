package com.aifinancecopilot.ai.dto;

import com.aifinancecopilot.ai.AiInsight;
import java.time.Instant;
import java.util.UUID;

public record AiInsightResponse(UUID id, String insightText, Instant generatedAt) {
    public static AiInsightResponse from(AiInsight insight) {
        return new AiInsightResponse(insight.getId(), insight.getInsightText(), insight.getGeneratedAt());
    }
}
