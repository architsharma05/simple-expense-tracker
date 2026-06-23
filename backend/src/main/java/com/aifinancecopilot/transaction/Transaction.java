package com.aifinancecopilot.transaction;

import com.aifinancecopilot.user.User;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "transactions")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Column(nullable = false, length = 100)
    private String category;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(columnDefinition = "text")
    private String description;

    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected Transaction() {}

    public Transaction(User user, TransactionType type, String category, BigDecimal amount, String description, LocalDate transactionDate) {
        this.user = user;
        this.type = type;
        this.category = category;
        this.amount = amount;
        this.description = description;
        this.transactionDate = transactionDate;
    }

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = Instant.now();
    }

    public void update(TransactionType type, String category, BigDecimal amount, String description, LocalDate transactionDate) {
        this.type = type;
        this.category = category;
        this.amount = amount;
        this.description = description;
        this.transactionDate = transactionDate;
    }

    public UUID getId() { return id; }
    public User getUser() { return user; }
    public TransactionType getType() { return type; }
    public String getCategory() { return category; }
    public BigDecimal getAmount() { return amount; }
    public String getDescription() { return description; }
    public LocalDate getTransactionDate() { return transactionDate; }
    public Instant getCreatedAt() { return createdAt; }
}
