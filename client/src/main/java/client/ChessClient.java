package client;

import chess.ChessBoard;
import chess.ChessGame;
import model.AuthData;
import model.GameData;
import ui.ChessBoardRenderer;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class ChessClient {

    private enum State {
        SIGNED_OUT,
        SIGNED_IN,
        QUIT
    }

    private final ServerFacade facade;
    private final Scanner scanner;
    private final ChessBoardRenderer renderer;

    private State state;
    private String authToken;
    private String username;
    private List<GameData> displayedGames = List.of();

    public ChessClient(ServerFacade facade) {
        this.facade = facade;
        this.scanner = new Scanner(System.in);
        this.renderer = new ChessBoardRenderer();
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
                    handleSignedIn(command, parts, input);
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

    private void handleSignedIn(
            String command, String[] parts, String input)
            throws IOException, InterruptedException {

        switch (command) {
            case "help" -> printHelp();
            case "list" -> listGames();
            case "create" -> createGame(input);
            case "join" -> joinGame(parts);
            case "observe" -> observeGame(parts);
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
        displayedGames = List.of();
        state = State.SIGNED_OUT;

        System.out.println("Logged out successfully!");
        printHelp();
    }

    private void listGames()
            throws IOException, InterruptedException {

        displayedGames = List.of();

        List<GameData> games = facade.listGames(authToken);
        displayedGames = List.copyOf(games);

        if (displayedGames.isEmpty()) {
            System.out.println("No games available.");
            return;
        }

        System.out.println("\nAvailable games:");

        for (int i = 0; i < displayedGames.size(); i++) {
            GameData game = displayedGames.get(i);

            String white = game.whiteUsername() == null
                    ? "Available" : game.whiteUsername();

            String black = game.blackUsername() == null
                    ? "Available" : game.blackUsername();

            System.out.printf(
                    "  %d. %s | White: %s | Black: %s%n",
                    i + 1,
                    game.gameName(),
                    white,
                    black
            );
        }
    }

    private void createGame(String input)
            throws IOException, InterruptedException {

        String[] parts = input.split("\\s+", 2);

        if (parts.length < 2 || parts[1].isBlank()) {
            System.out.println("Usage: create <game name>");
            return;
        }

        String gameName = parts[1].trim();

        facade.createGame(authToken, gameName);

        displayedGames = List.of();

        System.out.println(
                "Game created successfully! Type list to see games.");
    }

    private void joinGame(String[] parts)
            throws IOException, InterruptedException {

        if (parts.length != 3) {
            System.out.println(
                    "Usage: join <game number> <WHITE|BLACK>");
            return;
        }

        int gameNumber = parseGameNumber(parts[1]);

        ChessGame.TeamColor color;

        try {
            color = ChessGame.TeamColor.valueOf(
                    parts[2].toUpperCase());
        } catch (IllegalArgumentException e) {
            System.out.println(
                    "Invalid color. Choose WHITE or BLACK.");
            return;
        }

        GameData game = findGame(gameNumber);

        if (game == null) {
            System.out.println(
                    "Game number not found. Type list to see games.");
            return;
        }

        facade.joinGame(
                authToken,
                color.name(),
                game.gameID()
        );

        System.out.println(
                "Joined " + game.gameName() + " as " + color + "!");

        drawGameBoard(color);
    }

    private void observeGame(String[] parts)
            throws IOException, InterruptedException {

        if (parts.length != 2) {
            System.out.println(
                    "Usage: observe <game number>");
            return;
        }

        int gameNumber = parseGameNumber(parts[1]);

        GameData game = findGame(gameNumber);

        if (game == null) {
            System.out.println(
                    "Game number not found. Type list to see games.");
            return;
        }

        System.out.println(
                "Observing " + game.gameName());

        drawGameBoard(ChessGame.TeamColor.WHITE);
    }

    private int parseGameNumber(String value) {
        try {
            int gameNumber = Integer.parseInt(value);

            if (gameNumber <= 0) {
                throw new NumberFormatException();
            }

            return gameNumber;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Game number must be a positive integer.");
        }
    }

    private GameData findGame(int gameNumber) {

        if (gameNumber < 1
                || gameNumber > displayedGames.size()) {
            return null;
        }

        return displayedGames.get(gameNumber - 1);
    }

    private void drawGameBoard(
            ChessGame.TeamColor perspective) {

        ChessBoard board = new ChessBoard();
        board.resetBoard();

        renderer.drawBoard(board, perspective);
    }

    private void signIn(AuthData auth) {
        authToken = auth.authToken();
        username = auth.username();
        displayedGames = List.of();
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
        } else if (state == State.SIGNED_IN) {
            System.out.println(
                    "  help                         Show commands");
            System.out.println(
                    "  list                         List available games");
            System.out.println(
                    "  create <game name>           Create a game");
            System.out.println(
                    "  join <number> <WHITE|BLACK>   Join a listed game");
            System.out.println(
                    "  observe <number>             Observe a listed game");
            System.out.println(
                    "  logout                       Log out");
            System.out.println(
                    "  quit                         Exit");
        }
    }
}
