package com.aifinancecopilot.ai;

import com.aifinancecopilot.ai.dto.AiAnswerResponse;
import com.aifinancecopilot.ai.dto.AiInsightResponse;
import com.aifinancecopilot.ai.dto.CategorizeTransactionRequest;
import com.aifinancecopilot.ai.dto.CategorizeTransactionResponse;
import com.aifinancecopilot.budget.Budget;
import com.aifinancecopilot.budget.BudgetRepository;
import com.aifinancecopilot.goal.Goal;
import com.aifinancecopilot.goal.GoalRepository;
import com.aifinancecopilot.transaction.Transaction;
import com.aifinancecopilot.transaction.TransactionRepository;
import com.aifinancecopilot.transaction.TransactionType;
import com.aifinancecopilot.user.User;
import java.math.BigDecimal;
import java.time.Instant;
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
public class AiFinanceService {
    private static final String SYSTEM_INSTRUCTIONS = """
            You are AI Finance Copilot, a careful personal finance assistant. Use only the provided user-scoped finance data.
            Do not invent transactions. Give concise, practical insights with clear caveats. This is educational guidance, not financial advice.
            """;

    private final OpenAiClient openAiClient;
    private final TransactionRepository transactionRepository;
    private final BudgetRepository budgetRepository;
    private final GoalRepository goalRepository;
    private final AiInsightRepository aiInsightRepository;

    public AiFinanceService(OpenAiClient openAiClient, TransactionRepository transactionRepository,
                            BudgetRepository budgetRepository, GoalRepository goalRepository,
                            AiInsightRepository aiInsightRepository) {
        this.openAiClient = openAiClient;
        this.transactionRepository = transactionRepository;
        this.budgetRepository = budgetRepository;
        this.goalRepository = goalRepository;
        this.aiInsightRepository = aiInsightRepository;
    }

    @Transactional
    public AiAnswerResponse spendingSummary(User user) {
        YearMonth month = YearMonth.now();
        String prompt = "Summarize this month's spending, identify the top categories, and recommend 3 practical next steps.\n\n" + context(user, month.atDay(1), month.atEndOfMonth());
        return answerAndStore(user, prompt);
    }

    @Transactional
    public AiAnswerResponse budgetCoach(User user) {
        YearMonth month = YearMonth.now();
        String prompt = "Compare current month expenses against budgets. Identify overspending risk and recommend budget adjustments.\n\n" + context(user, month.atDay(1), month.atEndOfMonth());
        return answerAndStore(user, prompt);
    }

    @Transactional
    public AiAnswerResponse goalCoach(User user) {
        LocalDate to = LocalDate.now();
        LocalDate from = to.minusMonths(3).withDayOfMonth(1);
        String prompt = "Recommend a savings plan for the user goals. Identify monthly savings targets and spending categories to reduce.\n\n"
                + context(user, from, to) + "\n\nGoals:\n" + goals(user);
        return answerAndStore(user, prompt);
    }

    @Transactional
    public AiAnswerResponse chat(User user, String question) {
        LocalDate to = LocalDate.now();
        LocalDate from = to.minusMonths(6).withDayOfMonth(1);
        String prompt = "User question: " + question + "\n\nAnswer using this finance context.\n\n" + context(user, from, to);
        return answerAndStore(user, prompt);
    }

    @Transactional
    public CategorizeTransactionResponse categorize(User user, CategorizeTransactionRequest request) {
        String prompt = "Suggest one concise transaction category for this transaction. Return a short category and rationale.\n"
                + "Type: " + request.type() + "\nAmount: $" + request.amount() + "\nDescription: " + request.description() + "\n\n"
                + "Recent user context:\n" + context(user, LocalDate.now().minusMonths(3).withDayOfMonth(1), LocalDate.now());
        if (openAiClient.isConfigured()) {
            String answer = openAiClient.generate(SYSTEM_INSTRUCTIONS, prompt);
            String category = answer.lines().filter(line -> !line.isBlank()).findFirst().orElse("Uncategorized");
            return new CategorizeTransactionResponse(category.replace("Category:", "").trim(), 0.75, answer, true);
        }
        return fallbackCategory(request);
    }

    @Transactional(readOnly = true)
    public List<AiInsightResponse> insights(User user) {
        return aiInsightRepository.findTop20ByUserIdOrderByGeneratedAtDesc(user.getId()).stream()
                .map(AiInsightResponse::from)
                .toList();
    }

