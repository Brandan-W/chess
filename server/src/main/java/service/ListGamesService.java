package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import model.GameData;

import java.util.ArrayList;
import java.util.List;

public class ListGamesService {

    private final AuthDAO authDAO;
    private final GameDAO gameDAO;

    public ListGamesService(AuthDAO authDAO, GameDAO gameDAO) {
        this.authDAO = authDAO;
        this.gameDAO = gameDAO;
    }

    public List<GameData> listGames(String authToken)
            throws DataAccessException {

        // Validate the authentication token
        if (authToken == null || authToken.isEmpty()
                || authDAO.getAuth(authToken) == null) {
            throw new SecurityException("Invalid auth token");
        }

        // Retrieve all existing games
        return new ArrayList<>(gameDAO.listGames());
    }
}
