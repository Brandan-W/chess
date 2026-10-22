package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import model.GameData;

public class CreateGameService {

    private final AuthDAO authDAO;
    private final GameDAO gameDAO;

    public CreateGameService(AuthDAO authDAO, GameDAO gameDAO) {
        this.authDAO = authDAO;
        this.gameDAO = gameDAO;
    }

    public int createGame(String authToken, String gameName)
            throws DataAccessException {

        // Validate authentication
        if (authToken == null || authToken.isBlank()
                || authDAO.getAuth(authToken) == null) {
            throw new SecurityException("Invalid auth token");
        }

        // Validate game name
        if (gameName == null || gameName.isBlank()) {
            throw new IllegalArgumentException("Missing game name");
        }

        // Create an unoccupied chess game
        GameData game = new GameData(
                0,
                null,
                null,
                gameName,
                new chess.ChessGame()
        );

        // Store game and return its assigned ID
        return gameDAO.createGame(game);
    }
}
