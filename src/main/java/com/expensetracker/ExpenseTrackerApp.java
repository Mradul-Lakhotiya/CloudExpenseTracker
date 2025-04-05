package com.expensetracker;

import com.expensetracker.ec2.EC2Launcher;
import com.expensetracker.model.Expense;
import software.amazon.awssdk.regions.Region;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import com.expensetracker.s3.S3Service;
import com.expensetracker.util.CSVUtils;
import software.amazon.awssdk.services.s3.S3Client;

import java.util.List;
import java.util.Scanner;
import java.util.UUID;

public class ExpenseTrackerApp {

    public static void main(String[] args) {
        DynamoDbClient client = DynamoDbClient.builder()
                .region(Region.of(System.getenv("AWS_REGION")))
                .build();

        S3Client s3 = S3Client.builder()
                .region(Region.of(System.getenv("AWS_REGION")))
                .build();

        String bucketName = "aws-expense-tracker-armaan";
        S3Service.createBucketIfNotExists(s3, bucketName);

        ExpenseService service = new ExpenseService(client);
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\n📊 Expense Tracker");
            System.out.println("1. Add Expense");
            System.out.println("2. Get Expense");
            System.out.println("3. Delete Expense");
            System.out.println("4. List All");
            System.out.println("5. Get Total Expense");
            System.out.println("6. Export to CSV and Upload to S3");
            System.out.println("7. Upload CSV and Import Expenses");
            System.out.println("8. Launch EC2 Instance");
            System.out.println("9. Exit");
            System.out.print("Enter option: ");
            int option = scanner.nextInt();
            scanner.nextLine(); // consume newline
            List<Expense> expenses;
            String id;

            switch (option) {
                case 1:
                    id = UUID.randomUUID().toString();
                    System.out.print("Title: ");
                    String title = scanner.nextLine();
                    System.out.print("Amount: ");
                    double amount = scanner.nextDouble();
                    scanner.nextLine();
                    System.out.print("Category: ");
                    String category = scanner.nextLine();
                    System.out.print("Date (YYYY-MM-DD): ");
                    String date = scanner.nextLine();
                    Expense expense = new Expense(id, title, amount, category, date);
                    service.addExpense(expense);
                    break;

                case 2:
                    System.out.print("Enter expense ID: ");
                    id = scanner.nextLine();
                    Expense e = service.getExpense(id);
                    System.out.println(e);
                    break;

                case 3:
                    System.out.print("Enter ID to delete: ");
                    String idInput = scanner.nextLine();
                    id = idInput.startsWith("S=") ? idInput.substring(2) : idInput;
                    service.deleteExpense(id);
                    break;

                case 4:
                    expenses = service.scanExpenses();
                    if (expenses.isEmpty()) {
                        System.out.println("🚫 No expenses found.");
                    } 
                    else {
                        service.printExpenseTable(expenses);
                    }
                    break;

                case 5:
                    expenses = service.scanExpenses();

                    if (expenses.isEmpty()) {
                        System.out.println("🚫 No expenses to calculate.");
                    } 
                    else {
                        double total = expenses.stream().mapToDouble(e2 -> e2.getAmount()).sum();
                        System.out.printf("💰 Total Expenses: Rs %.2f%n", total);
                    }
                    break;

                case 6:
                    expenses = service.scanExpenses();
                    if (expenses.isEmpty()) {
                        System.out.println("🚫 No expenses to export.");
                    } 
                    else {
                        String csvData = CSVUtils.generateCSV(expenses);
                        String fileName = "expenses_" + System.currentTimeMillis() + ".csv";
                        CSVUtils.uploadCSVToS3(s3, bucketName, fileName, csvData);
                    }
                    break;
                
                case 7:
                    System.out.print("Enter full path to your local CSV file: ");
                    String filePath = scanner.nextLine();
                    System.out.print("Enter the S3 key (file name in S3): ");
                    String keyName = scanner.nextLine();

                    try {
                        // Upload to S3
                        S3Service.uploadLocalFileToS3(s3, bucketName, filePath, keyName);

                        // Parse and import to DynamoDB
                        List<Expense> csvExpenses = CSVUtils.parseCSV(filePath);
                        for (Expense exp : csvExpenses) {
                            service.addExpense(exp);
                        }

                        System.out.printf("✅ Imported %d expenses from CSV and saved to DynamoDB%n", csvExpenses.size());

                    } catch (Exception ex) {
                        System.out.println("❌ Error: " + ex.getMessage());
                    }
                    break;
                case 8:
                    EC2Launcher.launchInstance(); // call the method you just created
                    break;
                    
    
                case 9:
                    client.close();
                    s3.close();
                    scanner.close();
                    System.exit(0);
                    break;
                
                default:
                    System.out.println("❌ Invalid option.");
                    break;
            }
        }
    }
}
