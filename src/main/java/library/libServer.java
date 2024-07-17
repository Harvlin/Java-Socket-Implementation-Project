package library;

import java.sql.*;
import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class libServer {
    private static final String SERVER_ADDRESS = "192.168.6.12";
    private static final int PORT = 3000;
    private static ExecutorService threadPool = Executors.newCachedThreadPool();
    private static final String DB_URL = "jdbc:mysql://localhost:3306/library";
    private static final String DB_PASSWORD = "";
    private static final String DB_USER = "root";

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT, 50, InetAddress.getByName(SERVER_ADDRESS))) {
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
        private String nickname;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                     PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                     Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {

                    if (!authenticateUser(connection, in, out)) {
                        return;
                    }

                    String request;
                    while ((request = in.readLine()) != null) {
                        System.out.println("Received: " + request);
                        String response = handleRequest(request, connection);
                        out.println(response);
                    }
                }
            } catch (ClassNotFoundException | IOException | SQLException e) {
                e.printStackTrace();
            }
        }

        private String handleRequest(String request, Connection connection) {
            String[] parts = request.split(" ", 2);
            String command = parts[0].toLowerCase();
            String argument = parts.length > 1 ? parts[1] : "";

            try {
                switch (command) {
                    case "list":
                        return listBook(connection);
                    case "borrow":
                        return borrowBook(connection, Integer.parseInt(argument));
                    case "return":
                        return returnBook(connection, Integer.parseInt(argument));
                    case "delete":
                        return deleteBook(connection, Integer.parseInt(argument));
                    case "add":
                        String[] details = argument.split(", ", 4);
                        return addBook(connection, Integer.parseInt(details[0]), details[1], details[2], Boolean.parseBoolean(details[3]));
                    case "admin_list":
                        return adminList(connection);
                    case "add_amin":
                        String[] detail = argument.split(", ", 2);
                        return addAdmin(connection, detail[0], detail[1]);
                    default:
                        return "Invalid command";
                }
            } catch (Exception e) {
                e.printStackTrace();
                return "Error";
            }
        }

        private boolean authenticateUser(Connection connection, BufferedReader in, PrintWriter out) throws SQLException, IOException {
            out.println("Enter your name: ");
            nickname = in.readLine();

            out.println("Admin or User: ");
            String response = in.readLine();

            if (response.equalsIgnoreCase("admin")) {
                String adminQuery = "SELECT * FROM admin WHERE name = ?";
                try (PreparedStatement preparedStatement = connection.prepareStatement(adminQuery)) {
                    preparedStatement.setString(1, nickname);
                    try (ResultSet resultSet = preparedStatement.executeQuery()) {
                        if (!resultSet.next()) {
                            out.println("User doesn't exist.");
                            return false;
                        } else {
                            out.println("Enter your password: ");
                            String password = in.readLine();
                            if (!password.equals(resultSet.getString("password"))) {
                                out.println("Wrong password");
                                return false;
                            } else {
                                return true;
                            }
                        }
                    }
                }
            } else if (response.equalsIgnoreCase("user")) {
                String userQuery = "SELECT * FROM user WHERE username = ?";
                try (PreparedStatement preparedStatement = connection.prepareStatement(userQuery)) {
                    preparedStatement.setString(1, nickname);
                    try (ResultSet resultSet = preparedStatement.executeQuery()) {
                        if (!resultSet.next()) {
                            out.println("User doesn't exist. Please register.");
                            out.println("Enter a password: ");
                            String password = in.readLine();
                            registerUser(connection, password);
                            out.println("Registered. You can now log in.");
                            return false;
                        } else {
                            out.println("Enter your password: ");
                            String password = in.readLine();
                            if (!password.equals(resultSet.getString("password"))) {
                                out.println("Wrong password");
                                return false;
                            } else {
                                return true;
                            }
                        }
                    }
                }
            }
            return false;
        }

        private void registerUser(Connection connection, String password) throws SQLException {
            String query = "INSERT INTO user (name, password) VALUES (?, ?)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, nickname);
                preparedStatement.setString(2, password);
                preparedStatement.executeUpdate();
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
                            .append(", Borrowed: ").append(resultSet.getBoolean("is_borrowed") ? "Yes" : "No")
                            .append("\n");
                }
            }
            return response.toString();
        }

        private String borrowBook(Connection connection, int bookId) throws SQLException {
            String query = "UPDATE books SET is_borrowed = TRUE, username = ? WHERE id = ? AND is_borrowed = FALSE";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, nickname);
                preparedStatement.setInt(2, bookId);
                int rowsUpdated = preparedStatement.executeUpdate();
                return rowsUpdated > 0 ? "Borrowed Successfully" : "Already Borrowed or does not exist";
            }
        }

        private String returnBook(Connection connection, int bookId) throws SQLException {
            String query = "UPDATE books SET is_borrowed = FALSE, username = NULL WHERE id = ? AND is_borrowed = TRUE";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setInt(1, bookId);
                int rowsUpdated = preparedStatement.executeUpdate();
                return rowsUpdated > 0 ? "Returned successfully" : "Book is not borrowed or does not exist";
            }
        }

        private static String deleteBook(Connection connection, int bookId) throws SQLException {
            String query = "DELETE FROM books WHERE id = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setInt(1, bookId);
                int rowsUpdated = preparedStatement.executeUpdate();
                return rowsUpdated > 0 ? "Deleted successfully" : "Book does not exist";
            }
        }

        private static String addBook(Connection connection, int id, String title, String author, boolean is_borrowed) throws SQLException {
            String query = "INSERT INTO books (id, title, author, is_borrowed) VALUES (?, ?, ?, ?)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setInt(1, id);
                preparedStatement.setString(2, title);
                preparedStatement.setString(3, author);
                preparedStatement.setBoolean(4, is_borrowed);
                int rowsUpdated = preparedStatement.executeUpdate();
                return rowsUpdated > 0 ? "Added successfully" : "Book already exists";
            }
        }

        private String adminList(Connection connection) throws SQLException {
            StringBuilder response = new StringBuilder();
            String query = "SELECT * FROM books";
            try (Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery(query)) {
                while (resultSet.next()) {
                    response.append("ID: ").append(resultSet.getInt("id"))
                            .append(", Title: ").append(resultSet.getString("title"))
                            .append(", Author: ").append(resultSet.getString("author"))
                            .append(", Borrowed: ").append(resultSet.getBoolean("is_borrowed") ? "Yes" : "No")
                            .append(resultSet.getBoolean("is_borrowed") ? ", Borrowed by: " + resultSet.getString("username") : "")
                            .append("\n");
                }
            }
            return response.toString();
        }

        private String addAdmin (Connection connection, String username, String password) throws SQLException{
            String query = "INSERT INTO admin (username, password) VALUES (?, ?)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, username);
                preparedStatement.setString(2, password);
                int rowsUpdated = preparedStatement.executeUpdate();
                return rowsUpdated > 0 ? "Added successfully" : "Failed";
            }
        }
    }
}