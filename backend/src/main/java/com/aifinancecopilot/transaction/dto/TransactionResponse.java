package com.aifinancecopilot.transaction.dto;

import com.aifinancecopilot.transaction.Transaction;
import com.aifinancecopilot.transaction.TransactionType;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record TransactionResponse(UUID id, TransactionType type, String category, BigDecimal amount,
                                  String description, LocalDate transactionDate, Instant createdAt) {
    public static TransactionResponse from(Transaction transaction) {
        return new TransactionResponse(transaction.getId(), transaction.getType(), transaction.getCategory(),
                transaction.getAmount(), transaction.getDescription(), transaction.getTransactionDate(), transaction.getCreatedAt());
    }
}
