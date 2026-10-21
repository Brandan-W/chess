package service;

import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryGameDAO;
import model.AuthData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ListGamesServiceTest {

    private ListGamesService listGamesService;
    private MemoryAuthDAO authDAO;

    @BeforeEach
    public void setUp() throws Exception {
        authDAO = new MemoryAuthDAO();
        MemoryGameDAO gameDAO = new MemoryGameDAO();

        listGamesService = new ListGamesService(authDAO, gameDAO);

        authDAO.createAuth(new AuthData(
                "valid-token",
                "testuser"
        ));
    }

    @Test
    public void listGamesSuccess() throws Exception {
        var games = listGamesService.listGames("valid-token");

        assertNotNull(games);
        assertTrue(games.isEmpty());
    }

    @Test
    public void listGamesInvalidToken() {
        assertThrows(SecurityException.class, () ->
                listGamesService.listGames("invalid-token")
        );
    }

    @Test
    public void listGamesMissingToken() {
        assertThrows(SecurityException.class, () ->
                listGamesService.listGames(null)
        );

        assertThrows(SecurityException.class, () ->
                listGamesService.listGames("")
        );
    }
}
