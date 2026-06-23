package com.aifinancecopilot.insight.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RecurringExpenseResponse(String category, String description, BigDecimal typicalAmount,
                                       long occurrenceCount, LocalDate firstSeen, LocalDate lastSeen) {}
