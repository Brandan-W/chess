package client;

import com.google.gson.Gson;
import model.AuthData;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

public class ServerFacade {

    private final String serverUrl;
    private final HttpClient httpClient;
    private final Gson gson;

    public ServerFacade(int port) {
        serverUrl = "http://localhost:" + port;
        httpClient = HttpClient.newHttpClient();
        gson = new Gson();
    }

    public AuthData register(String username, String password, String email)
            throws IOException, InterruptedException {

        String requestBody = gson.toJson(Map.of(
                "username", username,
                "password", password,
                "email", email
        ));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + "/user"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(
                request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("Registration failed: " + response.body());
        }

        return gson.fromJson(response.body(), AuthData.class);
    }

    public AuthData login(String username, String password)
            throws IOException, InterruptedException {

        String requestBody = gson.toJson(Map.of(
                "username", username,
                "password", password
        ));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + "/session"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(
                request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("Login failed: " + response.body());
        }

        return gson.fromJson(response.body(), AuthData.class);
    }
}
