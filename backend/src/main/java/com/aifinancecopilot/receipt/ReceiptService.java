package com.aifinancecopilot.receipt;

import com.aifinancecopilot.receipt.dto.ReceiptExtractionResponse;
import com.aifinancecopilot.receipt.dto.ReceiptResponse;
import com.aifinancecopilot.user.User;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ReceiptService {
    private final ReceiptRepository receiptRepository;
    private final Path storageRoot;

    public ReceiptService(ReceiptRepository receiptRepository,
                          @Value("${app.receipts.storage-path:uploads/receipts}") String storagePath) {
        this.receiptRepository = receiptRepository;
        this.storageRoot = Path.of(storagePath);
    }

    @Transactional(readOnly = true)
    public List<ReceiptResponse> list(User user) {
        return receiptRepository.findByUserIdOrderByUploadedAtDesc(user.getId()).stream().map(ReceiptResponse::from).toList();
    }

    @Transactional
    public ReceiptResponse upload(User user, MultipartFile file) {
        if (file.isEmpty()) throw new IllegalArgumentException("Receipt file is required");
        String original = StringUtils.cleanPath(file.getOriginalFilename() == null ? "receipt" : file.getOriginalFilename());
        String extension = extension(original);
        if (!List.of(".png", ".jpg", ".jpeg", ".pdf", ".webp").contains(extension.toLowerCase())) {
            throw new IllegalArgumentException("Receipt must be a PNG, JPG, WEBP, or PDF file");
        }
        try {
            Path userDirectory = storageRoot.resolve(user.getId().toString());
            Files.createDirectories(userDirectory);
            String storedName = UUID.randomUUID() + extension;
            Path target = userDirectory.resolve(storedName);
            file.transferTo(target);
            Receipt receipt = receiptRepository.save(new Receipt(user, target.toString()));
            return ReceiptResponse.from(receipt);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to store receipt", exception);
        }
    }

    @Transactional
    public void delete(User user, UUID id) {
        Receipt receipt = findOwned(user, id);
        receiptRepository.delete(receipt);
        try {
            Files.deleteIfExists(Path.of(receipt.getFileUrl()));
        } catch (IOException ignored) {
            // File cleanup should not fail the database delete.
        }
    }

    @Transactional(readOnly = true)
    public ReceiptExtractionResponse extract(User user, UUID id) {
        Receipt receipt = findOwned(user, id);
        return new ReceiptExtractionResponse("Pending OCR", BigDecimal.ZERO, LocalDate.now(), "General", "PREVIEW",
                "Receipt stored at " + receipt.getFileUrl() + ". OCR extraction is scaffolded for the next AI vision integration step.");
    }

    private Receipt findOwned(User user, UUID id) {
        return receiptRepository.findByIdAndUserId(id, user.getId()).orElseThrow(() -> new IllegalArgumentException("Receipt not found"));
    }

    private String extension(String filename) {
        int index = filename.lastIndexOf('.');
        return index >= 0 ? filename.substring(index) : "";
    }
}
