package com.aifinancecopilot.dashboard.dto;

import java.math.BigDecimal;

public record CategorySummaryResponse(String category, BigDecimal total) {}
