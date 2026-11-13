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
    private boolean gamesListed = false;

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
            case "help" -> {
                if (noExtraArguments(parts)) {
                    printHelp();
                }
            }
            case "register" -> register(parts);
            case "login" -> login(parts);
            case "quit" -> {
                if (noExtraArguments(parts)) {
                    state = State.QUIT;
                }
            }
            default -> System.out.println(
                    "Unknown command. Type help.");
        }
    }

    private void handleSignedIn(
            String command, String[] parts, String input)
            throws IOException, InterruptedException {

        switch (command) {
            case "help" -> {
                if (noExtraArguments(parts)) {
                    printHelp();
                }
            }
            case "list" -> {
                if (noExtraArguments(parts)) {
                    listGames();
                }
            }
            case "create" -> createGame(input);
            case "join", "play" -> joinGame(parts);
            case "observe" -> observeGame(parts);
            case "logout" -> {
                if (noExtraArguments(parts)) {
                    logout();
                }
            }
            case "quit" -> {
                if (noExtraArguments(parts)) {
                    state = State.QUIT;
                }
            }
            default -> System.out.println(
                    "Unknown command. Type help.");
        }
    }

    private boolean noExtraArguments(String[] parts) {
        if (parts.length != 1) {
            System.out.println(
                    "This command does not accept additional arguments.");
            return false;
        }

        return true;
    }

    private String prompt(String label) {
        System.out.print(label + ": ");

        if (!scanner.hasNextLine()) {
            state = State.QUIT;
            throw new IllegalArgumentException(
                    "Input ended before the command was completed.");
        }

        return scanner.nextLine().trim();
    }

    private void register(String[] parts)
            throws IOException, InterruptedException {

        if (parts.length != 1 && parts.length != 4) {
            System.out.println(
                    "Usage: register <username> <password> <email>");
            return;
        }

        String user = parts.length == 4
                ? parts[1] : prompt("Username");

        String password = parts.length == 4
                ? parts[2] : prompt("Password");

        String email = parts.length == 4
                ? parts[3] : prompt("Email");

        if (user.isBlank() || password.isBlank() || email.isBlank()) {
            System.out.println(
                    "All registration fields are required.");
            return;
        }

        if (!email.contains("@")
                || email.startsWith("@")
                || email.endsWith("@")) {
            System.out.println(
                    "Please enter a valid email address.");
            return;
        }

        AuthData auth = facade.register(user, password, email);

        signIn(auth);
        System.out.println("Registered successfully!");
    }

    private void login(String[] parts)
            throws IOException, InterruptedException {

        if (parts.length != 1 && parts.length != 3) {
            System.out.println(
                    "Usage: login <username> <password>");
            return;
        }

        String user = parts.length == 3
                ? parts[1] : prompt("Username");

        String password = parts.length == 3
                ? parts[2] : prompt("Password");

        if (user.isBlank() || password.isBlank()) {
            System.out.println(
                    "Username and password are required.");
            return;
        }

        AuthData auth = facade.login(user, password);

        signIn(auth);
        System.out.println("Logged in successfully!");
    }

    private void logout()
            throws IOException, InterruptedException {

        facade.logout(authToken);

        authToken = null;
        username = null;
        clearGameList();
        state = State.SIGNED_OUT;

        System.out.println("Logged out successfully!");
        printHelp();
    }

    private void clearGameList() {
        displayedGames = List.of();
        gamesListed = false;
    }

    private void listGames()
            throws IOException, InterruptedException {

        clearGameList();

        List<GameData> games = facade.listGames(authToken);
        displayedGames = List.copyOf(games);
        gamesListed = true;

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

        String gameName = parts.length == 2
                ? parts[1].trim() : prompt("Game name");

        if (gameName.isBlank()) {
            System.out.println(
                    "Game name cannot be empty.");
            return;
        }

        facade.createGame(authToken, gameName);
        clearGameList();

        System.out.println(
                "Game created successfully! Type list to see games.");
    }

    private void joinGame(String[] parts)
            throws IOException, InterruptedException {

        if (parts.length != 1 && parts.length != 3) {
            System.out.println(
                    "Usage: join <game number> <WHITE|BLACK>");
            return;
        }

        if (!ensureGamesListed()) {
            return;
        }

        String numberInput = parts.length == 3
                ? parts[1] : prompt("Game number");

        int gameNumber = parseGameNumber(numberInput);

        GameData game = findGame(gameNumber);

        if (game == null) {
            System.out.println(
                    "Game number not found. Type list to see games.");
            return;
        }

        String colorInput = parts.length == 3
                ? parts[2] : prompt("Color (WHITE or BLACK)");

        ChessGame.TeamColor color;

        try {
            color = ChessGame.TeamColor.valueOf(
                    colorInput.toUpperCase());
        } catch (IllegalArgumentException e) {
            System.out.println(
                    "Invalid color. Choose WHITE or BLACK.");
            return;
        }

        // Check whether the selected color is already occupied.
        String occupiedBy = color == ChessGame.TeamColor.WHITE
                ? game.whiteUsername()
                : game.blackUsername();

        if (occupiedBy != null) {
            System.out.println(
                    color + " is already occupied by " + occupiedBy
                            + ". Type list to refresh available games.");
            return;
        }

        // Ask the server to join the selected game.
        facade.joinGame(
                authToken,
                color.name(),
                game.gameID()
        );

        // The game list is now outdated, so clear it.
        clearGameList();

        System.out.println(
                "Joined " + game.gameName() + " as " + color + "!");

        drawGameBoard(color);
    }

    private void observeGame(String[] parts)
            throws IOException, InterruptedException {

        if (parts.length != 1 && parts.length != 2) {
            System.out.println(
                    "Usage: observe <game number>");
            return;
        }

        if (!ensureGamesListed()) {
            return;
        }

        String numberInput = parts.length == 2
                ? parts[1] : prompt("Game number");

        int gameNumber = parseGameNumber(numberInput);

        GameData game = findGame(gameNumber);

        if (game == null) {
            System.out.println(
                    "Game number not found. Type list to see games.");
            return;
        }

        System.out.println("Observing " + game.gameName());

        drawGameBoard(ChessGame.TeamColor.WHITE);
    }

    private boolean ensureGamesListed() {
        if (!gamesListed) {
            System.out.println(
                    "Please type list before selecting a game.");
            return false;
        }

        if (displayedGames.isEmpty()) {
            System.out.println(
                    "No games available. Create a game or refresh the list.");
            return false;
        }

        return true;
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
        clearGameList();
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
                    "  register                     Create an account");
            System.out.println(
                    "  login                        Log in");
            System.out.println(
                    "  quit                         Exit");
        } else if (state == State.SIGNED_IN) {
            System.out.println(
                    "  help                         Show commands");
            System.out.println(
                    "  list                         List available games");
            System.out.println(
                    "  create                       Create a game");
            System.out.println(
                    "  join / play                  Join a listed game");
            System.out.println(
                    "  observe                      Observe a listed game");
            System.out.println(
                    "  logout                       Log out");
            System.out.println(
                    "  quit                         Exit");
        }
    }
}
