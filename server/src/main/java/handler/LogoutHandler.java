package handler;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import service.LogoutService;

import java.util.Map;

public class LogoutHandler implements Handler {

    private final LogoutService logoutService;

    public LogoutHandler(LogoutService logoutService) {
        this.logoutService = logoutService;
    }

    @Override
    public void handle(Context ctx) throws Exception {
        try {
            // Read authentication token from request header
            String authToken = ctx.header("authorization");

            // Log out the user
            logoutService.logout(authToken);

            // Return successful response
            ctx.status(200);
            ctx.json(Map.of());

        } catch (SecurityException e) {
            ctx.status(401);
            ctx.json(Map.of("message", "Error: unauthorized"));
        }
    }
}