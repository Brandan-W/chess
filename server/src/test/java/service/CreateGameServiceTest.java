package service;

import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryGameDAO;
import model.AuthData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CreateGameServiceTest {

    private CreateGameService createGameService;
    private MemoryGameDAO gameDAO;

    @BeforeEach
    public void setUp() throws Exception {
        MemoryAuthDAO authDAO = new MemoryAuthDAO();
        gameDAO = new MemoryGameDAO();

        createGameService = new CreateGameService(authDAO, gameDAO);

        authDAO.createAuth(new AuthData(
                "valid-token",
                "testuser"
        ));
    }

    @Test
    public void createGameSuccess() throws Exception {
        int gameID = createGameService.createGame(
                "valid-token",
                "Practice Game"
        );

        assertEquals(1, gameID);
        assertEquals("Practice Game", gameDAO.getGame(gameID).gameName());
        assertNull(gameDAO.getGame(gameID).whiteUsername());
        assertNull(gameDAO.getGame(gameID).blackUsername());
    }

    @Test
    public void createGameInvalidToken() {
        assertThrows(SecurityException.class, () ->
                createGameService.createGame(
                        "invalid-token",
                        "Practice Game"
                )
        );
    }

    @Test
    public void createGameMissingName() {
        assertThrows(IllegalArgumentException.class, () ->
                createGameService.createGame(
                        "valid-token",
                        ""
                )
        );
    }

    @Test
    public void createGameUniqueIDs() throws Exception {
        int firstID = createGameService.createGame(
                "valid-token",
                "Game One"
        );

        int secondID = createGameService.createGame(
                "valid-token",
                "Game Two"
        );

        assertNotEquals(firstID, secondID);
        assertEquals(2, gameDAO.listGames().size());
    }
}
