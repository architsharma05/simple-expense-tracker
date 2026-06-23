package com.aifinancecopilot.dashboard;

import com.aifinancecopilot.dashboard.dto.CategorySummaryResponse;
import com.aifinancecopilot.dashboard.dto.MonthlySummaryResponse;
import com.aifinancecopilot.dashboard.dto.MonthlyTrendResponse;
import com.aifinancecopilot.transaction.Transaction;
import com.aifinancecopilot.transaction.TransactionRepository;
import com.aifinancecopilot.transaction.TransactionType;
import com.aifinancecopilot.user.User;
import java.math.BigDecimal;
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
public class DashboardService {
    private final TransactionRepository transactionRepository;

    public DashboardService(TransactionRepository transactionRepository) { this.transactionRepository = transactionRepository; }

    @Transactional(readOnly = true)
    public MonthlySummaryResponse monthlySummary(User user, YearMonth month) {
        List<Transaction> transactions = between(user.getId(), month.atDay(1), month.atEndOfMonth());
        BigDecimal income = total(transactions, TransactionType.INCOME);
        BigDecimal expenses = total(transactions, TransactionType.EXPENSE);
        return new MonthlySummaryResponse(month, income, expenses, income.subtract(expenses));
    }

    @Transactional(readOnly = true)
    public List<CategorySummaryResponse> categorySummary(User user, LocalDate from, LocalDate to) {
        return between(user.getId(), from, to).stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .collect(Collectors.groupingBy(Transaction::getCategory, Collectors.reducing(BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add)))
                .entrySet().stream()
                .map(entry -> new CategorySummaryResponse(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(CategorySummaryResponse::total).reversed())
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MonthlyTrendResponse> monthlyTrends(User user, int year) {
        LocalDate from = LocalDate.of(year, 1, 1);
        LocalDate to = LocalDate.of(year, 12, 31);
        Map<YearMonth, List<Transaction>> byMonth = between(user.getId(), from, to).stream()
                .collect(Collectors.groupingBy(t -> YearMonth.from(t.getTransactionDate())));
        return java.util.stream.IntStream.rangeClosed(1, 12)
                .mapToObj(month -> {
                    YearMonth yearMonth = YearMonth.of(year, month);
                    List<Transaction> transactions = byMonth.getOrDefault(yearMonth, List.of());
                    BigDecimal income = total(transactions, TransactionType.INCOME);
                    BigDecimal expenses = total(transactions, TransactionType.EXPENSE);
                    return new MonthlyTrendResponse(yearMonth, income, expenses, income.subtract(expenses));
                }).toList();
    }

    private List<Transaction> between(UUID userId, LocalDate from, LocalDate to) {
        Specification<Transaction> specification = (root, query, cb) -> cb.and(
                cb.equal(root.get("user").get("id"), userId),
                cb.greaterThanOrEqualTo(root.get("transactionDate"), from),
                cb.lessThanOrEqualTo(root.get("transactionDate"), to));
        return transactionRepository.findAll(specification);
    }

    private static BigDecimal total(List<Transaction> transactions, TransactionType type) {
        return transactions.stream().filter(t -> t.getType() == type).map(Transaction::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
