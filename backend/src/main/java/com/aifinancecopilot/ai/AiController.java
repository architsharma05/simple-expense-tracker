package com.aifinancecopilot.ai;

import com.aifinancecopilot.ai.dto.AiAnswerResponse;
import com.aifinancecopilot.ai.dto.AiInsightResponse;
import com.aifinancecopilot.ai.dto.AiPromptRequest;
import com.aifinancecopilot.ai.dto.CategorizeTransactionRequest;
import com.aifinancecopilot.ai.dto.CategorizeTransactionResponse;
import com.aifinancecopilot.common.api.ApiResponse;
import com.aifinancecopilot.security.AuthenticatedUser;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class AiController {
    private final AiFinanceService aiFinanceService;

    public AiController(AiFinanceService aiFinanceService) { this.aiFinanceService = aiFinanceService; }

    @PostMapping("/spending-summary")
    public ApiResponse<AiAnswerResponse> spendingSummary(@AuthenticationPrincipal AuthenticatedUser principal) {
        return ApiResponse.success(aiFinanceService.spendingSummary(principal.getUser()));
    }

    @PostMapping("/budget-coach")
    public ApiResponse<AiAnswerResponse> budgetCoach(@AuthenticationPrincipal AuthenticatedUser principal) {
        return ApiResponse.success(aiFinanceService.budgetCoach(principal.getUser()));
    }

    @PostMapping("/goal-coach")
    public ApiResponse<AiAnswerResponse> goalCoach(@AuthenticationPrincipal AuthenticatedUser principal) {
        return ApiResponse.success(aiFinanceService.goalCoach(principal.getUser()));
    }

    @PostMapping("/chat")
    public ApiResponse<AiAnswerResponse> chat(@AuthenticationPrincipal AuthenticatedUser principal, @Valid @RequestBody AiPromptRequest request) {
        return ApiResponse.success(aiFinanceService.chat(principal.getUser(), request.question()));
    }

    @PostMapping("/categorize-transaction")
    public ApiResponse<CategorizeTransactionResponse> categorize(@AuthenticationPrincipal AuthenticatedUser principal, @Valid @RequestBody CategorizeTransactionRequest request) {
        return ApiResponse.success(aiFinanceService.categorize(principal.getUser(), request));
    }

    @GetMapping("/insights")
    public ApiResponse<List<AiInsightResponse>> insights(@AuthenticationPrincipal AuthenticatedUser principal) {
        return ApiResponse.success(aiFinanceService.insights(principal.getUser()));
    }
}
