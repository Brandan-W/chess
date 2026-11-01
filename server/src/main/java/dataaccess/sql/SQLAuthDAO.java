package dataaccess.sql;

import dataaccess.*;

import model.AuthData;

import java.sql.SQLException;

public class SQLAuthDAO implements AuthDAO {

    public SQLAuthDAO() throws DataAccessException {
        DatabaseManager.createDatabase();

        String sql = """
                CREATE TABLE IF NOT EXISTS auth (
                    authToken VARCHAR(255) NOT NULL PRIMARY KEY,
                    username VARCHAR(255) NOT NULL
                )
                """;

        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement(sql)) {
            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException(
                    "Failed to create auth table", ex);
        }
    }

    @Override
    public void createAuth(AuthData auth) throws DataAccessException {
        String sql = """
                INSERT INTO auth (authToken, username)
                VALUES (?, ?)
                """;

        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement(sql)) {

            statement.setString(1, auth.authToken());
            statement.setString(2, auth.username());

            statement.executeUpdate();

        } catch (SQLException ex) {
            throw new DataAccessException(
                    "Failed to create auth token", ex);
        }
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        String sql = """
                SELECT authToken, username
                FROM auth
                WHERE authToken = ?
                """;

        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement(sql)) {

            statement.setString(1, authToken);

            try (var results = statement.executeQuery()) {
                if (results.next()) {
                    return new AuthData(
                            results.getString("authToken"),
                            results.getString("username")
                    );
                }
            }

            return null;

        } catch (SQLException ex) {
            throw new DataAccessException(
                    "Failed to retrieve auth token", ex);
        }
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        String sql = "DELETE FROM auth WHERE authToken = ?";

        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement(sql)) {

            statement.setString(1, authToken);
            statement.executeUpdate();

        } catch (SQLException ex) {
            throw new DataAccessException(
                    "Failed to delete auth token", ex);
        }
    }

    @Override
    public void clear() throws DataAccessException {
        String sql = "DELETE FROM auth";

        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement(sql)) {
            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException(
                    "Failed to clear auth tokens", ex);
        }
    }
}
