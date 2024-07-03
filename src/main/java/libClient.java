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
            String response;

            while (true) {
                printMenu();
                String command = consoleInput.readLine().toLowerCase();
                switch (command) {
                    case "list":
                        out.println("list");
                        response = in.readLine();
                        while (response != null && !response.isEmpty()) {
                            System.out.println("Available Books:");
                            System.out.println(response);
                        }
                        break;
                    case "borrow":
                        System.out.print("Enter book ID to borrow: ");
                        String borrowId = consoleInput.readLine();

                        out.println("borrow " +  borrowId);
                        response = in.readLine();
                        System.out.println(response);
                        break;
                    case "return":
                        System.out.print("Enter book ID to return: ");
                        String returnId = consoleInput.readLine();
                        out.println("return " + returnId);
                        response = in.readLine();
                        System.out.println(response);
                        break;
                    case "exit":
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
    private static void printMenu() {
        System.out.println("\nLibrary Menu:");
        System.out.println("1. List all books");
        System.out.println("2. Borrow a book");
        System.out.println("3. Return a book");
        System.out.println("4. Exit");
        System.out.print("Enter your choice: ");
    }
}
