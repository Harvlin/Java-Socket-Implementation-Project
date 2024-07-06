package library;

import java.io.*;
import java.net.*;

public class libAdmin {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 3000;
    private static String username;
    private static String password;

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedReader consoleInput = new BufferedReader(new InputStreamReader(System.in));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            System.out.println("Connected to the library server");
            authenticateAdmin(in, out, consoleInput);

            while (true) {
                printMenu();
                String command = consoleInput.readLine().toLowerCase();
                switch (command) {
                    case "1":
                        addBook(out, in, consoleInput);
                        break;
                    case "2":
                        deleteBook(out, in, consoleInput);
                        break;
                    case "3":
                        listBooks(out, in);
                        break;
                    case "4":
                        addAdmin(out, in, consoleInput);
                        break;
                    case "5":
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

    private static void authenticateAdmin(BufferedReader in, PrintWriter out, BufferedReader consoleInput) throws IOException {
        String response;
        while (true) {
            response = in.readLine();
            if (response != null) {
                System.out.println(response);
                if (response.contains("Enter your name: ")) {
                    System.out.print("Enter your name: ");
                    username = consoleInput.readLine();
                    out.println(username);
                } else if (response.contains("Enter your password: ")) {
                    System.out.print("Enter your password: ");
                    password = consoleInput.readLine();
                    out.println(password);
                } else if (response.contains("User doesn't exist.") || response.contains("Wrong password")) {
                    System.out.println("Authentication failed. Please try again.");
                    continue;
                } else if (response.contains("Admin or User: ")) {
                    out.println("Admin");
                } else {
                    System.out.println("Authentication successful.");
                    break;
                }
            }
        }
    }

    private static void addBook(PrintWriter out, BufferedReader in, BufferedReader consoleInput) throws IOException {
        System.out.print("Book id: ");
        String bookId = consoleInput.readLine();
        System.out.print("Book title: ");
        String bookTitle = consoleInput.readLine();
        System.out.print("Book author: ");
        String bookAuthor = consoleInput.readLine();

        String addDetail = bookId + ", " + bookTitle + ", " + bookAuthor + ", " + "False";
        out.println("add " + addDetail);
        String response = in.readLine();
        System.out.println(response);
    }

    private static void deleteBook(PrintWriter out, BufferedReader in, BufferedReader consoleInput) throws IOException {
        System.out.print("Book id: ");
        String bookID = consoleInput.readLine();
        out.println("delete " + bookID);
        String response = in.readLine();
        System.out.println(response);
    }

    private static void listBooks(PrintWriter out, BufferedReader in) throws IOException {
        out.println("admin_List");
        String response;
        System.out.println("Available Books:");
        while ((response = in.readLine()) != null && !response.isEmpty()) {
            System.out.println(response);
        }
    }

    private static void addAdmin(PrintWriter out, BufferedReader in, BufferedReader consoleInput) throws IOException{
        System.out.print("Name: ");
        String name = consoleInput.readLine();
        System.out.print("Password: ");
        String password = consoleInput.readLine();

        String adminDetail = name + ", " + password;
        out.println("add_Admin " + adminDetail);
        String response = in.readLine();
        System.out.println(response);
    }

    private static void printMenu() {
        System.out.print("\nCommand Menu:\n1. Add\n2. Delete\n3. List\n4. Add another admin\n5. Exit\nEnter your choice: ");
    }
}