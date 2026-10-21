package service;

import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryUserDAO;
import model.AuthData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RegisterServiceTest {

    private RegisterService registerService;
    private MemoryUserDAO userDAO;
    private MemoryAuthDAO authDAO;

    @BeforeEach
    public void setUp() {
        userDAO = new MemoryUserDAO();
        authDAO = new MemoryAuthDAO();
        registerService = new RegisterService(userDAO, authDAO);
    }

    @Test
    public void registerSuccess() throws Exception {
        AuthData auth = registerService.register(
                "testuser", "password123", "test@example.com");

        assertNotNull(auth);
        assertEquals("testuser", auth.username());
        assertNotNull(auth.authToken());

        assertNotNull(userDAO.getUser("testuser"));
        assertNotNull(authDAO.getAuth(auth.authToken()));
    }

    @Test
    public void registerDuplicateUsername() throws Exception {
        registerService.register(
                "testuser", "password123", "test@example.com");

        assertThrows(IllegalStateException.class, () ->
                registerService.register(
                        "testuser", "different", "other@example.com"));
    }

    @Test
    public void registerMissingInformation() {
        assertThrows(IllegalArgumentException.class, () ->
                registerService.register(null, "password", "test@example.com"));

        assertThrows(IllegalArgumentException.class, () ->
                registerService.register("testuser", "", "test@example.com"));

        assertThrows(IllegalArgumentException.class, () ->
                registerService.register("testuser", "password", null));
    }
}
