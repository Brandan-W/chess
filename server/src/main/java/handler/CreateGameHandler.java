package handler;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import service.CreateGameService;

import java.util.Map;

public class CreateGameHandler implements Handler {

    private final CreateGameService createGameService;

    public CreateGameHandler(CreateGameService createGameService) {
        this.createGameService = createGameService;
    }

    private record CreateGameRequest(String gameName) {}

    @Override
    public void handle(Context ctx) throws Exception {
        try {
            String authToken = ctx.header("authorization");

            CreateGameRequest request =
                    ctx.bodyAsClass(CreateGameRequest.class);

            int gameID = createGameService.createGame(
                    authToken,
                    request == null ? null : request.gameName()
            );

            ctx.status(200);
            ctx.json(Map.of("gameID", gameID));

        } catch (SecurityException e) {
            ctx.status(401);
            ctx.json(Map.of("message", "Error: unauthorized"));

        } catch (IllegalArgumentException e) {
            ctx.status(400);
            ctx.json(Map.of("message", "Error: bad request"));
        }
    }
}
