package com.aifinancecopilot.receipt;

import com.aifinancecopilot.common.api.ApiResponse;
import com.aifinancecopilot.receipt.dto.ReceiptExtractionResponse;
import com.aifinancecopilot.receipt.dto.ReceiptResponse;
import com.aifinancecopilot.security.AuthenticatedUser;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/receipts")
public class ReceiptController {
    private final ReceiptService receiptService;

    public ReceiptController(ReceiptService receiptService) { this.receiptService = receiptService; }

    @GetMapping
    public ApiResponse<List<ReceiptResponse>> list(@AuthenticationPrincipal AuthenticatedUser principal) {
        return ApiResponse.success(receiptService.list(principal.getUser()));
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ReceiptResponse> upload(@AuthenticationPrincipal AuthenticatedUser principal, @RequestPart("file") MultipartFile file) {
        return ApiResponse.success(receiptService.upload(principal.getUser(), file));
    }

    @PostMapping("/{id}/extract")
    public ApiResponse<ReceiptExtractionResponse> extract(@AuthenticationPrincipal AuthenticatedUser principal, @PathVariable UUID id) {
        return ApiResponse.success(receiptService.extract(principal.getUser(), id));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@AuthenticationPrincipal AuthenticatedUser principal, @PathVariable UUID id) {
        receiptService.delete(principal.getUser(), id);
        return ApiResponse.success(null);
    }
}
