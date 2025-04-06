package com.expensetracker;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.util.List;
import java.util.Scanner;
import java.util.UUID;

import com.expensetracker.s3.S3Service;
import com.expensetracker.util.CSVUtils;
import com.expensetracker.model.Expense;
import com.expensetracker.ec2.EC2Launcher;
import software.amazon.awssdk.regions.Region;
import com.expensetracker.lambda.LambdaScheduler;
import software.amazon.awssdk.core.ResponseInputStream;

public class ExpenseTrackerApp {
    
    public static final String BUCKET_NAME = System.getenv("BUCKET_NAME");

    public static void main(String[] args) {
        DynamoDbClient client = DynamoDbClient.builder()
                .region(Region.of(System.getenv("AWS_REGION")))
                .build();

        S3Client s3 = S3Client.builder()
                .region(Region.of(System.getenv("AWS_REGION")))
                .build();

        S3Presigner presigner = S3Presigner.create();
        System.out.println(BUCKET_NAME);
        S3Service.createBucketIfNotExists(s3, BUCKET_NAME);
        LambdaScheduler lambdaScheduler = new LambdaScheduler();

        ExpenseService service = new ExpenseService(client);
        Scanner scanner = new Scanner(System.in);

        // TODO: Fix This ones You have The mutipl user funtion 
        System.out.print("Enter user ID: ");
        String userId = scanner.nextLine();

        while (true) {
            System.out.println("\n📊 Expense Tracker");
            System.out.println("1. Add Expense");
            System.out.println("2. Get Expense");
            System.out.println("3. Delete Expense");
            System.out.println("4. List All");
            System.out.println("5. Get Total Expense");
            System.out.println("6. Export to CSV and Upload to S3");
            System.out.println("7. Upload CSV and Import Expenses");
            System.out.println("8. Launch EC2 Instance (Very Dangerous \"DO NOT USE\")");
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
                        // TODO: Use this URL in UI so usre can Download the data
                        String s3Key = CSVUtils.exportCSV(s3, presigner, BUCKET_NAME, csvData, userId);
                        System.out.println(s3Key);

                        lambdaScheduler.scheduleFileDeletion(List.of(s3Key));

                        System.out.println("🕒 Waiting for file to be deleted from S3...");

                        boolean fileExists = true;
                        int attempts = 0;
                        int maxAttempts = 20; // ~10 seconds if we sleep 500ms each
                        while (fileExists && attempts < maxAttempts) {
                            try {
                                Thread.sleep(5000); // half a second
                                s3.headObject(b -> b.bucket(BUCKET_NAME).key(s3Key));
                                System.out.println("🔍 File still exists...");
                            } catch (software.amazon.awssdk.services.s3.model.NoSuchKeyException ex) {
                                System.out.println("✅ File successfully deleted from S3.");
                                fileExists = false;
                                break;
                            } catch (Exception ex) {
                                System.out.println("⚠️ Unexpected error while checking for file: " + ex.getMessage());
                            }
                            attempts++;
                        }
    
                        if (fileExists) {
                            System.out.println("⏰ Gave up waiting. File still exists after timeout.");
                        }
                    }

                    break;

                case 7:
                    // TODO: make some chnage here cause this will not work 
                    String s3Key = CSVUtils.generateS3KeyUpload(userId);
                
                    try {
                        GetObjectRequest getRequest = GetObjectRequest.builder()
                                .bucket(BUCKET_NAME)
                                .key(s3Key)
                                .build();
                
                        ResponseInputStream<GetObjectResponse> s3Object = s3.getObject(getRequest);
                        List<Expense> csvExpenses = CSVUtils.parseCSV(s3Object);
                
                        for (Expense exp : csvExpenses) {
                            service.addExpense(exp);
                        }
                
                        System.out.printf("✅ Imported %d expenses from S3 (%s) to DynamoDB%n", csvExpenses.size(), s3Key);
                        lambdaScheduler.scheduleFileDeletion(List.of(s3Key));
                
                    } catch (Exception ex) {
                        System.out.println("❌ Error during import: " + ex.getMessage());
                    }
                    break; 

                case 8:
                    EC2Launcher.launchInstance();
                    break;
                    
    
                case 9:
                    client.close();
                    s3.close();
                    scanner.close();
                    presigner.close();
                    System.exit(0);
                    break;
                
                default:
                    System.out.println("❌ Invalid option.");
                    break;
            }
        }
    }
}
