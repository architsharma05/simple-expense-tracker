package com.aifinancecopilot.budget.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record BudgetRequest(
        @NotBlank @Size(max = 100) String category,
        @NotNull @DecimalMin(value = "0.00") @Digits(integer = 10, fraction = 2) BigDecimal monthlyLimit
) {}
