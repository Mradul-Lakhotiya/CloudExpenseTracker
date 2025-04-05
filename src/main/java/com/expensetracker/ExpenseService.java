package com.expensetracker;

import com.expensetracker.model.Expense;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.*;

public class ExpenseService {
    private final String tableName = "ExpenseTest"; // change if needed
    private final DynamoDbClient ddb;

    public ExpenseService(DynamoDbClient client) {
        this.ddb = client;
    }

    // ➕ Add Expense
    public void addExpense(Expense expense) {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("id", AttributeValue.fromS(expense.getId()));
        item.put("title", AttributeValue.fromS(expense.getTitle()));
        item.put("amount", AttributeValue.fromN(String.valueOf(expense.getAmount())));
        item.put("category", AttributeValue.fromS(expense.getCategory()));
        item.put("date", AttributeValue.fromS(expense.getDate()));

        PutItemRequest request = PutItemRequest.builder()
                .tableName(tableName)
                .item(item)
                .build();

        ddb.putItem(request);
        System.out.println("✅ Expense added: " + expense.getId());
    }

    // 📄 Get Expense by ID
    public Expense getExpense(String id) {
        GetItemRequest request = GetItemRequest.builder()
                .tableName(tableName)
                .key(Map.of("id", AttributeValue.fromS(id)))
                .build();

        Map<String, AttributeValue> returnedItem = ddb.getItem(request).item();
        if (returnedItem == null || returnedItem.isEmpty()) {
            System.out.println("❌ Expense not found.");
            return null;
        }

        return new Expense(
                returnedItem.get("id").s(),
                returnedItem.get("title").s(),
                Double.parseDouble(returnedItem.get("amount").n()),
                returnedItem.get("category").s(),
                returnedItem.get("date").s()
        );
    }

    // 🗑️ Delete Expense
    public void deleteExpense(String id) {
        DeleteItemRequest request = DeleteItemRequest.builder()
                .tableName(tableName)
                .key(Map.of("id", AttributeValue.fromS(id)))
                .build();

        ddb.deleteItem(request);
        System.out.println("🗑️ Expense deleted: " + id);
    }

    // 📋 List all Expenses (scan table)

    public void printExpenseTable(List<Expense> expenses) {
        String format = "| %-26s | %-8s | %-10s | %-10s | %-36s |%n";
        double total = 0;
    
        System.out.println("┌────────────────────────────┬──────────┬────────────┬────────────┬──────────────────────────────────────┐");
        System.out.format("| %-26s | %-8s | %-10s | %-10s | %-36s |%n", "Title", "Amount", "Category", "Date", "ID");
        System.out.println("├────────────────────────────┼──────────┼────────────┼────────────┼──────────────────────────────────────┤");
    
        for (Expense exp : expenses) {
            total += exp.getAmount();
            System.out.format(format, exp.getTitle(), "₹" + exp.getAmount(), exp.getCategory(), exp.getDate(), exp.getId());
        }
    
        System.out.println("└────────────────────────────┴──────────┴────────────┴────────────┴──────────────────────────────────────┘");
        System.out.printf("💰 Total Expenses: ₹%.2f%n", total);
    }

    public List<Expense> scanExpenses() {
        ScanRequest scanRequest = ScanRequest.builder()
            .tableName(tableName)
            .build();
    
        ScanResponse response = ddb.scan(scanRequest);
    
        List<Expense> expenses = new ArrayList<>();
        for (Map<String, AttributeValue> item : response.items()) {
            expenses.add(Expense.from(item));
        }
        return expenses;
    }
}
