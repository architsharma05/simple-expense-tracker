package com.aifinancecopilot.receipt;

import com.aifinancecopilot.user.User;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "receipts")
public class Receipt {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "file_url", nullable = false, columnDefinition = "text")
    private String fileUrl;

    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private Instant uploadedAt;

    protected Receipt() {}

    public Receipt(User user, String fileUrl) {
        this.user = user;
        this.fileUrl = fileUrl;
    }

    @PrePersist
    void prePersist() {
        if (uploadedAt == null) uploadedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public User getUser() { return user; }
    public String getFileUrl() { return fileUrl; }
    public Instant getUploadedAt() { return uploadedAt; }
}
