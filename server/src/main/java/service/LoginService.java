package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.UserDAO;
import model.AuthData;
import model.UserData;

import java.util.UUID;

public class LoginService {

    private final UserDAO userDAO;
    private final AuthDAO authDAO;

    public LoginService(UserDAO userDAO, AuthDAO authDAO) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
    }

    public AuthData login(String username, String password)
            throws DataAccessException {

        // Validate required login fields
        if (username == null || username.isEmpty()
                || password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Missing login information");
        }

        // Find the existing user
        UserData user = userDAO.getUser(username);

        // Verify the username and password
        if (user == null || !user.password().equals(password)) {
            throw new SecurityException("Invalid username or password");
        }

        // Create a new authentication token
        AuthData auth = new AuthData(
                UUID.randomUUID().toString(),
                username
        );

        authDAO.createAuth(auth);

        return auth;
    }
}
