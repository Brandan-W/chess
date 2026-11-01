package dataaccess;

import dataaccess.sql.SQLAuthDAO;

import model.AuthData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SQLAuthDAOTest {

    private SQLAuthDAO authDAO;

    @BeforeEach
    void setup() throws DataAccessException {
        authDAO = new SQLAuthDAO();
        authDAO.clear();
    }

    @Test
    void createAndGetAuth() throws DataAccessException {
        AuthData auth = new AuthData(
                "test-token-123",
                "testuser"
        );

        authDAO.createAuth(auth);

        AuthData retrieved = authDAO.getAuth("test-token-123");

        assertEquals(auth, retrieved);
    }

    @Test
    void getNonexistentAuth() throws DataAccessException {
        assertNull(authDAO.getAuth("nonexistent-token"));
    }

    @Test
    void duplicateAuthToken() throws DataAccessException {
        AuthData auth = new AuthData(
                "test-token-123",
                "testuser"
        );

        authDAO.createAuth(auth);

        assertThrows(DataAccessException.class,
                () -> authDAO.createAuth(auth));
    }

    @Test
    void deleteAuth() throws DataAccessException {
        AuthData auth = new AuthData(
                "test-token-123",
                "testuser"
        );

        authDAO.createAuth(auth);
        authDAO.deleteAuth("test-token-123");

        assertNull(authDAO.getAuth("test-token-123"));
    }

    @Test
    void clearAuth() throws DataAccessException {
        authDAO.createAuth(
                new AuthData("token-1", "user1")
        );

        authDAO.createAuth(
                new AuthData("token-2", "user2")
        );

        authDAO.clear();

        assertNull(authDAO.getAuth("token-1"));
        assertNull(authDAO.getAuth("token-2"));
    }
}
