package com.aifinancecopilot.goal;

import com.aifinancecopilot.common.api.ApiResponse;
import com.aifinancecopilot.goal.dto.GoalRequest;
import com.aifinancecopilot.goal.dto.GoalResponse;
import com.aifinancecopilot.security.AuthenticatedUser;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/goals")
public class GoalController {
    private final GoalService goalService;

    public GoalController(GoalService goalService) { this.goalService = goalService; }

    @GetMapping
    public ApiResponse<List<GoalResponse>> list(@AuthenticationPrincipal AuthenticatedUser principal) {
        return ApiResponse.success(goalService.list(principal.getUser()));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<GoalResponse> create(@AuthenticationPrincipal AuthenticatedUser principal, @Valid @RequestBody GoalRequest request) {
        return ApiResponse.success(goalService.create(principal.getUser(), request));
    }

    @PutMapping("/{id}")
    public ApiResponse<GoalResponse> update(@AuthenticationPrincipal AuthenticatedUser principal, @PathVariable UUID id, @Valid @RequestBody GoalRequest request) {
        return ApiResponse.success(goalService.update(principal.getUser(), id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@AuthenticationPrincipal AuthenticatedUser principal, @PathVariable UUID id) {
        goalService.delete(principal.getUser(), id);
        return ApiResponse.success(null);
    }
}
