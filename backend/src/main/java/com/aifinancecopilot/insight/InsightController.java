package com.aifinancecopilot.insight;

import com.aifinancecopilot.common.api.ApiResponse;
import com.aifinancecopilot.insight.dto.AnomalyResponse;
import com.aifinancecopilot.insight.dto.RecurringExpenseResponse;
import com.aifinancecopilot.security.AuthenticatedUser;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/insights")
public class InsightController {
    private final InsightService insightService;

    public InsightController(InsightService insightService) { this.insightService = insightService; }

    @GetMapping("/recurring-expenses")
    public ApiResponse<List<RecurringExpenseResponse>> recurringExpenses(@AuthenticationPrincipal AuthenticatedUser principal) {
        return ApiResponse.success(insightService.recurringExpenses(principal.getUser()));
    }

    @GetMapping("/anomalies")
    public ApiResponse<List<AnomalyResponse>> anomalies(@AuthenticationPrincipal AuthenticatedUser principal) {
        return ApiResponse.success(insightService.anomalies(principal.getUser()));
    }
}
