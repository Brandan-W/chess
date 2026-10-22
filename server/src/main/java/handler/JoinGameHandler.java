package handler;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import service.JoinGameService;

import java.util.Map;

public class JoinGameHandler implements Handler {

    private final JoinGameService joinGameService;

    public JoinGameHandler(JoinGameService joinGameService) {
        this.joinGameService = joinGameService;
    }

    private record JoinGameRequest(
            String playerColor,
            Integer gameID
    ) {}

    @Override
    public void handle(Context ctx) throws Exception {
        try {
            String authToken = ctx.header("authorization");

            JoinGameRequest request =
                    ctx.bodyAsClass(JoinGameRequest.class);

            joinGameService.joinGame(
                    authToken,
                    request == null ? null : request.playerColor(),
                    request == null || request.gameID() == null
                            ? 0 : request.gameID()
            );

            ctx.status(200);
            ctx.json(Map.of());

        } catch (SecurityException e) {
            ctx.status(401);
            ctx.json(Map.of(
                    "message", "Error: unauthorized"
            ));

        } catch (IllegalArgumentException e) {
            ctx.status(400);
            ctx.json(Map.of(
                    "message", "Error: bad request"
            ));

        } catch (IllegalStateException e) {
            ctx.status(403);
            ctx.json(Map.of(
                    "message", "Error: already taken"
            ));
        }
    }
}
