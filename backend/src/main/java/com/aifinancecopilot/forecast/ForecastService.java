package com.aifinancecopilot.forecast;

import com.aifinancecopilot.forecast.dto.CategoryForecastResponse;
import com.aifinancecopilot.forecast.dto.MonthEndForecastResponse;
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
public class ForecastService {
    private final TransactionRepository transactionRepository;

    public ForecastService(TransactionRepository transactionRepository) { this.transactionRepository = transactionRepository; }

    @Transactional(readOnly = true)
    public MonthEndForecastResponse monthEnd(User user) {
        YearMonth month = YearMonth.now();
        LocalDate today = LocalDate.now();
        List<Transaction> transactions = transactions(user.getId(), month.atDay(1), today);
        BigDecimal spent = total(transactions, TransactionType.EXPENSE);
        BigDecimal income = total(transactions, TransactionType.INCOME);
        BigDecimal multiplier = BigDecimal.valueOf(month.lengthOfMonth()).divide(BigDecimal.valueOf(today.getDayOfMonth()), 4, RoundingMode.HALF_UP);
        BigDecimal projectedSpend = spent.multiply(multiplier).setScale(2, RoundingMode.HALF_UP);
        BigDecimal projectedIncome = income.multiply(multiplier).setScale(2, RoundingMode.HALF_UP);
        BigDecimal projectedNet = projectedIncome.subtract(projectedSpend);
        return new MonthEndForecastResponse(month, spent, projectedSpend, projectedIncome, projectedNet, risk(projectedSpend, income));
    }

    @Transactional(readOnly = true)
    public List<CategoryForecastResponse> categoryForecast(User user) {
        YearMonth month = YearMonth.now();
        LocalDate today = LocalDate.now();
        Map<String, BigDecimal> current = totalsByCategory(user.getId(), month.atDay(1), today);
        Map<String, BigDecimal> trailing = totalsByCategory(user.getId(), month.minusMonths(3).atDay(1), month.minusMonths(1).atEndOfMonth());
        BigDecimal multiplier = BigDecimal.valueOf(month.lengthOfMonth()).divide(BigDecimal.valueOf(today.getDayOfMonth()), 4, RoundingMode.HALF_UP);
        return current.entrySet().stream()
                .map(entry -> {
                    BigDecimal projected = entry.getValue().multiply(multiplier).setScale(2, RoundingMode.HALF_UP);
                    BigDecimal average = trailing.getOrDefault(entry.getKey(), BigDecimal.ZERO).divide(BigDecimal.valueOf(3), 2, RoundingMode.HALF_UP);
                    return new CategoryForecastResponse(entry.getKey(), entry.getValue(), projected, average, risk(projected, average));
                })
                .sorted(Comparator.comparing(CategoryForecastResponse::projectedSpend).reversed())
                .toList();
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

    private BigDecimal total(List<Transaction> transactions, TransactionType type) {
        return transactions.stream().filter(transaction -> transaction.getType() == type).map(Transaction::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String risk(BigDecimal projected, BigDecimal baseline) {
        if (baseline.compareTo(BigDecimal.ZERO) <= 0) return projected.compareTo(BigDecimal.valueOf(500)) > 0 ? "WATCH" : "LOW";
        if (projected.compareTo(baseline.multiply(BigDecimal.valueOf(1.25))) > 0) return "HIGH";
        if (projected.compareTo(baseline.multiply(BigDecimal.valueOf(1.1))) > 0) return "WATCH";
        return "LOW";
    }
}
