package com.aifinancecopilot.forecast;

import com.aifinancecopilot.common.api.ApiResponse;
import com.aifinancecopilot.forecast.dto.CategoryForecastResponse;
import com.aifinancecopilot.forecast.dto.MonthEndForecastResponse;
import com.aifinancecopilot.security.AuthenticatedUser;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/forecast")
public class ForecastController {
    private final ForecastService forecastService;

    public ForecastController(ForecastService forecastService) { this.forecastService = forecastService; }

    @GetMapping("/month-end")
    public ApiResponse<MonthEndForecastResponse> monthEnd(@AuthenticationPrincipal AuthenticatedUser principal) {
        return ApiResponse.success(forecastService.monthEnd(principal.getUser()));
    }

    @GetMapping("/categories")
    public ApiResponse<List<CategoryForecastResponse>> categories(@AuthenticationPrincipal AuthenticatedUser principal) {
        return ApiResponse.success(forecastService.categoryForecast(principal.getUser()));
    }
}
