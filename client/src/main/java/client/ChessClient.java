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
        state = State.SIGNED_OUT;

        System.out.println("Logged out successfully!");
        printHelp();
    }

    private void listGames()
            throws IOException, InterruptedException {

        List<GameData> games = facade.listGames(authToken);

        if (games.isEmpty()) {
            System.out.println("No games available.");
            return;
        }

        System.out.println("\nAvailable games:");

        for (GameData game : games) {
            String white = game.whiteUsername() == null
                    ? "Available" : game.whiteUsername();

            String black = game.blackUsername() == null
                    ? "Available" : game.blackUsername();

            System.out.printf(
                    "  ID: %d | %s | White: %s | Black: %s%n",
                    game.gameID(),
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
        int gameID = facade.createGame(authToken, gameName);

        System.out.println(
                "Game created successfully! ID: " + gameID);
    }

    private void joinGame(String[] parts)
            throws IOException, InterruptedException {

        if (parts.length != 3) {
            System.out.println(
                    "Usage: join <game ID> <WHITE|BLACK>");
            return;
        }

        int gameID = parseGameID(parts[1]);

        ChessGame.TeamColor color;
        try {
            color = ChessGame.TeamColor.valueOf(
                    parts[2].toUpperCase());
        } catch (IllegalArgumentException e) {
            System.out.println(
                    "Invalid color. Choose WHITE or BLACK.");
            return;
        }

        GameData game = findGame(gameID);

        if (game == null) {
            System.out.println("Game not found.");
            return;
        }

        facade.joinGame(authToken, color.name(), gameID);

        System.out.println(
                "Joined game " + gameID + " as " + color + "!");

        drawGameBoard(game, color);
    }

    private void observeGame(String[] parts)
            throws IOException, InterruptedException {

        if (parts.length != 2) {
            System.out.println("Usage: observe <game ID>");
            return;
        }

        int gameID = parseGameID(parts[1]);
        GameData game = findGame(gameID);

        if (game == null) {
            System.out.println("Game not found.");
            return;
        }

        System.out.println(
                "Observing game " + gameID + ": "
                        + game.gameName());

        drawGameBoard(game, ChessGame.TeamColor.WHITE);
    }

    private int parseGameID(String value) {
        try {
            int gameID = Integer.parseInt(value);

            if (gameID <= 0) {
                throw new NumberFormatException();
            }

            return gameID;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Game ID must be a positive integer.");
        }
    }

    private GameData findGame(int gameID)
            throws IOException, InterruptedException {

        List<GameData> games = facade.listGames(authToken);

        for (GameData game : games) {
            if (game.gameID() == gameID) {
                return game;
            }
        }

        return null;
    }

    private void drawGameBoard(
            GameData game, ChessGame.TeamColor perspective) {

        ChessBoard board = new ChessBoard();
        board.resetBoard();

        renderer.drawBoard(board, perspective);
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
        } else if (state == State.SIGNED_IN) {
            System.out.println(
                    "  help                         Show commands");
            System.out.println(
                    "  list                         List available games");
            System.out.println(
                    "  create <game name>           Create a game");
            System.out.println(
                    "  join <ID> <WHITE|BLACK>        Join a game");
            System.out.println(
                    "  observe <ID>                 Observe a game");
            System.out.println(
                    "  logout                       Log out");
            System.out.println(
                    "  quit                         Exit");
        }
    }
}
