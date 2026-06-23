package com.aifinancecopilot.receipt.dto;

import com.aifinancecopilot.receipt.Receipt;
import java.time.Instant;
import java.util.UUID;

public record ReceiptResponse(UUID id, String fileUrl, Instant uploadedAt) {
    public static ReceiptResponse from(Receipt receipt) {
        return new ReceiptResponse(receipt.getId(), receipt.getFileUrl(), receipt.getUploadedAt());
    }
}
