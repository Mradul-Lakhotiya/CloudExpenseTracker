# CloudExpenseTracker

A simple AWS-powered expense tracker built in Java.

---

## 🧑‍💻 About This Project

This project is a basic expense tracking application created using Amazon Web Services (AWS) and Java.  
It was made as an early learning exercise to practice using AWS SDKs and cloud concepts.

The code is beginner-friendly and demonstrates how you can store, retrieve, and manage simple expense records using AWS services.

---

## 🚀 Features

- **Add Expense:** Enter title, amount, category, and date for your expenses.
- **View Expense:** Retrieve a specific expense by its unique ID.
- **Delete Expense:** Remove expense records by ID.
- **List All Expenses:** Print a table of all expenses stored in the database.
- **Get Total Expense:** Calculate the sum of all your expenses.
- **Export to CSV & Upload to S3:** Generate a CSV file of expenses and upload it to Amazon S3 for download.
- **Import Expenses from S3:** Upload a CSV to S3 and import the expenses into DynamoDB.
- **Automatic File Deletion:** Uses an AWS Lambda function to automatically delete exported files from S3 after some time.
- **EC2 Launch (Not for Production):** Experimental option to launch an EC2 instance (for learning only; not recommended).

---

## 🛠️ AWS Services Used

- **DynamoDB:** Stores all expense records.
- **Amazon S3:** Stores CSV exports/imports for expenses.
- **AWS Lambda:** Automatically deletes files from S3 after export/import.
- **(Optional/Experimental) EC2:** Can launch an instance from the app menu for demonstration.

---

## 📦 How It Works

1. The app prompts for a user ID at startup.
2. Main menu offers options to add, view, delete, list, calculate, export, and import expenses.
3. Expenses are stored in DynamoDB with a unique ID, title, amount, category, and date.
4. Exported expenses are saved as CSV files in S3; Lambda is used to clean up files.
5. Import reads CSV files from S3 and saves the records back to DynamoDB.

---

## ⚠️ Notes

- This project was made during my early days learning AWS and Java. Code may not be optimal or production-ready.
- The EC2 launch feature is for experimentation only and should not be used in real environments.
- AWS credentials, region, and S3 bucket name must be configured via environment variables.

---

## 📚 Usage

- Clone this repository and set up AWS credentials.
- Run the main class: `ExpenseTrackerApp.java` (in `src/main/java/com/expensetracker/`).
- Follow menu instructions in the terminal.

---

_Made by [Mradul-Lakhotiya](https://github.com/Mradul-Lakhotiya) for practice and learning._