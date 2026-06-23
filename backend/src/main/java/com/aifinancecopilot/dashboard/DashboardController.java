package com.aifinancecopilot.dashboard;

import com.aifinancecopilot.common.api.ApiResponse;
import com.aifinancecopilot.dashboard.dto.CategorySummaryResponse;
import com.aifinancecopilot.dashboard.dto.MonthlySummaryResponse;
import com.aifinancecopilot.dashboard.dto.MonthlyTrendResponse;
import com.aifinancecopilot.security.AuthenticatedUser;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) { this.dashboardService = dashboardService; }

    @GetMapping("/summary")
    public ApiResponse<MonthlySummaryResponse> summary(@AuthenticationPrincipal AuthenticatedUser principal,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth month) {
        return ApiResponse.success(dashboardService.monthlySummary(principal.getUser(), month));
    }

    @GetMapping("/category-summary")
    public ApiResponse<List<CategorySummaryResponse>> categorySummary(@AuthenticationPrincipal AuthenticatedUser principal,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ApiResponse.success(dashboardService.categorySummary(principal.getUser(), from, to));
    }

    @GetMapping("/monthly-trends")
    public ApiResponse<List<MonthlyTrendResponse>> monthlyTrends(@AuthenticationPrincipal AuthenticatedUser principal, @RequestParam int year) {
        return ApiResponse.success(dashboardService.monthlyTrends(principal.getUser(), year));
    }
}
