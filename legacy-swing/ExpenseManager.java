import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

public class ExpenseManager {
    private ArrayList<Expense> expenses;
    private FileHandler fileHandler;

    public ExpenseManager(String fileName) {
        fileHandler = new FileHandler(fileName);
        expenses = fileHandler.loadExpenses();
    }

    public void addExpense(String category, double amount, String date) {
        expenses.add(new Expense(category, amount, date));
        fileHandler.saveExpenses(expenses);
    }

    public ArrayList<Expense> getExpenses() {
        return expenses;
    }

    public double getTotalExpense() {
        double total = 0;
        for (Expense expense : expenses) {
            total += expense.getAmount();
        }
        return total;
    }

    public Map<String, Double> getTotalByCategory() {
        Map<String, Double> totals = new TreeMap<>();
        for (Expense expense : expenses) {
            String category = expense.getCategory();
            double currentTotal = totals.getOrDefault(category, 0.0);
            totals.put(category, currentTotal + expense.getAmount());
        }
        return totals;
    }

    public Map<String, Double> getExpenseTrend() {
        Map<String, Double> trend = new TreeMap<>();
        for (Expense expense : expenses) {
            String date = expense.getDate();
            double currentTotal = trend.getOrDefault(date, 0.0);
            trend.put(date, currentTotal + expense.getAmount());
        }
        return trend;
    }

    public String getHighestCategory() {
        Map<String, Double> totals = getTotalByCategory();
        String highestCategory = "N/A";
        double highestAmount = -1;

        for (String category : totals.keySet()) {
            if (totals.get(category) > highestAmount) {
                highestAmount = totals.get(category);
                highestCategory = category;
            }
        }
        return highestCategory;
    }

    public String getLowestCategory() {
        Map<String, Double> totals = getTotalByCategory();
        String lowestCategory = "N/A";
        double lowestAmount = Double.MAX_VALUE;

        for (String category : totals.keySet()) {
            if (totals.get(category) < lowestAmount) {
                lowestAmount = totals.get(category);
                lowestCategory = category;
            }
        }
        return lowestCategory;
    }
}
