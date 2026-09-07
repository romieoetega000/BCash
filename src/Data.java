import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;

public class Data {
    private static final String URL = "jdbc:mysql://localhost:3306/bcash?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public void searchAccount(String number) {
        String query = "SELECT * FROM accounts WHERE phone_number = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = conn.prepareStatement(query)) {

            statement.setString(1, number);

            ResultSet resultSet = statement.executeQuery();
            if (!resultSet.next()) {
                throw new InputMismatchException("The number does not exist.");
            }

        } catch (SQLException e) {
            throw new RuntimeException("Database Error: Account does not exist.");
        }
    }

    public User log(String number, String mPin) {
        String query = "SELECT * FROM accounts WHERE phone_number = ? AND pin = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = conn.prepareStatement(query)) {

            statement.setString(1, number);
            statement.setString(2, mPin);

            User logInInfo = null;
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next())
                logInInfo = new User(resultSet.getLong("id"), resultSet.getString("name"), resultSet.getString("phone_number"), resultSet.getDouble("balance"));
            else
                throw new InputMismatchException("Incorrect MPIN");

            return logInInfo;
        } catch (SQLException exception) {
            throw new RuntimeException("Database Error: Log in failed.");
        }
    }

    public double transaction(User self, double amount) {
        String query = "UPDATE accounts SET balance = balance + ? WHERE phone_number = ?";
        String queryRecord = "INSERT INTO transactions (transaction_type, log_names, log_numbers, amount, time_generated) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = conn.prepareStatement(query);
             PreparedStatement statementRecord = conn.prepareStatement(queryRecord)) {

            statement.setDouble(1, amount);
            statement.setString(2, self.getNumber());
            statement.executeUpdate();

            statementRecord.setString(1, "CASH IN");
            statementRecord.setString(2, self.getName());
            statementRecord.setString(3, self.getNumber());
            statementRecord.setDouble(4, amount);
            statementRecord.setObject(5, LocalDateTime.now());
            statementRecord.executeUpdate();

            return self.getBalance() + amount;
        } catch (SQLException exception) {
            throw new RuntimeException("Database Error: Transaction failed.");
        }
    }

    public double transaction(User self, String numberOther, double amount) {
        String queryOther = "SELECT name, phone_number FROM accounts WHERE phone_number = ?";
        String updateOther = "UPDATE accounts SET balance = balance + ? WHERE phone_number = ?";
        String updateSelf = "UPDATE accounts SET balance = balance - ? WHERE phone_number = ?";
        String queryRecord = "INSERT INTO transactions (transaction_type, log_names, log_numbers, receiver_names, receiver_numbers, amount, time_generated) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statementOther = conn.prepareStatement(queryOther);
             PreparedStatement statementUpdateOther = conn.prepareStatement(updateOther);
             PreparedStatement statementSelf = conn.prepareStatement(updateSelf);
             PreparedStatement statementRecord = conn.prepareStatement(queryRecord)) {

            statementOther.setString(1, numberOther);

            String otherName = null;
            String otherNumber = null;
            try (ResultSet rsOther = statementOther.executeQuery()) {
                if (rsOther.next()) {
                    otherName = rsOther.getString("name");
                    otherNumber = rsOther.getString("phone_number");
                } else
                    throw new SQLException("Mobile number does not exist");
            }

            statementUpdateOther.setDouble(1, amount);
            statementUpdateOther.setString(2, numberOther);
            statementUpdateOther.executeUpdate();

            statementSelf.setDouble(1, amount);
            statementSelf.setString(2, self.getNumber());
            statementSelf.executeUpdate();

            statementRecord.setString(1, "TRANSFER");
            statementRecord.setString(2, self.getName());
            statementRecord.setString(3, self.getNumber());
            statementRecord.setString(4, otherName);
            statementRecord.setString(5, otherNumber);
            statementRecord.setDouble(6, amount);
            statementRecord.setObject(7, LocalDateTime.now());
            statementRecord.executeUpdate();

            return self.getBalance() - amount;
        } catch (SQLException exception) {
            throw new RuntimeException("Database Error: Transaction failed.");
        }
    }

    public List<String> retrieveTransactionRecords(String accountName) {
        String queryTransactions = "SELECT * FROM transactions WHERE log_names = ? OR receiver_names = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = conn.prepareStatement(queryTransactions)) {

            List<String> transactionsRecord = new ArrayList<>();
            statement.setString(1, accountName);
            statement.setString(2, accountName);

            try (ResultSet resultSetTransaction = statement.executeQuery()) {
                while (resultSetTransaction.next()) {
                    String transactionType = resultSetTransaction.getString("transaction_type") + "\n";
                    String logName = resultSetTransaction.getString("log_names") + "\n";
                    String logNumber = resultSetTransaction.getString("log_numbers") + "\n";
                    String receiverName = resultSetTransaction.getString("receiver_names") + "\n";
                    String receiverNumber = resultSetTransaction.getString("receiver_numbers") + "\n";
                    String amount = resultSetTransaction.getString("amount") + "\n";
                    String timeGenerated = resultSetTransaction.getString("time_generated");

                    if ("CASH IN".equalsIgnoreCase(transactionType.trim()))
                        transactionsRecord.add(transactionType + "+" + amount + timeGenerated);
                    else if(accountName.equals(logName.trim()))
                        transactionsRecord.add(transactionType + receiverName + receiverNumber + "-" + amount + timeGenerated);
                    else
                        transactionsRecord.add(transactionType + logName + logNumber + "+" + amount + timeGenerated);
                }
            }

            return transactionsRecord;
        } catch (SQLException exception) {
            throw new RuntimeException("Database Error: Records do not exist.");
        }
    }
}