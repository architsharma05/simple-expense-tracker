package com.aifinancecopilot.ai.dto;

import com.aifinancecopilot.transaction.TransactionType;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record CategorizeTransactionRequest(@NotNull TransactionType type,
                                           @NotNull @DecimalMin("0.01") BigDecimal amount,
                                           @NotBlank @Size(max = 500) String description) {}
