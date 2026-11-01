package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import model.GameData;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class SQLGameDAO implements GameDAO {

    private final Gson gson = new Gson();

    public SQLGameDAO() throws DataAccessException {
        DatabaseManager.createDatabase();

        String sql = """
                CREATE TABLE IF NOT EXISTS games (
                    gameID INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                    whiteUsername VARCHAR(255),
                    blackUsername VARCHAR(255),
                    gameName VARCHAR(255) NOT NULL,
                    game TEXT NOT NULL
                )
                """;

        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement(sql)) {
            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException(
                    "Failed to create games table", ex);
        }
    }

    @Override
    public int createGame(GameData game) throws DataAccessException {
        String sql = """
                INSERT INTO games
                    (whiteUsername, blackUsername, gameName, game)
                VALUES (?, ?, ?, ?)
                """;

        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, game.whiteUsername());
            statement.setString(2, game.blackUsername());
            statement.setString(3, game.gameName());
            statement.setString(4, gson.toJson(game.game()));

            statement.executeUpdate();

            try (var keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }

            throw new DataAccessException(
                    "Failed to generate game ID");

        } catch (SQLException ex) {
            throw new DataAccessException(
                    "Failed to create game", ex);
        }
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        String sql = """
                SELECT gameID, whiteUsername, blackUsername,
                       gameName, game
                FROM games
                WHERE gameID = ?
                """;

        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement(sql)) {

            statement.setInt(1, gameID);

            try (var results = statement.executeQuery()) {
                if (results.next()) {
                    return new GameData(
                            results.getInt("gameID"),
                            results.getString("whiteUsername"),
                            results.getString("blackUsername"),
                            results.getString("gameName"),
                            gson.fromJson(
                                    results.getString("game"),
                                    ChessGame.class)
                    );
                }
            }

            return null;

        } catch (SQLException | JsonParseException ex) {
            throw new DataAccessException(
                    "Failed to retrieve game", ex);
        }
    }

    @Override
    public List<GameData> listGames() throws DataAccessException {
        String sql = """
                SELECT gameID, whiteUsername, blackUsername,
                       gameName, game
                FROM games
                """;

        List<GameData> games = new ArrayList<>();

        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement(sql);
             var results = statement.executeQuery()) {

            while (results.next()) {
                games.add(new GameData(
                        results.getInt("gameID"),
                        results.getString("whiteUsername"),
                        results.getString("blackUsername"),
                        results.getString("gameName"),
                        gson.fromJson(
                                results.getString("game"),
                                ChessGame.class)
                ));
            }

            return games;

        } catch (SQLException | JsonParseException ex) {
            throw new DataAccessException(
                    "Failed to list games", ex);
        }
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
        String sql = """
                UPDATE games
                SET whiteUsername = ?,
                    blackUsername = ?,
                    gameName = ?,
                    game = ?
                WHERE gameID = ?
                """;

        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement(sql)) {

            statement.setString(1, game.whiteUsername());
            statement.setString(2, game.blackUsername());
            statement.setString(3, game.gameName());
            statement.setString(4, gson.toJson(game.game()));
            statement.setInt(5, game.gameID());

            statement.executeUpdate();

        } catch (SQLException ex) {
            throw new DataAccessException(
                    "Failed to update game", ex);
        }
    }

    @Override
    public void clear() throws DataAccessException {
        String sql = "TRUNCATE TABLE games";

        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement(sql)) {
            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException(
                    "Failed to clear games", ex);
        }
    }
}
