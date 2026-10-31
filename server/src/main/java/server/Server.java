package server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dataaccess.*;
import handler.ClearHandler;
import handler.CreateGameHandler;
import handler.JoinGameHandler;
import handler.ListGamesHandler;
import handler.LoginHandler;
import handler.LogoutHandler;
import handler.RegisterHandler;
import io.javalin.Javalin;
import io.javalin.json.JsonMapper;
import org.jetbrains.annotations.NotNull;
import service.ClearService;
import service.CreateGameService;
import service.JoinGameService;
import service.ListGamesService;
import service.LoginService;
import service.LogoutService;
import service.RegisterService;

import java.lang.reflect.Type;

public class Server {

    private final Javalin javalin;

    public Server() {
        try {
            // Create SQL data access objects
            UserDAO userDAO = new SQLUserDAO();
            AuthDAO authDAO = new SQLAuthDAO();
            GameDAO gameDAO = new SQLGameDAO();

            // Create services
            var clearService = new ClearService(
                    userDAO, authDAO, gameDAO);
            var registerService = new RegisterService(
                    userDAO, authDAO);
            var loginService = new LoginService(
                    userDAO, authDAO);
            var logoutService = new LogoutService(authDAO);
            var listGamesService = new ListGamesService(
                    authDAO, gameDAO);
            var createGameService = new CreateGameService(
                    authDAO, gameDAO);
            var joinGameService = new JoinGameService(
                    authDAO, gameDAO);

            // Create handlers
            var clearHandler = new ClearHandler(clearService);
            var registerHandler = new RegisterHandler(registerService);
            var loginHandler = new LoginHandler(loginService);
            var logoutHandler = new LogoutHandler(logoutService);
            var listGamesHandler = new ListGamesHandler(listGamesService);
            var createGameHandler = new CreateGameHandler(
                    createGameService);
            var joinGameHandler = new JoinGameHandler(
                    joinGameService);

            // Configure Gson for JSON serialization
            Gson gson = new GsonBuilder()
                    .serializeNulls()
                    .create();

            javalin = Javalin.create(config -> {
                config.staticFiles.add("web");

                config.jsonMapper(new JsonMapper() {
                    @Override
                    @NotNull
                    public String toJsonString(
                            @NotNull Object obj,
                            @NotNull Type type) {
                        return gson.toJson(obj, type);
                    }

                    @Override
                    @NotNull
                    public <T> T fromJsonString(
                            @NotNull String json,
                            @NotNull Type targetType) {
                        return gson.fromJson(json, targetType);
                    }
                });
            });

            // Return a consistent JSON response for database failures.
            javalin.exception(DataAccessException.class, (exception, context) -> {
                context.status(500);
                context.json(java.util.Map.of(
                        "message", "Error: Database operation failed"));
            });

            // Register HTTP endpoints
            javalin.delete("/db", clearHandler);
            javalin.post("/user", registerHandler);
            javalin.post("/session", loginHandler);
            javalin.delete("/session", logoutHandler);
            javalin.get("/game", listGamesHandler);
            javalin.post("/game", createGameHandler);
            javalin.put("/game", joinGameHandler);

        } catch (DataAccessException ex) {
            throw new RuntimeException(
                    "Failed to initialize SQL database", ex);
        }
    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}

