import java.io.*;
import java.util.ArrayList;

public class FileHandler {
    private String fileName;

    public FileHandler(String fileName) {
        this.fileName = fileName;
    }

    public ArrayList<Expense> loadExpenses() {
        ArrayList<Expense> expenses = new ArrayList<>();
        File file = new File(fileName);

        if (!file.exists()) {
            return expenses;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 3 && !parts[0].equalsIgnoreCase("Category")) {
                    String category = parts[0];
                    double amount = Double.parseDouble(parts[1]);
                    String date = parts[2];
                    expenses.add(new Expense(category, amount, date));
                }
            }
        } catch (Exception e) {
            System.out.println("Error reading file: " + e.getMessage());
        }

        return expenses;
    }

    public void saveExpenses(ArrayList<Expense> expenses) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {
            writer.println("Category,Amount,Date");
            for (Expense expense : expenses) {
                writer.println(expense.toCsvLine());
            }
        } catch (Exception e) {
            System.out.println("Error saving file: " + e.getMessage());
        }
    }
}
