package com.aifinancecopilot.goal.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record GoalRequest(
        @NotBlank @Size(max = 150) String goalName,
        @NotNull @DecimalMin(value = "0.01") @Digits(integer = 10, fraction = 2) BigDecimal targetAmount,
        @NotNull @FutureOrPresent LocalDate targetDate
) {}
