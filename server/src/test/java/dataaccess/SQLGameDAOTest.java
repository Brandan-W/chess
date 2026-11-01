package dataaccess;

import dataaccess.sql.SQLGameDAO;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPiece;
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

    private ChessMove move(int startRow, int startCol,
                           int endRow, int endCol) {
        return new ChessMove(
                new ChessPosition(startRow, startCol),
                new ChessPosition(endRow, endCol),
                null
        );
    }

    private ChessGame saveAndReload(ChessGame chessGame)
            throws DataAccessException {
        int gameID = gameDAO.createGame(new GameData(
                0, null, null, "Persistence Test", chessGame
        ));

        GameData retrieved = gameDAO.getGame(gameID);
        assertNotNull(retrieved);

        return retrieved.game();
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

        chessGame.makeMove(move(2, 5, 4, 5));

        ChessGame restored = saveAndReload(chessGame);

        assertEquals(chessGame, restored);
        assertEquals(
                ChessGame.TeamColor.BLACK,
                restored.getTeamTurn()
        );
        assertNotNull(restored.getBoard().getPiece(
                new ChessPosition(4, 5)));
        assertNull(restored.getBoard().getPiece(
                new ChessPosition(2, 5)));
    }

    @Test
    void continuePlayingAfterReload() throws Exception {
        ChessGame game = new ChessGame();

        game.makeMove(move(2, 5, 4, 5));

        ChessGame restored = saveAndReload(game);

        restored.makeMove(move(7, 5, 5, 5));

        assertEquals(
                ChessGame.TeamColor.WHITE,
                restored.getTeamTurn()
        );
        assertNotNull(restored.getBoard().getPiece(
                new ChessPosition(5, 5)));
        assertNull(restored.getBoard().getPiece(
                new ChessPosition(7, 5)));
    }

    @Test
    void castlingRightsPersist() throws Exception {
        ChessGame game = new ChessGame();

        game.makeMove(move(2, 5, 4, 5));
        game.makeMove(move(7, 5, 5, 5));
        game.makeMove(move(1, 7, 3, 6));
        game.makeMove(move(8, 2, 6, 3));
        game.makeMove(move(1, 6, 2, 5));
        game.makeMove(move(8, 7, 6, 6));

        ChessGame restored = saveAndReload(game);

        ChessMove castle = move(1, 5, 1, 7);

        assertTrue(restored.validMoves(
                new ChessPosition(1, 5)).contains(castle));

        restored.makeMove(castle);

        assertEquals(
                ChessPiece.PieceType.KING,
                restored.getBoard().getPiece(
                        new ChessPosition(1, 7)).getPieceType()
        );
        assertEquals(
                ChessPiece.PieceType.ROOK,
                restored.getBoard().getPiece(
                        new ChessPosition(1, 6)).getPieceType()
        );
    }

    @Test
    void enPassantPersists() throws Exception {
        ChessGame game = new ChessGame();

        game.makeMove(move(2, 5, 4, 5));
        game.makeMove(move(7, 1, 6, 1));
        game.makeMove(move(4, 5, 5, 5));
        game.makeMove(move(7, 4, 5, 4));

        ChessGame restored = saveAndReload(game);

        ChessMove enPassant = move(5, 5, 6, 4);

        assertTrue(restored.validMoves(
                new ChessPosition(5, 5)).contains(enPassant));

        restored.makeMove(enPassant);

        assertNotNull(restored.getBoard().getPiece(
                new ChessPosition(6, 4)));
        assertNull(restored.getBoard().getPiece(
                new ChessPosition(5, 4)));
    }

    @Test
    void pawnPromotionPersists() throws Exception {
        ChessGame game = new ChessGame();

        game.makeMove(move(2, 1, 4, 1));
        game.makeMove(move(7, 2, 5, 2));
        game.makeMove(move(4, 1, 5, 2));
        game.makeMove(move(7, 8, 6, 8));
        game.makeMove(move(5, 2, 6, 2));
        game.makeMove(move(6, 8, 5, 8));
        game.makeMove(move(6, 2, 7, 2));
        game.makeMove(move(7, 7, 6, 7));

        ChessGame restored = saveAndReload(game);

        ChessMove promotion = new ChessMove(
                new ChessPosition(7, 2),
                new ChessPosition(8, 1),
                ChessPiece.PieceType.QUEEN
        );

        assertTrue(restored.validMoves(
                new ChessPosition(7, 2)).contains(promotion));

        restored.makeMove(promotion);

        assertEquals(
                ChessPiece.PieceType.QUEEN,
                restored.getBoard().getPiece(
                        new ChessPosition(8, 1)).getPieceType()
        );
    }
}
