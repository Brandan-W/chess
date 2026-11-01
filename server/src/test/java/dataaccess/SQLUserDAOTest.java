package dataaccess;

import dataaccess.sql.SQLUserDAO;

import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SQLUserDAOTest {

    private SQLUserDAO userDAO;

    @BeforeEach
    void setup() throws DataAccessException {
        userDAO = new SQLUserDAO();
        userDAO.clear();
    }

    @Test
    void createAndGetUser() throws DataAccessException {
        UserData user = new UserData(
                "testuser",
                "testpassword",
                "test@example.com"
        );

        userDAO.createUser(user);

        UserData retrieved = userDAO.getUser("testuser");

        assertEquals(user, retrieved);
    }

    @Test
    void getNonexistentUser() throws DataAccessException {
        assertNull(userDAO.getUser("nonexistent"));
    }

    @Test
    void duplicateUsername() throws DataAccessException {
        UserData user = new UserData(
                "testuser",
                "testpassword",
                "test@example.com"
        );

        userDAO.createUser(user);

        assertThrows(DataAccessException.class,
                () -> userDAO.createUser(user));
    }

    @Test
    void clearUsers() throws DataAccessException {
        UserData user = new UserData(
                "testuser",
                "testpassword",
                "test@example.com"
        );

        userDAO.createUser(user);
        userDAO.clear();

        assertNull(userDAO.getUser("testuser"));
    }
}
