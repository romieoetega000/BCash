import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

public class Payment {
    private Data data;

    Payment (Data data) {
        this.data = data;
    }

    public void send(User logged) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter the recipient's mobile number.");
        String numberOther = scanner.nextLine();
        if(numberOther.equals(logged.getNumber())) {
            System.out.println("Invalid Number");
            return;
        }

        try {
        data.searchAccount(numberOther);
        } catch (InputMismatchException exception) {
            System.out.println(exception.getMessage());
            return;
        } catch (RuntimeException exception) {
            System.out.println(exception.getMessage());
            return;
        }

        System.out.println("Enter the amount");
        double amount = scanner.nextDouble();
        scanner.nextLine();

        if (amount <= 0) {
            System.out.println("Invalid amount.");
            return;
        }
        if (logged.getBalance() < amount) {
            System.out.println("Insufficient balance.");
            return;
        }

        try {
            logged.setBalance(data.transaction(logged, numberOther, amount));
            System.out.println("Send successful!");
        } catch (RuntimeException exception) {
            System.out.println(exception.getMessage());
        }

    }

    public void cashIn(User logged) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Cash In");
        System.out.println("Enter the amount");
        double amount = scanner.nextDouble();
        scanner.nextLine();

        if (amount <= 0) {
            System.out.println("Invalid amount.");
            return;
        }

        try {
            logged.setBalance(data.transaction(logged, amount));
            System.out.println("Cash in successful!");
        } catch (RuntimeException exception) {
            System.out.println(exception.getMessage());
        }

    }

    public void printTransactionRecords(User logged) {
        List<String> transactions = null;
        try {
            transactions = data.retrieveTransactionRecords(logged.getName());
        } catch (RuntimeException exception) {
            System.out.println(exception.getMessage());
            return;
        }
        if (transactions.isEmpty()) {
            System.out.println("No recorded transactions.");
            return;
        }
        for(String transaction : transactions) {
            System.out.println(transaction);
            System.out.println();
        }
    }
}