    private CategorizeTransactionResponse fallbackCategory(CategorizeTransactionRequest request) {
        String description = request.description().toLowerCase();
        String category = description.contains("uber") || description.contains("lyft") || description.contains("gas") ? "Transportation"
                : description.contains("grocery") || description.contains("restaurant") || description.contains("coffee") ? "Food"
                : description.contains("rent") || description.contains("mortgage") ? "Housing"
                : description.contains("netflix") || description.contains("spotify") || description.contains("subscription") ? "Subscriptions"
                : request.type() == TransactionType.INCOME ? "Income" : "General";
        return new CategorizeTransactionResponse(category, 0.55, "Rule-based preview categorization because OPENAI_API_KEY is not configured.", false);
    }

    private AiAnswerResponse answerAndStore(User user, String prompt) {
        boolean generatedByAi = openAiClient.isConfigured();
        String answer = generatedByAi ? openAiClient.generate(SYSTEM_INSTRUCTIONS, prompt) : fallbackAnswer(prompt);
        aiInsightRepository.save(new AiInsight(user, answer));
        return new AiAnswerResponse(answer, generatedByAi, Instant.now());
    }

    private String context(User user, LocalDate from, LocalDate to) {
        List<Transaction> transactions = transactions(user.getId(), from, to);
        List<Budget> budgets = budgetRepository.findByUserIdOrderByCategoryAsc(user.getId());
        BigDecimal income = total(transactions, TransactionType.INCOME);
        BigDecimal expenses = total(transactions, TransactionType.EXPENSE);
        Map<String, BigDecimal> byCategory = transactions.stream()
                .filter(transaction -> transaction.getType() == TransactionType.EXPENSE)
                .collect(Collectors.groupingBy(Transaction::getCategory, Collectors.reducing(BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add)));
        String categoryLines = byCategory.entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
                .map(entry -> "- " + entry.getKey() + ": $" + entry.getValue())
                .collect(Collectors.joining("\n"));
        String budgetLines = budgets.stream()
                .map(budget -> "- " + budget.getCategory() + ": monthly limit $" + budget.getMonthlyLimit())
                .collect(Collectors.joining("\n"));
        String recentLines = transactions.stream()
                .sorted(Comparator.comparing(Transaction::getTransactionDate).reversed())
                .limit(25)
                .map(transaction -> "- " + transaction.getTransactionDate() + " " + transaction.getType() + " " + transaction.getCategory() + " $" + transaction.getAmount() + " " + nullToEmpty(transaction.getDescription()))
                .collect(Collectors.joining("\n"));
        return "Period: " + from + " to " + to + "\n"
                + "Income: $" + income + "\n"
                + "Expenses: $" + expenses + "\n"
                + "Net: $" + income.subtract(expenses) + "\n\n"
                + "Expense categories:\n" + emptyIfBlank(categoryLines) + "\n\n"
                + "Budgets:\n" + emptyIfBlank(budgetLines) + "\n\n"
                + "Recent transactions:\n" + emptyIfBlank(recentLines);
    }

    private String goals(User user) {
        List<Goal> goals = goalRepository.findByUserIdOrderByTargetDateAsc(user.getId());
        String lines = goals.stream()
                .map(goal -> "- " + goal.getGoalName() + ": target $" + goal.getTargetAmount() + " by " + goal.getTargetDate())
                .collect(Collectors.joining("\n"));
        return emptyIfBlank(lines);
    }

    private List<Transaction> transactions(UUID userId, LocalDate from, LocalDate to) {
        Specification<Transaction> specification = (root, query, cb) -> cb.and(
                cb.equal(root.get("user").get("id"), userId),
                cb.greaterThanOrEqualTo(root.get("transactionDate"), from),
                cb.lessThanOrEqualTo(root.get("transactionDate"), to));
        return transactionRepository.findAll(specification);
    }

    private String fallbackAnswer(String prompt) {
        return "OpenAI is not configured, so this deterministic preview was generated from your scoped finance data.\n\n"
                + prompt.lines().filter(line -> line.startsWith("Income:") || line.startsWith("Expenses:") || line.startsWith("Net:") || line.startsWith("- "))
                .limit(12).collect(Collectors.joining("\n"))
                + "\n\nSet OPENAI_API_KEY to enable full AI analysis.";
    }

    private static BigDecimal total(List<Transaction> transactions, TransactionType type) {
        return transactions.stream().filter(transaction -> transaction.getType() == type).map(Transaction::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static String emptyIfBlank(String value) { return value == null || value.isBlank() ? "- None yet" : value; }
    private static String nullToEmpty(String value) { return value == null ? "" : value; }
}
