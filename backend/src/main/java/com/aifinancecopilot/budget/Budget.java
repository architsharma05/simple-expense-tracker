package com.aifinancecopilot.budget;

import com.aifinancecopilot.user.User;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "budgets")
public class Budget {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 100)
    private String category;

    @Column(name = "monthly_limit", nullable = false, precision = 12, scale = 2)
    private BigDecimal monthlyLimit;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Budget() {}

    public Budget(User user, String category, BigDecimal monthlyLimit) {
        this.user = user;
        this.category = category;
        this.monthlyLimit = monthlyLimit;
    }

    @PrePersist
    void prePersist() { Instant now = Instant.now(); createdAt = now; updatedAt = now; }

    @PreUpdate
    void preUpdate() { updatedAt = Instant.now(); }

    public void update(String category, BigDecimal monthlyLimit) { this.category = category; this.monthlyLimit = monthlyLimit; }

    public UUID getId() { return id; }
    public User getUser() { return user; }
    public String getCategory() { return category; }
    public BigDecimal getMonthlyLimit() { return monthlyLimit; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
