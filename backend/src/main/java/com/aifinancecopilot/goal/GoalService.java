package com.aifinancecopilot.goal;

import com.aifinancecopilot.goal.dto.GoalRequest;
import com.aifinancecopilot.goal.dto.GoalResponse;
import com.aifinancecopilot.user.User;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GoalService {
    private final GoalRepository goalRepository;

    public GoalService(GoalRepository goalRepository) { this.goalRepository = goalRepository; }

    @Transactional(readOnly = true)
    public List<GoalResponse> list(User user) {
        return goalRepository.findByUserIdOrderByTargetDateAsc(user.getId()).stream().map(this::toResponse).toList();
    }

    @Transactional
    public GoalResponse create(User user, GoalRequest request) {
        Goal goal = goalRepository.save(new Goal(user, request.goalName().trim(), request.targetAmount(), request.targetDate()));
        return toResponse(goal);
    }

    @Transactional
    public GoalResponse update(User user, UUID id, GoalRequest request) {
        Goal goal = findOwned(user, id);
        goal.update(request.goalName().trim(), request.targetAmount(), request.targetDate());
        return toResponse(goal);
    }

    @Transactional
    public void delete(User user, UUID id) { goalRepository.delete(findOwned(user, id)); }

    private Goal findOwned(User user, UUID id) {
        return goalRepository.findByIdAndUserId(id, user.getId()).orElseThrow(() -> new IllegalArgumentException("Goal not found"));
    }

    private GoalResponse toResponse(Goal goal) {
        long days = Math.max(0, ChronoUnit.DAYS.between(LocalDate.now(), goal.getTargetDate()));
        BigDecimal months = BigDecimal.valueOf(Math.max(1, Math.ceil(days / 30.0)));
        BigDecimal monthly = goal.getTargetAmount().divide(months, 2, RoundingMode.HALF_UP);
        return GoalResponse.from(goal, days, monthly);
    }
}
