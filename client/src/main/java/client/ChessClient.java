package client;

import model.AuthData;

import java.io.IOException;
import java.util.Scanner;

public class ChessClient {

    private enum State {
        SIGNED_OUT,
        SIGNED_IN,
        QUIT
    }

    private final ServerFacade facade;
    private final Scanner scanner;
    private State state;
    private String authToken;
    private String username;

    public ChessClient(ServerFacade facade) {
        this.facade = facade;
        this.scanner = new Scanner(System.in);
        this.state = State.SIGNED_OUT;
    }

    public void run() {
        System.out.println("Welcome to Chess!");
        printHelp();

        while (state != State.QUIT) {
            System.out.print("\n[" + state + "] >>> ");

            if (!scanner.hasNextLine()) {
                break;
            }

            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                continue;
            }

            String[] parts = input.split("\\s+");
            String command = parts[0].toLowerCase();

            try {
                if (state == State.SIGNED_OUT) {
                    handleSignedOut(command, parts);
                } else {
                    handleSignedIn(command, parts);
                }
            } catch (IOException e) {
                System.out.println("Error: " + e.getMessage());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("Request interrupted.");
                break;
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid input: " + e.getMessage());
            }
        }

        System.out.println("Goodbye!");
    }

    private void handleSignedOut(String command, String[] parts)
            throws IOException, InterruptedException {

        switch (command) {
            case "help" -> printHelp();
            case "register" -> register(parts);
            case "login" -> login(parts);
            case "quit" -> state = State.QUIT;
            default -> System.out.println(
                    "Unknown command. Type help.");
        }
    }

    private void handleSignedIn(String command, String[] parts)
            throws IOException, InterruptedException {

        switch (command) {
            case "help" -> printHelp();
            case "logout" -> logout();
            case "quit" -> state = State.QUIT;
            default -> System.out.println(
                    "Unknown command. Type help.");
        }
    }

    private void register(String[] parts)
            throws IOException, InterruptedException {

        if (parts.length != 4) {
            System.out.println(
                    "Usage: register <username> <password> <email>");
            return;
        }

        AuthData auth = facade.register(
                parts[1], parts[2], parts[3]);

        signIn(auth);
        System.out.println("Registered successfully!");
    }

    private void login(String[] parts)
            throws IOException, InterruptedException {

        if (parts.length != 3) {
            System.out.println(
                    "Usage: login <username> <password>");
            return;
        }

        AuthData auth = facade.login(parts[1], parts[2]);

        signIn(auth);
        System.out.println("Logged in successfully!");
    }

    private void logout()
            throws IOException, InterruptedException {

        facade.logout(authToken);
        authToken = null;
        username = null;
        state = State.SIGNED_OUT;

        System.out.println("Logged out successfully!");
        printHelp();
    }

    private void signIn(AuthData auth) {
        authToken = auth.authToken();
        username = auth.username();
        state = State.SIGNED_IN;

        System.out.println("Welcome, " + username + "!");
        printHelp();
    }

    private void printHelp() {
        System.out.println("\nAvailable commands:");

        if (state == State.SIGNED_OUT) {
            System.out.println(
                    "  help                         Show commands");
            System.out.println(
                    "  register <user> <pass> <email> Create account");
            System.out.println(
                    "  login <user> <pass>           Log in");
            System.out.println(
                    "  quit                         Exit");
        } else {
            System.out.println(
                    "  help                         Show commands");
            System.out.println(
                    "  logout                       Log out");
            System.out.println(
                    "  quit                         Exit");
        }
    }
}
