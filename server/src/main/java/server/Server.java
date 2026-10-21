package server;

import com.google.gson.Gson;
import io.javalin.Javalin;
import io.javalin.json.JsonMapper;
import handler.ClearHandler;
import handler.RegisterHandler;
import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryGameDAO;
import dataaccess.MemoryUserDAO;
import org.jetbrains.annotations.NotNull;
import service.ClearService;
import service.RegisterService;

import java.lang.reflect.Type;

public class Server {

    private final Javalin javalin;

    public Server() {
        // Create data access objects
        var userDAO = new MemoryUserDAO();
        var authDAO = new MemoryAuthDAO();
        var gameDAO = new MemoryGameDAO();

        // Create services
        var clearService = new ClearService(userDAO, authDAO, gameDAO);
        var registerService = new RegisterService(userDAO, authDAO);

        // Create handlers
        var clearHandler = new ClearHandler(clearService);
        var registerHandler = new RegisterHandler(registerService);

        // Configure Gson for JSON serialization
        Gson gson = new Gson();

        javalin = Javalin.create(config -> {
            config.staticFiles.add("web");

            config.jsonMapper(new JsonMapper() {
                @Override
                @NotNull
                public String toJsonString(@NotNull Object obj, @NotNull Type type) {
                    return gson.toJson(obj, type);
                }

                @Override
                @NotNull
                public <T> T fromJsonString(@NotNull String json, @NotNull Type targetType) {
                    return gson.fromJson(json, targetType);
                }
            });
        });

        // Register HTTP endpoints
        javalin.delete("/db", clearHandler);
        javalin.post("/user", registerHandler);
    }

    public int run(int port) {
        javalin.start(port);
        return port;
    }

    public void stop() {
        javalin.stop();
    }
}