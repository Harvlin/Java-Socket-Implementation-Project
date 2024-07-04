package library;

import java.io.*;
import java.net.*;

public class libClient {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 3000;

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader consoleInput = new BufferedReader(new InputStreamReader(System.in))) {

            System.out.println("Connected to the library server.");

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
