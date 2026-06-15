import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Map;

public class ExpenseTrackerApp extends JFrame {
    private ExpenseManager manager;

    private JComboBox<String> categoryBox;
    private JTextField amountField;
    private JTextField dateField;
    private JTable expenseTable;
    private DefaultTableModel tableModel;
    private JTextArea summaryArea;

    public ExpenseTrackerApp() {
        manager = new ExpenseManager("expenses.csv");

        setTitle("Simple Expense Tracker");
        setSize(750, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        createInputPanel();
        createTablePanel();
        createSummaryPanel();

        loadTableData();
        updateSummary();

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void createInputPanel() {
        JPanel inputPanel = new JPanel(new GridLayout(2, 4, 8, 8));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Add Expense"));

        categoryBox = new JComboBox<>(new String[] { "Food", "Travel", "Shopping", "Bills", "Other" });
        amountField = new JTextField();
        dateField = new JTextField("YYYY-MM-DD");
        JButton addButton = new JButton("Add Expense");

        inputPanel.add(new JLabel("Category"));
        inputPanel.add(new JLabel("Amount"));
        inputPanel.add(new JLabel("Date (YYYY-MM-DD)"));
        inputPanel.add(new JLabel(""));

        inputPanel.add(categoryBox);
        inputPanel.add(amountField);
        inputPanel.add(dateField);
        inputPanel.add(addButton);

        addButton.addActionListener(e -> addExpense());

        add(inputPanel, BorderLayout.NORTH);
    }

    private void createTablePanel() {
        tableModel = new DefaultTableModel(new String[] { "Category", "Amount", "Date" }, 0);
        expenseTable = new JTable(tableModel);

        JScrollPane scrollPane = new JScrollPane(expenseTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Expenses"));

        add(scrollPane, BorderLayout.CENTER);
    }

    private void createSummaryPanel() {
        summaryArea = new JTextArea();
        summaryArea.setEditable(false);
        summaryArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        JScrollPane scrollPane = new JScrollPane(summaryArea);
        scrollPane.setPreferredSize(new Dimension(750, 180));
        scrollPane.setBorder(BorderFactory.createTitledBorder("Summary"));

        add(scrollPane, BorderLayout.SOUTH);
    }

    private void addExpense() {
        String category = categoryBox.getSelectedItem().toString();
        String amountText = amountField.getText().trim();
        String date = dateField.getText().trim();

        if (amountText.isEmpty() || date.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in amount and date.");
            return;
        }

        try {
            double amount = Double.parseDouble(amountText);

            if (amount <= 0) {
                JOptionPane.showMessageDialog(this, "Amount must be greater than 0.");
                return;
            }

            manager.addExpense(category, amount, date);

            amountField.setText("");

            loadTableData();
            updateSummary();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Amount must be a number.");
        }
    }

    private void loadTableData() {
        tableModel.setRowCount(0);

        for (Expense expense : manager.getExpenses()) {
            tableModel.addRow(new Object[] {
                    expense.getCategory(),
                    String.format("$%.2f", expense.getAmount()),
                    expense.getDate()
            });
        }
    }

    private void updateSummary() {
        StringBuilder summary = new StringBuilder();

        summary.append("Total Expense: $")
                .append(String.format("%.2f", manager.getTotalExpense()))
                .append("\n\n");

        summary.append("Highest Spend Category: ")
                .append(manager.getHighestCategory())
                .append("\n");

        summary.append("Lowest Spend Category: ")
                .append(manager.getLowestCategory())
                .append("\n\n");

        summary.append("Total By Category:\n");
        for (Map.Entry<String, Double> entry : manager.getTotalByCategory().entrySet()) {
            summary.append(entry.getKey())
                    .append(": $")
                    .append(String.format("%.2f", entry.getValue()))
                    .append("\n");
        }

        summary.append("\nExpense Trend By Date:\n");
        for (Map.Entry<String, Double> entry : manager.getExpenseTrend().entrySet()) {
            summary.append(entry.getKey())
                    .append(": $")
                    .append(String.format("%.2f", entry.getValue()))
                    .append("\n");
        }

        summaryArea.setText(summary.toString());
    }

    public static void main(String[] args) {
        new ExpenseTrackerApp();
    }
}