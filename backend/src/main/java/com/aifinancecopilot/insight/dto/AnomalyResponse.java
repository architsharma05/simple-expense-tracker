package com.aifinancecopilot.insight.dto;

import java.math.BigDecimal;

public record AnomalyResponse(String category, BigDecimal currentMonthSpend, BigDecimal trailingMonthlyAverage,
                              BigDecimal difference, String explanation) {}
