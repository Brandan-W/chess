package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import model.AuthData;

public class LogoutService {

    private final AuthDAO authDAO;

    public LogoutService(AuthDAO authDAO) {
        this.authDAO = authDAO;
    }

    public void logout(String authToken) throws DataAccessException {

        // Validate authentication token
        if (authToken == null || authToken.isEmpty()) {
            throw new SecurityException("Unauthorized");
        }

        // Check that the token exists
        AuthData auth = authDAO.getAuth(authToken);

        if (auth == null) {
            throw new SecurityException("Unauthorized");
        }

        // Remove the authentication token
        authDAO.deleteAuth(authToken);
    }
}