package com.aifinancecopilot.forecast.dto;

import java.math.BigDecimal;

public record CategoryForecastResponse(String category, BigDecimal currentSpend, BigDecimal projectedSpend,
                                       BigDecimal trailingMonthlyAverage, String riskLevel) {}
