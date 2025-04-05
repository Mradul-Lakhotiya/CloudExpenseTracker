package com.expensetracker.model;

import java.util.Map;

import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

public class Expense {
    private String id;
    private String title;
    private double amount;
    private String category;
    private String date;

    public Expense() {}

    public Expense(String id, String title, double amount, String category, String date) {
        this.id = id;
        this.title = title;
        this.amount = amount;
        this.category = category;
        this.date = date;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public static Expense from(Map<String, AttributeValue> item) {
        return new Expense(
            item.get("id").s(),
            item.get("title").s(),
            Double.parseDouble(item.get("amount").n()),
            item.get("category").s(),
            item.get("date").s()
        );
    }


    @Override
    public String toString() {
        return String.format("🧾 %s | ₹%.2f | %s | %s | id=%s", title, amount, category, date, id);
    }
}
