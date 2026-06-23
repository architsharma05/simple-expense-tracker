package com.aifinancecopilot.goal.dto;

import com.aifinancecopilot.goal.Goal;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record GoalResponse(UUID id, String goalName, BigDecimal targetAmount, LocalDate targetDate,
                           long daysRemaining, BigDecimal monthlySavingsRequired, Instant createdAt, Instant updatedAt) {
    public static GoalResponse from(Goal goal, long daysRemaining, BigDecimal monthlySavingsRequired) {
        return new GoalResponse(goal.getId(), goal.getGoalName(), goal.getTargetAmount(), goal.getTargetDate(),
                daysRemaining, monthlySavingsRequired, goal.getCreatedAt(), goal.getUpdatedAt());
    }
}
