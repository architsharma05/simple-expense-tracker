package com.aifinancecopilot.insight;

import com.aifinancecopilot.insight.dto.AnomalyResponse;
import com.aifinancecopilot.insight.dto.RecurringExpenseResponse;
import com.aifinancecopilot.transaction.Transaction;
import com.aifinancecopilot.transaction.TransactionRepository;
import com.aifinancecopilot.transaction.TransactionType;
import com.aifinancecopilot.user.User;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InsightService {
    private final TransactionRepository transactionRepository;

    public InsightService(TransactionRepository transactionRepository) { this.transactionRepository = transactionRepository; }

    @Transactional(readOnly = true)
    public List<RecurringExpenseResponse> recurringExpenses(User user) {
        LocalDate to = LocalDate.now();
        LocalDate from = to.minusMonths(6).withDayOfMonth(1);
        return transactions(user.getId(), from, to).stream()
                .filter(transaction -> transaction.getType() == TransactionType.EXPENSE)
                .collect(Collectors.groupingBy(transaction -> key(transaction.getCategory(), transaction.getDescription())))
                .values().stream()
                .filter(group -> group.size() >= 2)
                .map(this::toRecurring)
                .filter(response -> response.occurrenceCount() >= 2)
                .sorted(Comparator.comparing(RecurringExpenseResponse::typicalAmount).reversed())
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AnomalyResponse> anomalies(User user) {
        YearMonth current = YearMonth.now();
        LocalDate currentStart = current.atDay(1);
        LocalDate currentEnd = current.atEndOfMonth();
        LocalDate trailingStart = current.minusMonths(3).atDay(1);
        LocalDate trailingEnd = current.minusMonths(1).atEndOfMonth();
        Map<String, BigDecimal> currentSpend = totalsByCategory(user.getId(), currentStart, currentEnd);
        Map<String, BigDecimal> trailingSpend = totalsByCategory(user.getId(), trailingStart, trailingEnd);
        return currentSpend.entrySet().stream()
                .map(entry -> {
                    BigDecimal average = trailingSpend.getOrDefault(entry.getKey(), BigDecimal.ZERO).divide(BigDecimal.valueOf(3), 2, RoundingMode.HALF_UP);
                    BigDecimal difference = entry.getValue().subtract(average);
                    String explanation = entry.getKey() + " is above your trailing 3-month monthly average by $" + difference + ".";
                    return new AnomalyResponse(entry.getKey(), entry.getValue(), average, difference, explanation);
                })
                .filter(response -> response.difference().compareTo(BigDecimal.valueOf(25)) > 0
                        && response.currentMonthSpend().compareTo(response.trailingMonthlyAverage().multiply(BigDecimal.valueOf(1.5))) > 0)
                .sorted(Comparator.comparing(AnomalyResponse::difference).reversed())
                .toList();
    }

    private RecurringExpenseResponse toRecurring(List<Transaction> group) {
        List<Transaction> sorted = group.stream().sorted(Comparator.comparing(Transaction::getTransactionDate)).toList();
        BigDecimal typical = sorted.stream().map(Transaction::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(sorted.size()), 2, RoundingMode.HALF_UP);
        Transaction first = sorted.get(0);
        Transaction last = sorted.get(sorted.size() - 1);
        return new RecurringExpenseResponse(first.getCategory(), first.getDescription(), typical, sorted.size(), first.getTransactionDate(), last.getTransactionDate());
    }

    private Map<String, BigDecimal> totalsByCategory(UUID userId, LocalDate from, LocalDate to) {
        return transactions(userId, from, to).stream()
                .filter(transaction -> transaction.getType() == TransactionType.EXPENSE)
                .collect(Collectors.groupingBy(Transaction::getCategory, Collectors.reducing(BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add)));
    }

    private List<Transaction> transactions(UUID userId, LocalDate from, LocalDate to) {
        Specification<Transaction> specification = (root, query, cb) -> cb.and(
                cb.equal(root.get("user").get("id"), userId),
                cb.greaterThanOrEqualTo(root.get("transactionDate"), from),
                cb.lessThanOrEqualTo(root.get("transactionDate"), to));
        return transactionRepository.findAll(specification);
    }

    private String key(String category, String description) {
        String normalizedDescription = description == null ? "" : description.toLowerCase().replaceAll("[^a-z0-9 ]", "").trim();
        return category.toLowerCase() + "|" + normalizedDescription;
    }
}
