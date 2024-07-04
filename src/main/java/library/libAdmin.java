package library;

import java.net.*;
import java.io.*;

public class libAdmin {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 3000;

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedReader consoleInput = new BufferedReader(new InputStreamReader(System.in));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            System.out.println("Connected to the library server");
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

    private static void addBook(PrintWriter out, BufferedReader in, BufferedReader consoleInput) throws IOException {
        System.out.print("Book id: "); String bookId = consoleInput.readLine();
        System.out.print("Book title: "); String bookTitle = consoleInput.readLine();
        System.out.print("Book author: "); String bookAuthor = consoleInput.readLine();

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
        out.println("list");
        String response = in.readLine();
        System.out.println("Available Books:");
        while (response != null && !response.isEmpty()) {
            System.out.println(response);
            response = in.readLine();
        }
    }

    private static void printMenu() {
        System.out.print("\nCommand Menu:\n1. Add\n2. Delete\n3. List\n4. Exit\nEnter your choice: ");
    }
}
