package client;

import dataaccess.DatabaseManager;
import model.AuthData;
import org.junit.jupiter.api.*;
import server.Server;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade facade;

    @BeforeAll
    public static void init() {
        server = new Server();
        int port = server.run(0);
        facade = new ServerFacade(port);
    }

    @AfterAll
    public static void stopServer() {
        server.stop();
    }

    @BeforeEach
    public void clearDatabase() throws Exception {
        DatabaseManager.createDatabase();

        try (var connection = DatabaseManager.getConnection();
             var statement = connection.createStatement()) {

            statement.executeUpdate("DELETE FROM auth");
            statement.executeUpdate("DELETE FROM games");
            statement.executeUpdate("DELETE FROM users");
        }
    }

    @Test
    public void registerSuccess() throws Exception {
        AuthData result = facade.register(
                "player1", "password123", "player1@example.com");

        assertEquals("player1", result.username());
        assertNotNull(result.authToken());
        assertFalse(result.authToken().isBlank());
    }

    @Test
    public void registerDuplicateUsername() throws Exception {
        facade.register(
                "player1", "password123", "player1@example.com");

        assertThrows(IOException.class, () ->
                facade.register(
                        "player1", "anotherPassword", "other@example.com"));
    }

    @Test
    public void loginSuccess() throws Exception {
        facade.register(
                "player1", "password123", "player1@example.com");

        AuthData result = facade.login("player1", "password123");

        assertEquals("player1", result.username());
        assertNotNull(result.authToken());
        assertFalse(result.authToken().isBlank());
    }

    @Test
    public void loginWrongPassword() throws Exception {
        facade.register(
                "player1", "password123", "player1@example.com");

        assertThrows(IOException.class, () ->
                facade.login("player1", "wrongPassword"));
    }

    @Test
    public void logoutSuccess() throws Exception {
        AuthData auth = facade.register(
                "player1", "password123", "player1@example.com");

        facade.logout(auth.authToken());

        assertThrows(IOException.class, () ->
                facade.listGames(auth.authToken()));
    }

    @Test
    public void logoutInvalidToken() {
        assertThrows(IOException.class, () ->
                facade.logout("invalid-token"));
    }

    @Test
    public void listGamesSuccess() throws Exception {
        AuthData auth = facade.register(
                "player1", "password123", "player1@example.com");

        int gameID = facade.createGame(auth.authToken(), "Test Game");

        var games = facade.listGames(auth.authToken());

        assertEquals(1, games.size());
        assertEquals(gameID, games.getFirst().gameID());
        assertEquals("Test Game", games.getFirst().gameName());
    }

    @Test
    public void listGamesInvalidToken() {
        assertThrows(IOException.class, () ->
                facade.listGames("invalid-token"));
    }

    @Test
    public void createGameSuccess() throws Exception {
        AuthData auth = facade.register(
                "player1", "password123", "player1@example.com");

        int gameID = facade.createGame(auth.authToken(), "New Game");

        assertTrue(gameID > 0);
        assertEquals(1, facade.listGames(auth.authToken()).size());
    }

    @Test
    public void createGameInvalidToken() {
        assertThrows(IOException.class, () ->
                facade.createGame("invalid-token", "New Game"));
    }

    @Test
    public void joinGameSuccess() throws Exception {
        AuthData auth = facade.register(
                "player1", "password123", "player1@example.com");

        int gameID = facade.createGame(auth.authToken(), "Test Game");

        facade.joinGame(auth.authToken(), "WHITE", gameID);

        var games = facade.listGames(auth.authToken());

        assertEquals("player1", games.getFirst().whiteUsername());
    }

    @Test
    public void joinGameColorTaken() throws Exception {
        AuthData player1 = facade.register(
                "player1", "password123", "player1@example.com");

        AuthData player2 = facade.register(
                "player2", "password123", "player2@example.com");

        int gameID = facade.createGame(
                player1.authToken(), "Test Game");

        facade.joinGame(player1.authToken(), "WHITE", gameID);

        assertThrows(IOException.class, () ->
                facade.joinGame(player2.authToken(), "WHITE", gameID));
    }

    @Test
    public void createGameMissingName() throws Exception {
        AuthData auth = facade.register(
                "player1", "password123", "player1@example.com");

        assertThrows(IOException.class, () ->
                facade.createGame(auth.authToken(), ""));
    }

    @Test
    public void joinGameInvalidGameID() throws Exception {
        AuthData auth = facade.register(
                "player1", "password123", "player1@example.com");

        assertThrows(IOException.class, () ->
                facade.joinGame(auth.authToken(), "WHITE", -1));
    }
}
