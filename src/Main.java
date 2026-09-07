import  java.util.Scanner;
public class Main {
    public static void main(String[] args) {
        Data data = new Data();
        Scanner scanner = new Scanner (System.in);
        System.out.println("Welcome to BCash");
        int tries = 0;
        do {
            try {
                System.out.println("Enter your mobile number.");
                String number = scanner.nextLine();
                data.searchAccount(number);

                System.out.println("Enter your MPIN");
                String mPin = scanner.nextLine();

                User account = data.log(number, mPin);
                userSession(data, account);

                break;
            } catch (RuntimeException exception) {
                System.out.println(exception.getMessage());
                tries++;
            }

        } while (tries < 3);

        if (tries == 3) System.out.println("3rd attempt failed. Exiting...");
    }

    private static void userSession(Data data, User logInAccount){
        Payment payment = new Payment(data);
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("Welcome, " + logInAccount.getName());
            System.out.println("Balance: " + logInAccount.getBalance());
            System.out.println("1 - Transfer");
            System.out.println("2 - Cash In");
            System.out.println("3 - View Transaction History");
            System.out.println("Press any other key to log out.");
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    payment.send(logInAccount);
                    break;
                case 2:
                    payment.cashIn(logInAccount);
                    break;
                case 3:
                    payment.printTransactionRecords(logInAccount);
                    break;
                default:
                    System.out.println("Logging out...");
                    return;
            }
        }
    }
}