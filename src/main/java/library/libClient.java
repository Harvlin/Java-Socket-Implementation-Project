package library;

import java.io.*;
import java.net.*;

public class libClient {
    private static final String SERVER_ADDRESS = "192.168.6.12";
    private static final int SERVER_PORT = 3000;
    private static String username;
    private static String password;

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader consoleInput = new BufferedReader(new InputStreamReader(System.in))) {

            System.out.println("Connected to the library server.");
            authenticateUser(in, out, consoleInput);

            while (true) {
                printMenu();
                String command = consoleInput.readLine().toLowerCase();
                switch (command) {
                    case "1":
                        listBooks(out, in);
                        break;
                    case "2":
                        borrowBook(out, in, consoleInput);
                        break;
                    case "3":
                        returnBook(out, in, consoleInput);
                        break;
                    case "4":
                        System.out.println("Exiting...");
                        return;
                    default:
                        System.out.println("Invalid command. Please try again.");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void authenticateUser(BufferedReader in, PrintWriter out, BufferedReader consoleInput) throws IOException {
        String response;
        while (true) {
            response = in.readLine();
            if (response != null) {
                System.out.println(response);
                if (response.contains("Admin or User")) {
                    System.out.print("Admin or User: ");
                    String status = consoleInput.readLine();
                    out.println(status);
                } else if (response.contains("Enter your name: ")) {
                    username = consoleInput.readLine();
                    out.println(username);
                } else if (response.contains("Enter your password: ") || response.contains("Enter a password: ")) {
                    password = consoleInput.readLine();
                    out.println(password);
                } else if (response.contains("Registered. You can now log in.")) {
                    break;
                } else if (response.contains("Wrong password") || response.contains("User doesn't exist")) {
                    continue;
                }
            }
        }
    }

    private static void listBooks(PrintWriter out, BufferedReader in) throws IOException {
        out.println("list");
        String response = in.readLine();
        System.out.println("Available Books:");
        while (response != null && !response.isEmpty()) {
            System.out.println(response);
            response = in.readLine();
        }
    }

    private static void borrowBook(PrintWriter out, BufferedReader in, BufferedReader consoleInput) throws IOException {
        System.out.print("Enter book ID to borrow: ");
        String borrowId = consoleInput.readLine();
        out.println("borrow " + borrowId);
        String response = in.readLine();
        System.out.println(response);
    }

    private static void returnBook(PrintWriter out, BufferedReader in, BufferedReader consoleInput) throws IOException {
        System.out.print("Enter book ID to return: ");
        String returnId = consoleInput.readLine();
        out.println("return " + returnId);
        String response = in.readLine();
        System.out.println(response);
    }

    private static void printMenu() {
        System.out.println("\nLibrary Menu:\n1. List all books\n2. Borrow a book\n3. Return a book\n4. Exit\nEnter your choice: ");
    }
}