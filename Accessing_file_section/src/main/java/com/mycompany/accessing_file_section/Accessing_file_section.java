package com.mycompany.accessing_file_section;

import java.io.*;
import java.util.*;
import javax.swing.*;

public class Accessing_file_section {

    public static void main(String[] args) {
        System.out.println("ABC Banking System");
        ABCBankSystem bankSystem = new ABCBankSystem();
        bankSystem.start();
    }
}

// Main Banking System Class
class ABCBankSystem {
    private ArrayList<BankAccount> accounts = new ArrayList<>();
    private File accountsFile = new File("accounts.txt");
    private File transactionLogFile = new File("transactions.log");

    public void start() {
        loadAccounts();
        while (true) {
            String menu = "1. Create Account\n" +
                          "2. View Account Details\n" +
                          "3. Deposit/Withdraw\n" +
                          "4. Transfer Funds\n" +
                          "5. View Transaction History\n" +
                          "6. Generate and View Reports\n" +
                          "7. Exit";
            String choice = JOptionPane.showInputDialog(menu);

            if (choice == null || choice.equals("7")) break;

            try {
                switch (choice) {
                    case "1":
                        createAccount();
                        break;
                    case "2":
                        viewAccountDetails();
                        break;
                    case "3":
                        depositOrWithdraw();
                        break;
                    case "4":
                        transferFunds();
                        break;
                    case "5":
                        viewTransactionHistory();
                        break;
                    case "6":
                        generateAndViewReports();
                        break;
                    default:
                        JOptionPane.showMessageDialog(null, "Invalid choice.");
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
            }
        }
        saveAccounts();
    }

   private void loadAccounts() {
    try (BufferedReader reader = new BufferedReader(new FileReader("accounts.txt"))) {
        String line;
        while ((line = reader.readLine()) != null) {
            String[] data = line.split(",");
            int accountNumber = Integer.parseInt(data[0]);
            String accountType = data[1];
            double balance = Double.parseDouble(data[2]);
            long creationTime = Long.parseLong(data[3]);

            BankAccount account;
            if (accountType.equalsIgnoreCase("Savings")) {
                account = new SavingsAccount(accountNumber, balance, 5.0);
            } else {
                account = new CurrentAccount(accountNumber, balance, 500.0);
            }
            account.setCreationDate(new Date(creationTime));
            accounts.add(account); // Add to in-memory list
        }
    } catch (IOException e) {
        System.out.println("Error loading accounts: " + e.getMessage());
    }
}


    private void saveAccounts() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(accountsFile))) {
            for (BankAccount account : accounts) {
                writer.println(account.getAccountNumber() + "," +
                               account.getAccountType() + "," +
                               account.getBalance() + "," +
                               account.getCreationDate().getTime());
            }
        } catch (IOException e) {
            System.out.println("Error saving accounts: " + e.getMessage());
        }
    }

    private void createAccount() {
    String accountType = JOptionPane.showInputDialog("Enter account type (Savings/Current):");
    if (accountType == null || (!accountType.equalsIgnoreCase("Savings") && !accountType.equalsIgnoreCase("Current"))) {
        JOptionPane.showMessageDialog(null, "Invalid account type.");
        return;
    }

    double initialBalance = Double.parseDouble(JOptionPane.showInputDialog("Enter initial balance:"));
    int accountNumber = new Random().nextInt(9000) + 1000; // Generate a 4-digit account number

    BankAccount newAccount;
    if (accountType.equalsIgnoreCase("Savings")) {
        newAccount = new SavingsAccount(accountNumber, initialBalance, 5.0);
    } else {
        newAccount = new CurrentAccount(accountNumber, initialBalance, 500.0);
    }

    accounts.add(newAccount); // Add to in-memory list

    // Write the account to the file
    try (PrintWriter writer = new PrintWriter(new FileWriter("accounts.txt", true))) {
        writer.println(accountNumber + "," + accountType + "," + initialBalance + "," + new Date().getTime());
    } catch (IOException e) {
        JOptionPane.showMessageDialog(null, "Error saving account to file: " + e.getMessage());
    }

    JOptionPane.showMessageDialog(null, "Account created successfully. Account Number: " + accountNumber);
}


    private void viewAccountDetails() {
        int accountNumber = Integer.parseInt(JOptionPane.showInputDialog("Enter account number:"));
        for (BankAccount account : accounts) {
            if (account.getAccountNumber() == accountNumber) {
                JOptionPane.showMessageDialog(null, account.toString());
                return;
            }
        }
        JOptionPane.showMessageDialog(null, "Account not found.");
    }

    private void depositOrWithdraw() {
    int accountNumber = Integer.parseInt(JOptionPane.showInputDialog("Enter account number:"));
    BankAccount account = findAccount(accountNumber);

    if (account == null) {
        JOptionPane.showMessageDialog(null, "Account not found.");
        return;
    }

    String operation = JOptionPane.showInputDialog("Enter operation (Deposit/Withdraw):");
    double amount = Double.parseDouble(JOptionPane.showInputDialog("Enter amount:"));

    try {
        if (operation.equalsIgnoreCase("Deposit")) {
            account.deposit(amount);
            logTransaction(accountNumber, "Deposit", amount, "Success");
            JOptionPane.showMessageDialog(null, "Deposit successful. New Balance: " + account.getBalance());
        } else if (operation.equalsIgnoreCase("Withdraw")) {
            account.withdraw(amount);
            logTransaction(accountNumber, "Withdraw", amount, "Success");
            JOptionPane.showMessageDialog(null, "Withdrawal successful. New Balance: " + account.getBalance());
        } else {
            JOptionPane.showMessageDialog(null, "Invalid operation.");
        }
    } catch (Exception e) {
        logTransaction(accountNumber, operation, amount, "Failed: " + e.getMessage());
        JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
    }
}


    private void transferFunds() {
    int sourceAccountNumber = Integer.parseInt(JOptionPane.showInputDialog("Enter source account number:"));
    int targetAccountNumber = Integer.parseInt(JOptionPane.showInputDialog("Enter target account number:"));
    double amount = Double.parseDouble(JOptionPane.showInputDialog("Enter amount:"));

    BankAccount sourceAccount = findAccount(sourceAccountNumber);
    BankAccount targetAccount = findAccount(targetAccountNumber);

    if (sourceAccount == null || targetAccount == null) {
        JOptionPane.showMessageDialog(null, "Invalid account number(s).");
        return;
    }

    try {
        sourceAccount.withdraw(amount);
        targetAccount.deposit(amount);
        logTransaction(sourceAccountNumber, "Transfer Out", amount, "Success");
        logTransaction(targetAccountNumber, "Transfer In", amount, "Success");
        JOptionPane.showMessageDialog(null, "Transfer successful.");
    } catch (Exception e) {
        logTransaction(sourceAccountNumber, "Transfer Out", amount, "Failed: " + e.getMessage());
        JOptionPane.showMessageDialog(null, "Transfer failed: " + e.getMessage());
    }
}


   private void viewTransactionHistory() {
    try (BufferedReader reader = new BufferedReader(new FileReader(transactionLogFile))) {
        StringBuilder history = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            history.append(line).append("\n");
        }
        JOptionPane.showMessageDialog(null, history.toString().isEmpty() ? 
                                      "No transactions found." : history.toString());
    } catch (IOException e) {
        JOptionPane.showMessageDialog(null, "Error reading transaction history: " + e.getMessage());
    }
}


    private void generateAndViewReports() {
    try (PrintWriter writer = new PrintWriter(new FileWriter("summary_report.txt"))) {
        for (BankAccount account : accounts) {
            writer.println(account.toString());
        }
        JOptionPane.showMessageDialog(null, "Report generated and saved to summary_report.txt.");
    } catch (IOException e) {
        JOptionPane.showMessageDialog(null, "Error generating report: " + e.getMessage());
    }
}


    private void logTransaction(int accountNumber, String type, double amount, String status) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(transactionLogFile, true))) {
            writer.println("Account: " + accountNumber + ", Type: " + type +
                           ", Amount: " + amount + ", Status: " + status +
                           ", Date: " + new Date());
        } catch (IOException e) {
            System.out.println("Error logging transaction: " + e.getMessage());
        }
    }
    
    private BankAccount findAccount(int accountNumber) {
    for (BankAccount account : accounts) {
        if (account.getAccountNumber() == accountNumber) {
            return account;
        }
    }
    return null; // Return null if the account is not found
}

}

