package dataaccess;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;
import model.GameData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SQLGameDAOTest {

    private SQLGameDAO gameDAO;

    @BeforeEach
    void setup() throws DataAccessException {
        gameDAO = new SQLGameDAO();
        gameDAO.clear();
    }

    @Test
    void createAndGetGame() throws DataAccessException {
        ChessGame chessGame = new ChessGame();
        GameData game = new GameData(
                0, null, null, "Test Game", chessGame
        );

        int gameID = gameDAO.createGame(game);
        GameData retrieved = gameDAO.getGame(gameID);

        assertTrue(gameID > 0);
        assertNotNull(retrieved);
        assertEquals(gameID, retrieved.gameID());
        assertEquals("Test Game", retrieved.gameName());
        assertEquals(chessGame, retrieved.game());
    }

    @Test
    void getNonexistentGame() throws DataAccessException {
        assertNull(gameDAO.getGame(999999));
    }

    @Test
    void listGames() throws DataAccessException {
        gameDAO.createGame(new GameData(
                0, null, null, "Game One", new ChessGame()
        ));

        gameDAO.createGame(new GameData(
                0, null, null, "Game Two", new ChessGame()
        ));

        List<GameData> games = gameDAO.listGames();

        assertEquals(2, games.size());
        assertTrue(games.stream().anyMatch(
                game -> game.gameName().equals("Game One")));
        assertTrue(games.stream().anyMatch(
                game -> game.gameName().equals("Game Two")));
    }

    @Test
    void updateGame() throws DataAccessException {
        int gameID = gameDAO.createGame(new GameData(
                0, null, null, "Original Game", new ChessGame()
        ));

        GameData updated = new GameData(
                gameID,
                "whitePlayer",
                "blackPlayer",
                "Updated Game",
                new ChessGame()
        );

        gameDAO.updateGame(updated);

        GameData retrieved = gameDAO.getGame(gameID);

        assertNotNull(retrieved);
        assertEquals(updated, retrieved);
    }

    @Test
    void clearGames() throws DataAccessException {
        gameDAO.createGame(new GameData(
                0, null, null, "Test Game", new ChessGame()
        ));

        gameDAO.clear();

        assertTrue(gameDAO.listGames().isEmpty());
    }

    @Test
    void gameStatePersists() throws Exception {
        ChessGame chessGame = new ChessGame();

        ChessMove move = new ChessMove(
                new ChessPosition(2, 5),
                new ChessPosition(4, 5),
                null
        );

        chessGame.makeMove(move);

        int gameID = gameDAO.createGame(new GameData(
                0, null, null, "Played Game", chessGame
        ));

        GameData retrieved = gameDAO.getGame(gameID);

        assertNotNull(retrieved);
        assertEquals(chessGame, retrieved.game());
        assertEquals(
                ChessGame.TeamColor.BLACK,
                retrieved.game().getTeamTurn()
        );
        assertNotNull(
                retrieved.game().getBoard().getPiece(
                        new ChessPosition(4, 5))
        );
        assertNull(
                retrieved.game().getBoard().getPiece(
                        new ChessPosition(2, 5))
        );
    }
}
