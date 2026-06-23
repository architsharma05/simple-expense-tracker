package com.aifinancecopilot.transaction.dto;

import com.aifinancecopilot.transaction.TransactionType;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionRequest(
        @NotNull TransactionType type,
        @NotBlank @Size(max = 100) String category,
        @NotNull @DecimalMin(value = "0.01") @Digits(integer = 10, fraction = 2) BigDecimal amount,
        @Size(max = 1000) String description,
        @NotNull LocalDate transactionDate
) {}
