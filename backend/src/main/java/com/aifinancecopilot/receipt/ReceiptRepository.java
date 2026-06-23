package com.aifinancecopilot.receipt;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReceiptRepository extends JpaRepository<Receipt, UUID> {
    List<Receipt> findByUserIdOrderByUploadedAtDesc(UUID userId);
    Optional<Receipt> findByIdAndUserId(UUID id, UUID userId);
}
