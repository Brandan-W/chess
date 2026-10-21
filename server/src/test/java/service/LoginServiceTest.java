package service;

import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryUserDAO;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LoginServiceTest {

    private LoginService loginService;
    private MemoryUserDAO userDAO;
    private MemoryAuthDAO authDAO;

    @BeforeEach
    public void setUp() {
        userDAO = new MemoryUserDAO();
        authDAO = new MemoryAuthDAO();

        loginService = new LoginService(userDAO, authDAO);

        // Create an existing user for login tests
        userDAO.createUser(new UserData(
                "testuser",
                "password123",
                "test@example.com"
        ));
    }

    @Test
    public void loginSuccess() throws Exception {
        AuthData auth = loginService.login(
                "testuser",
                "password123"
        );

        assertNotNull(auth);
        assertEquals("testuser", auth.username());
        assertNotNull(auth.authToken());

        // Verify the token was stored
        assertNotNull(authDAO.getAuth(auth.authToken()));
    }

    @Test
    public void loginMissingInformation() {
        assertThrows(IllegalArgumentException.class, () ->
                loginService.login("", "password123")
        );

        assertThrows(IllegalArgumentException.class, () ->
                loginService.login("testuser", "")
        );
    }

    @Test
    public void loginIncorrectPassword() {
        assertThrows(SecurityException.class, () ->
                loginService.login("testuser", "wrongpassword")
        );
    }

    @Test
    public void loginNonexistentUser() {
        assertThrows(SecurityException.class, () ->
                loginService.login("unknownuser", "password123")
        );
    }
}
