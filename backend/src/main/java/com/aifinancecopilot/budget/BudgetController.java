package com.aifinancecopilot.budget;

import com.aifinancecopilot.budget.dto.BudgetRequest;
import com.aifinancecopilot.budget.dto.BudgetResponse;
import com.aifinancecopilot.common.api.ApiResponse;
import com.aifinancecopilot.security.AuthenticatedUser;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/budgets")
public class BudgetController {
    private final BudgetService budgetService;

    public BudgetController(BudgetService budgetService) { this.budgetService = budgetService; }

    @GetMapping
    public ApiResponse<List<BudgetResponse>> list(@AuthenticationPrincipal AuthenticatedUser principal) {
        return ApiResponse.success(budgetService.list(principal.getUser()));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<BudgetResponse> create(@AuthenticationPrincipal AuthenticatedUser principal, @Valid @RequestBody BudgetRequest request) {
        return ApiResponse.success(budgetService.create(principal.getUser(), request));
    }

    @PutMapping("/{id}")
    public ApiResponse<BudgetResponse> update(@AuthenticationPrincipal AuthenticatedUser principal, @PathVariable UUID id, @Valid @RequestBody BudgetRequest request) {
        return ApiResponse.success(budgetService.update(principal.getUser(), id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@AuthenticationPrincipal AuthenticatedUser principal, @PathVariable UUID id) {
        budgetService.delete(principal.getUser(), id);
        return ApiResponse.success(null);
    }
}
