package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.UserDAO;
import model.AuthData;
import model.UserData;

import java.util.UUID;

public class RegisterService {

    private final UserDAO userDAO;
    private final AuthDAO authDAO;

    public RegisterService(UserDAO userDAO, AuthDAO authDAO) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
    }

    public AuthData register(String username, String password, String email)
            throws DataAccessException {

        // Validate required registration fields
        if (username == null || username.isEmpty()
                || password == null || password.isEmpty()
                || email == null || email.isEmpty()) {
            throw new IllegalArgumentException("Missing required registration information");
        }

        // Check whether the username is already taken
        if (userDAO.getUser(username) != null) {
            throw new IllegalStateException("Username already taken");
        }

        // Store the new user
        UserData user = new UserData(username, password, email);
        userDAO.createUser(user);

        // Generate and store an authentication token
        String token = UUID.randomUUID().toString();
        AuthData auth = new AuthData(token, username);
        authDAO.createAuth(auth);

        return auth;
    }
}