// Superclass BankAccount
abstract class BankAccount {
    private int accountNumber;
    private String accountType;
    private double balance;
    private Date creationDate;

    public BankAccount(int accountNumber, String accountType, double balance) {
        this.accountNumber = accountNumber;
        this.accountType = accountType;
        this.balance = balance;
        this.creationDate = new Date();
    }

    public int getAccountNumber() {
        return accountNumber;
    }

    public String getAccountType() {
        return accountType;
    }

    public double getBalance() {
        return balance;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date date) {
        this.creationDate = date;
    }

    public void deposit(double amount) {
        balance += amount;
    }

    public void withdraw(double amount) throws Exception {
        if (balance < amount) {
            throw new Exception("Insufficient balance.");
        }
        balance -= amount;
    }

    @Override
    public String toString() {
        return "Account Number: " + accountNumber + ", Type: " + accountType +
               ", Balance: " + balance + ", Created: " + creationDate;
    }
}

// Subclass SavingsAccount
class SavingsAccount extends BankAccount {
    private double interestRate;

    public SavingsAccount(int accountNumber, double balance, double interestRate) {
        super(accountNumber, "Savings", balance);
        this.interestRate = interestRate;
    }
}

// Subclass CurrentAccount
class CurrentAccount extends BankAccount {
    private double overdraftLimit;

    public CurrentAccount(int accountNumber, double balance, double overdraftLimit) {
        super(accountNumber, "Current", balance);
        this.overdraftLimit = overdraftLimit;
    }

    @Override
    public void withdraw(double amount) throws Exception {
        if (getBalance() + overdraftLimit < amount) {
            throw new Exception("Exceeds overdraft limit.");
        }
        super.withdraw(amount);
    }
}
