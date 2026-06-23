package com.aifinancecopilot.transaction;

import com.aifinancecopilot.common.api.ApiResponse;
import com.aifinancecopilot.security.AuthenticatedUser;
import com.aifinancecopilot.transaction.dto.TransactionRequest;
import com.aifinancecopilot.transaction.dto.TransactionResponse;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {
    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) { this.transactionService = transactionService; }

    @GetMapping
    public ApiResponse<List<TransactionResponse>> search(@AuthenticationPrincipal AuthenticatedUser principal,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String search) {
        return ApiResponse.success(transactionService.search(principal.getUser(), type, category, from, to, search));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<TransactionResponse> create(@AuthenticationPrincipal AuthenticatedUser principal, @Valid @RequestBody TransactionRequest request) {
        return ApiResponse.success(transactionService.create(principal.getUser(), request));
    }

    @GetMapping("/{id}")
    public ApiResponse<TransactionResponse> get(@AuthenticationPrincipal AuthenticatedUser principal, @PathVariable UUID id) {
        return ApiResponse.success(transactionService.get(principal.getUser(), id));
    }

    @PutMapping("/{id}")
    public ApiResponse<TransactionResponse> update(@AuthenticationPrincipal AuthenticatedUser principal, @PathVariable UUID id, @Valid @RequestBody TransactionRequest request) {
        return ApiResponse.success(transactionService.update(principal.getUser(), id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@AuthenticationPrincipal AuthenticatedUser principal, @PathVariable UUID id) {
        transactionService.delete(principal.getUser(), id);
        return ApiResponse.success(null);
    }
}
