package com.aifinancecopilot.forecast.dto;

import java.math.BigDecimal;
import java.time.YearMonth;

public record MonthEndForecastResponse(YearMonth month, BigDecimal spentSoFar, BigDecimal projectedSpend,
                                       BigDecimal projectedIncome, BigDecimal projectedNet, String riskLevel) {}
