package com.aifinancecopilot.dashboard.dto;

import java.math.BigDecimal;
import java.time.YearMonth;

public record MonthlySummaryResponse(YearMonth month, BigDecimal income, BigDecimal expenses, BigDecimal net) {}
