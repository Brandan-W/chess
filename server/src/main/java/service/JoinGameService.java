package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import model.AuthData;
import model.GameData;

public class JoinGameService {

    private final AuthDAO authDAO;
    private final GameDAO gameDAO;

    public JoinGameService(AuthDAO authDAO, GameDAO gameDAO) {
        this.authDAO = authDAO;
        this.gameDAO = gameDAO;
    }

    public void joinGame(String authToken, String playerColor, int gameID)
            throws DataAccessException {

        // Check authentication
        if (authToken == null || authToken.isBlank()) {
            throw new SecurityException("Invalid auth token");
        }

        AuthData authData = authDAO.getAuth(authToken);

        if (authData == null) {
            throw new SecurityException("Invalid auth token");
        }

        // Validate game ID and player color
        if (gameID <= 0 || playerColor == null ||
                (!playerColor.equals("WHITE") &&
                 !playerColor.equals("BLACK"))) {
            throw new IllegalArgumentException("Invalid game information");
        }

        GameData game = gameDAO.getGame(gameID);

        if (game == null) {
            throw new IllegalArgumentException("Game not found");
        }

        String username = authData.username();
        GameData updatedGame;

        if (playerColor.equals("WHITE")) {
            if (game.whiteUsername() != null) {
                throw new IllegalStateException("White already taken");
            }

            updatedGame = new GameData(
                    game.gameID(),
                    username,
                    game.blackUsername(),
                    game.gameName(),
                    game.game()
            );

        } else {
            if (game.blackUsername() != null) {
                throw new IllegalStateException("Black already taken");
            }

            updatedGame = new GameData(
                    game.gameID(),
                    game.whiteUsername(),
                    username,
                    game.gameName(),
                    game.game()
            );
        }

        // Save the updated game
        gameDAO.updateGame(updatedGame);
    }
}
