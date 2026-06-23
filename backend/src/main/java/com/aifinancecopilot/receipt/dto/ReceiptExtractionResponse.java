package com.aifinancecopilot.receipt.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ReceiptExtractionResponse(String merchant, BigDecimal amount, LocalDate transactionDate,
                                        String category, String status, String notes) {}
