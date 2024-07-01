import java.sql.*;
import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class libServer {
    private static final int PORT = 3000;
    private static ExecutorService threadPool = Executors.newCachedThreadPool();
    private static final String DB_URL = "jdbc:mysql://localhost:3306/library";
    private static final String DB_PASSWORD = "";
    private static final String DB_USER = "root";

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket socket = serverSocket.accept();
                threadPool.submit(new ClientHandler(socket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            threadPool.shutdown();
        }
    }

    private static class ClientHandler implements Runnable {
        private final Socket clientSocket;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

                String request;
                while ((request = in.readLine()) != null) {
                    System.out.println("Received: " + request);
                    String response = handleRequest(request);
                    out.println(response);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private String handleRequest(String request) {
            String[] parts =  request.split(" ", 2);
            String command = parts[0].toLowerCase();
            String argument = parts.length > 1 ? parts[1] : "";
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                    switch (command) {
                        case "list":
                            return listBook(connection);
                        case "borrow":
                            return borrowBook(connection, Integer.parseInt(argument));
                        case "return":
                            return returnBook(connection, Integer.parseInt(argument));
                        default:
                            return "Invalid command";
                    }
                }
            } catch (ClassNotFoundException | SQLException e) {
                e.printStackTrace();
                return "Database Error";
            }
        }

        private String listBook(Connection connection) throws SQLException {
            StringBuilder response = new StringBuilder();
            String query = "SELECT * FROM books";
            try (Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(query)) {
                while (resultSet.next()) {
                    response.append("ID: ").append(resultSet.getInt("id"))
                            .append(", Title: ").append(resultSet.getString("title"))
                            .append(", Author: ").append(resultSet.getString("author"))
                            .append(", Borrowed").append(resultSet.getBoolean("is_borrowed") ? "Yes" : "No")
                            .append("\n");
                }
            }
            return response.toString();
        }

        private String borrowBook(Connection connection, int bookId) throws SQLException {
            String query = "UPDATE books SET is_borrowed = TRUE WHERE id = ? AND is_borrowed = FALSE";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setInt(1, bookId);
                int rowsUpdated = preparedStatement.executeUpdate();
                return rowsUpdated > 0 ? "Borrowed Successfully" : "Already Borrowed or does not exist";
            }
        }

        private String returnBook(Connection conn, int bookId) throws SQLException {
            String query = "UPDATE books SET is_borrowed = FALSE WHERE id = ? AND is_borrowed = TRUE";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, bookId);
                int rowsUpdated = stmt.executeUpdate();
                return rowsUpdated > 0 ? "Returned successfully" : "Book is not borrowed or does not exist";
            }
        }
    }
}