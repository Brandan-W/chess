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
}
