package service;

import chess.ChessGame;
import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryGameDAO;
import model.AuthData;
import model.GameData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class JoinGameServiceTest {

    private JoinGameService joinGameService;
    private MemoryGameDAO gameDAO;
    private int gameID;

    @BeforeEach
    public void setUp() {
        MemoryAuthDAO authDAO = new MemoryAuthDAO();
        gameDAO = new MemoryGameDAO();

        joinGameService = new JoinGameService(authDAO, gameDAO);

        authDAO.createAuth(new AuthData("token1", "player1"));
        authDAO.createAuth(new AuthData("token2", "player2"));

        gameID = gameDAO.createGame(new GameData(
                0, null, null, "Test Game", new ChessGame()
        ));
    }

    @Test
    public void joinWhiteSuccess() throws Exception {
        joinGameService.joinGame("token1", "WHITE", gameID);

        assertEquals("player1",
                gameDAO.getGame(gameID).whiteUsername());
        assertNull(gameDAO.getGame(gameID).blackUsername());
    }

    @Test
    public void joinBlackSuccess() throws Exception {
        joinGameService.joinGame("token2", "BLACK", gameID);

        assertEquals("player2",
                gameDAO.getGame(gameID).blackUsername());
        assertNull(gameDAO.getGame(gameID).whiteUsername());
    }

    @Test
    public void joinBothColors() throws Exception {
        joinGameService.joinGame("token1", "WHITE", gameID);
        joinGameService.joinGame("token2", "BLACK", gameID);

        GameData game = gameDAO.getGame(gameID);

        assertEquals("player1", game.whiteUsername());
        assertEquals("player2", game.blackUsername());
    }

    @Test
    public void joinOccupiedColor() throws Exception {
        joinGameService.joinGame("token1", "WHITE", gameID);

        assertThrows(IllegalStateException.class, () ->
                joinGameService.joinGame("token2", "WHITE", gameID)
        );
    }

    @Test
    public void joinInvalidToken() {
        assertThrows(SecurityException.class, () ->
                joinGameService.joinGame("invalid", "WHITE", gameID)
        );
    }

    @Test
    public void joinInvalidGameID() {
        assertThrows(IllegalArgumentException.class, () ->
                joinGameService.joinGame("token1", "WHITE", 999)
        );
    }

    @Test
    public void joinInvalidColor() {
        assertThrows(IllegalArgumentException.class, () ->
                joinGameService.joinGame("token1", "GREEN", gameID)
        );
    }

    @Test
    public void joinMissingColor() {
        assertThrows(IllegalArgumentException.class, () ->
                joinGameService.joinGame("token1", null, gameID)
        );
    }
}
