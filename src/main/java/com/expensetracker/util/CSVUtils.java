package com.expensetracker.util;

import com.expensetracker.model.Expense;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.time.Duration;

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

    /**
     * General-purpose CSV uploader to S3 with automatic timestamp.
     *
     * @param s3           AWS S3 client
     * @param bucketName   S3 bucket name
     * @param folderPath   e.g. user123/exports
     * @param baseFileName e.g. expenses.csv (timestamp will be appended)
     * @param csvData      CSV content
     * @return             Full S3 key (folder/filename)
     */
    public static String uploadCSVToS3(S3Client s3, String bucketName, String folderPath, String baseFileName, String csvData) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String finalFileName = baseFileName.replace(".csv", "") + "_" + timestamp + ".csv";
        String s3Key = folderPath + "/" + finalFileName;

        try {
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .contentType("text/csv")
                    .build();

            s3.putObject(putRequest, RequestBody.fromString(csvData));
            System.out.println("✅ CSV uploaded to S3: " + s3Key);
            return s3Key;
        } catch (Exception e) {
            System.err.println("❌ Failed to upload CSV: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public static String exportCSV(S3Client s3, S3Presigner presigner, String bucketName, String csvData, String userId) {
        String folderPath = userId + "/exports";
        String baseFileName = "expenses.csv";
        String s3Key = uploadCSVToS3(s3, bucketName, folderPath, baseFileName, csvData);
    
        if (s3Key == null) return null;
    
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();
    
            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .getObjectRequest(getObjectRequest)
                    .signatureDuration(Duration.ofMinutes(2)) // 👈 valid for 2 minutes
                    .build();
    
            String presignedUrl = presigner.presignGetObject(presignRequest).url().toString();
            System.out.println("🔗 Presigned URL generated: " + presignedUrl);
            return s3Key;
    
        } catch (Exception e) {
            System.err.println("❌ Failed to generate presigned URL: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public static List<Expense> parseCSV(InputStream csvStream) throws IOException {
        List<Expense> expenses = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(csvStream))) {
            String line;
            boolean isHeader = true;

            while ((line = reader.readLine()) != null) {
                if (isHeader) {
                    isHeader = false;
                    continue;
                }

                String[] tokens = line.split(",");
                if (tokens.length == 5) {
                    String id = tokens[0].trim();
                    String title = tokens[1].trim();
                    double amount = Double.parseDouble(tokens[2].trim());
                    String category = tokens[3].trim();
                    String date = tokens[4].trim();

                    expenses.add(new Expense(id, title, amount, category, date));
                }
            }
        }
        return expenses;
    }

    public static String generateS3KeyUpload(String userId) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String newFileName = "expenses_" + timestamp + ".csv";
        return String.format("%s/uploads/%s", userId, newFileName); // e.g., "user123/uploads/expenses_1712421923.csv"
    }
}
