package handler;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import model.GameData;
import service.ListGamesService;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ListGamesHandler implements Handler {

    private final ListGamesService listGamesService;

    public ListGamesHandler(ListGamesService listGamesService) {
        this.listGamesService = listGamesService;
    }

    @Override
    public void handle(Context ctx) throws Exception {
        try {
            String authToken = ctx.header("authorization");

            List<GameData> games =
                    listGamesService.listGames(authToken);

            List<Map<String, Object>> gameSummaries =
                    new ArrayList<>();

            for (GameData game : games) {
                Map<String, Object> summary =
                        new LinkedHashMap<>();

                summary.put("gameID", game.gameID());
                summary.put("whiteUsername", game.whiteUsername());
                summary.put("blackUsername", game.blackUsername());
                summary.put("gameName", game.gameName());

                gameSummaries.add(summary);
            }

            ctx.status(200);
            ctx.json(Map.of("games", gameSummaries));

        } catch (SecurityException e) {
            ctx.status(401);
            ctx.json(Map.of(
                    "message", "Error: unauthorized"
            ));
        }
    }
}
