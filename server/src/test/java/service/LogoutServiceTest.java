package service;

import dataaccess.MemoryAuthDAO;
import model.AuthData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LogoutServiceTest {

    private LogoutService logoutService;
    private MemoryAuthDAO authDAO;

    @BeforeEach
    public void setUp() {
        authDAO = new MemoryAuthDAO();
        logoutService = new LogoutService(authDAO);
    }

    @Test
    public void logoutSuccess() throws Exception {
        AuthData auth = new AuthData("test-token", "testuser");
        authDAO.createAuth(auth);

        logoutService.logout("test-token");

        assertNull(authDAO.getAuth("test-token"));
    }

    @Test
    public void logoutMissingToken() {
        assertThrows(SecurityException.class, () ->
                logoutService.logout(null));

        assertThrows(SecurityException.class, () ->
                logoutService.logout(""));
    }

    @Test
    public void logoutInvalidToken() {
        assertThrows(SecurityException.class, () ->
                logoutService.logout("invalid-token"));
    }
}