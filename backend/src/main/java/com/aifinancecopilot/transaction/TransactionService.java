package com.aifinancecopilot.transaction;

import com.aifinancecopilot.transaction.dto.TransactionRequest;
import com.aifinancecopilot.transaction.dto.TransactionResponse;
import com.aifinancecopilot.user.User;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class TransactionService {
    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> search(User user, TransactionType type, String category, LocalDate from, LocalDate to, String search) {
        Specification<Transaction> specification = belongsTo(user.getId())
                .and(type == null ? null : (root, query, cb) -> cb.equal(root.get("type"), type))
                .and(StringUtils.hasText(category) ? (root, query, cb) -> cb.equal(cb.lower(root.get("category")), category.trim().toLowerCase()) : null)
                .and(from == null ? null : (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("transactionDate"), from))
                .and(to == null ? null : (root, query, cb) -> cb.lessThanOrEqualTo(root.get("transactionDate"), to))
                .and(StringUtils.hasText(search) ? textSearch(search.trim()) : null);
        return transactionRepository.findAll(specification, Sort.by(Sort.Direction.DESC, "transactionDate").and(Sort.by(Sort.Direction.DESC, "createdAt")))
                .stream().map(TransactionResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public TransactionResponse get(User user, UUID id) {
        return TransactionResponse.from(findOwned(user, id));
    }

    @Transactional
    public TransactionResponse create(User user, TransactionRequest request) {
        Transaction transaction = transactionRepository.save(new Transaction(user, request.type(), clean(request.category()), request.amount(), cleanNullable(request.description()), request.transactionDate()));
        return TransactionResponse.from(transaction);
    }

    @Transactional
    public TransactionResponse update(User user, UUID id, TransactionRequest request) {
        Transaction transaction = findOwned(user, id);
        transaction.update(request.type(), clean(request.category()), request.amount(), cleanNullable(request.description()), request.transactionDate());
        return TransactionResponse.from(transaction);
    }

    @Transactional
    public void delete(User user, UUID id) {
        transactionRepository.delete(findOwned(user, id));
    }

    private Transaction findOwned(User user, UUID id) {
        return transactionRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found"));
    }

    private static Specification<Transaction> belongsTo(UUID userId) {
        return (root, query, cb) -> cb.equal(root.get("user").get("id"), userId);
    }

    private static Specification<Transaction> textSearch(String search) {
        return (root, query, cb) -> {
            String pattern = "%" + search.toLowerCase() + "%";
            return cb.or(cb.like(cb.lower(root.get("category")), pattern), cb.like(cb.lower(cb.coalesce(root.get("description"), "")), pattern));
        };
    }

    private static String clean(String value) { return value.trim(); }
    private static String cleanNullable(String value) { return StringUtils.hasText(value) ? value.trim() : null; }
}
