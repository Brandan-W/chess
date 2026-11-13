package client;

import com.google.gson.Gson;
import model.AuthData;
import model.GameData;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

public class ServerFacade {

    private final String serverUrl;
    private final HttpClient httpClient;
    private final Gson gson;

    private record GamesResponse(List<GameData> games) {
    }

    private record CreateGameResponse(int gameID) {
    }

    public ServerFacade(int port) {
        serverUrl = "http://localhost:" + port;
        httpClient = HttpClient.newHttpClient();
        gson = new Gson();
    }

    private <T> T request(
            String method,
            String path,
            String authToken,
            Object body,
            Class<T> responseType)
            throws IOException, InterruptedException {

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + path));

        if (authToken != null) {
            builder.header("authorization", authToken);
        }

        String json = body == null ? "" : gson.toJson(body);

        switch (method) {
            case "POST", "PUT" -> {
                builder.header(
                        "Content-Type", "application/json");

                builder.method(
                        method,
                        HttpRequest.BodyPublishers.ofString(json));
            }

            case "DELETE" -> builder.DELETE();

            case "GET" -> builder.GET();

            default -> throw new IllegalArgumentException(
                    "Unsupported HTTP method");
        }

        HttpResponse<String> response;

        try {
            response = httpClient.send(
                    builder.build(),
                    HttpResponse.BodyHandlers.ofString()
            );
        } catch (ConnectException e) {
            throw new IOException(
                    "Cannot connect to the chess server. "
                            + "Make sure it is running.",
                    e
            );
        }

        if (response.statusCode() != 200) {

            String message = switch (response.statusCode()) {
                case 400 -> "Invalid request.";

                case 401 -> "Unauthorized. Please log in.";

                case 403 -> "The requested action is not allowed.";

                case 500 -> "The server encountered an error.";

                default -> "Unable to complete the request.";
            };

            throw new IOException(message);
        }

        if (responseType == Void.class) {
            return null;
        }

        return gson.fromJson(
                response.body(),
                responseType
        );
    }

    public AuthData register(
            String username,
            String password,
            String email)
            throws IOException, InterruptedException {

        return request(
                "POST",
                "/user",
                null,
                Map.of(
                        "username", username,
                        "password", password,
                        "email", email
                ),
                AuthData.class
        );
    }

    public AuthData login(
            String username,
            String password)
            throws IOException, InterruptedException {

        return request(
                "POST",
                "/session",
                null,
                Map.of(
                        "username", username,
                        "password", password
                ),
                AuthData.class
        );
    }

    public void logout(String authToken)
            throws IOException, InterruptedException {

        request(
                "DELETE",
                "/session",
                authToken,
                null,
                Void.class
        );
    }

    public List<GameData> listGames(String authToken)
            throws IOException, InterruptedException {

        GamesResponse response = request(
                "GET",
                "/game",
                authToken,
                null,
                GamesResponse.class
        );

        if (response == null || response.games() == null) {
            throw new IOException(
                    "Invalid game list response from server.");
        }

        return response.games();
    }

    public int createGame(
            String authToken,
            String gameName)
            throws IOException, InterruptedException {

        CreateGameResponse response = request(
                "POST",
                "/game",
                authToken,
                Map.of("gameName", gameName),
                CreateGameResponse.class
        );

        if (response == null || response.gameID() <= 0) {
            throw new IOException(
                    "Invalid create game response from server.");
        }

        return response.gameID();
    }

    public void joinGame(
            String authToken,
            String playerColor,
            int gameID)
            throws IOException, InterruptedException {

        request(
                "PUT",
                "/game",
                authToken,
                Map.of(
                        "playerColor", playerColor,
                        "gameID", gameID
                ),
                Void.class
        );
    }
}
