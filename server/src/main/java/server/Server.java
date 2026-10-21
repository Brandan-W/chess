package server;

import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryGameDAO;
import dataaccess.MemoryUserDAO;
import handler.ClearHandler;
import io.javalin.Javalin;
import service.ClearService;

public class Server {

    private final Javalin javalin;

    public Server() {
        var userDAO = new MemoryUserDAO();
        var authDAO = new MemoryAuthDAO();
        var gameDAO = new MemoryGameDAO();

        var clearService = new ClearService(userDAO, authDAO, gameDAO);
        var clearHandler = new ClearHandler(clearService);

        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        javalin.delete("/db", clearHandler);
    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}
