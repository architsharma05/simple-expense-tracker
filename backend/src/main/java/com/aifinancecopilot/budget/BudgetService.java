package com.aifinancecopilot.budget;

import com.aifinancecopilot.budget.dto.BudgetRequest;
import com.aifinancecopilot.budget.dto.BudgetResponse;
import com.aifinancecopilot.user.User;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BudgetService {
    private final BudgetRepository budgetRepository;

    public BudgetService(BudgetRepository budgetRepository) { this.budgetRepository = budgetRepository; }

    @Transactional(readOnly = true)
    public List<BudgetResponse> list(User user) {
        return budgetRepository.findByUserIdOrderByCategoryAsc(user.getId()).stream().map(BudgetResponse::from).toList();
    }

    @Transactional
    public BudgetResponse create(User user, BudgetRequest request) {
        String category = request.category().trim();
        if (budgetRepository.existsByUserIdAndCategoryIgnoreCase(user.getId(), category)) {
            throw new IllegalArgumentException("Budget already exists for category");
        }
        return BudgetResponse.from(budgetRepository.save(new Budget(user, category, request.monthlyLimit())));
    }

    @Transactional
    public BudgetResponse update(User user, UUID id, BudgetRequest request) {
        Budget budget = findOwned(user, id);
        budget.update(request.category().trim(), request.monthlyLimit());
        return BudgetResponse.from(budget);
    }

    @Transactional
    public void delete(User user, UUID id) {
        budgetRepository.delete(findOwned(user, id));
    }

    private Budget findOwned(User user, UUID id) {
        return budgetRepository.findByIdAndUserId(id, user.getId()).orElseThrow(() -> new IllegalArgumentException("Budget not found"));
    }
}
