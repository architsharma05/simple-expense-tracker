package com.aifinancecopilot.ai;

import com.aifinancecopilot.user.User;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ai_insights")
public class AiInsight {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "insight_text", nullable = false, columnDefinition = "text")
    private String insightText;

    @Column(name = "generated_at", nullable = false, updatable = false)
    private Instant generatedAt;

    protected AiInsight() {}

    public AiInsight(User user, String insightText) {
        this.user = user;
        this.insightText = insightText;
    }

    @PrePersist
    void prePersist() {
        if (generatedAt == null) generatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public User getUser() { return user; }
    public String getInsightText() { return insightText; }
    public Instant getGeneratedAt() { return generatedAt; }
}
