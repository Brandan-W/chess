package server;

import com.google.gson.Gson;
import dataaccess.*;
import handler.ClearHandler;
import handler.CreateGameHandler;
import handler.ListGamesHandler;
import handler.LoginHandler;
import handler.LogoutHandler;
import handler.RegisterHandler;
import io.javalin.Javalin;
import io.javalin.json.JsonMapper;
import org.jetbrains.annotations.NotNull;
import service.ClearService;
import service.CreateGameService;
import service.ListGamesService;
import service.LoginService;
import service.LogoutService;
import service.RegisterService;

import java.lang.reflect.Type;

public class Server {

    private final Javalin javalin;

    public Server() {

        // Create in-memory data access objects
        UserDAO userDAO = new MemoryUserDAO();
        AuthDAO authDAO = new MemoryAuthDAO();
        GameDAO gameDAO = new MemoryGameDAO();

        // Create services
        var clearService = new ClearService(userDAO, authDAO, gameDAO);
        var registerService = new RegisterService(userDAO, authDAO);
        var loginService = new LoginService(userDAO, authDAO);
        var logoutService = new LogoutService(authDAO);
        var listGamesService = new ListGamesService(authDAO, gameDAO);
        var createGameService = new CreateGameService(authDAO, gameDAO);

        // Create handlers
        var clearHandler = new ClearHandler(clearService);
        var registerHandler = new RegisterHandler(registerService);
        var loginHandler = new LoginHandler(loginService);
        var logoutHandler = new LogoutHandler(logoutService);
        var listGamesHandler = new ListGamesHandler(listGamesService);
        var createGameHandler = new CreateGameHandler(createGameService);

        // Configure Gson for JSON serialization
        Gson gson = new com.google.gson.GsonBuilder()
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

        // Register HTTP endpoints
        javalin.delete("/db", clearHandler);
        javalin.post("/user", registerHandler);
        javalin.post("/session", loginHandler);
        javalin.delete("/session", logoutHandler);
        javalin.get("/game", listGamesHandler);
        javalin.post("/game", createGameHandler);
    }

    public int run(int port) {
        javalin.start(port);
        return port;
    }

    public void stop() {
        javalin.stop();
    }
}
