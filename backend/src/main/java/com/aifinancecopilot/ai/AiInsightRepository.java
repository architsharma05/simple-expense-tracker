package com.aifinancecopilot.ai;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiInsightRepository extends JpaRepository<AiInsight, UUID> {
    List<AiInsight> findTop20ByUserIdOrderByGeneratedAtDesc(UUID userId);
}
