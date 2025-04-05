package com.expensetracker.util;

import com.expensetracker.model.Expense;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class CSVUtils {
    public static String generateCSV(List<Expense> expenses) {
        StringBuilder sb = new StringBuilder();
        sb.append("ID,Title,Amount,Category,Date\n");
        for (Expense e : expenses) {
            sb.append(String.format("%s,%s,%.2f,%s,%s\n",
                    e.getId(), e.getTitle(), e.getAmount(), e.getCategory(), e.getDate()));
        }
        return sb.toString();
    }

    public static void uploadCSVToS3(S3Client s3, String bucketName, String fileName, String csvData) {
        try {
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .contentType("text/csv")
                    .build();
    
            s3.putObject(putRequest, software.amazon.awssdk.core.sync.RequestBody.fromString(csvData));
            System.out.println("✅ CSV uploaded to S3: " + fileName);
        } catch (Exception e) {
            System.err.println("❌ Failed to upload CSV to S3: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static List<Expense> parseCSV(String filePath) throws IOException {
        List<Expense> expenses = new ArrayList<>();
        List<String> lines = Files.readAllLines(Paths.get(filePath));
        
        for (int i = 1; i < lines.size(); i++) {  // Skip header
            String[] tokens = lines.get(i).split(",");
            if (tokens.length == 5) {
                String id = tokens[0].trim();
                String title = tokens[1].trim();
                double amount = Double.parseDouble(tokens[2].trim());
                String category = tokens[3].trim();
                String date = tokens[4].trim();
                expenses.add(new Expense(id, title, amount, category, date));
            }
        }
        return expenses;
    }
}
