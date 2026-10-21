package handler;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import model.AuthData;
import service.LoginService;

import java.util.Map;

public class LoginHandler implements Handler {

    private final LoginService loginService;

    public LoginHandler(LoginService loginService) {
        this.loginService = loginService;
    }

    @Override
    public void handle(Context ctx) throws Exception {
        try {
            // Read the login request
            Map<String, String> request = new Gson().fromJson(
                    ctx.body(),
                    new TypeToken<Map<String, String>>() {}.getType()
            );

            if (request == null) {
                throw new IllegalArgumentException("Missing login request");
            }

            String username = request.get("username");
            String password = request.get("password");

            // Authenticate the user
            AuthData auth = loginService.login(username, password);

            // Return username and authentication token
            ctx.status(200);
            ctx.json(Map.of(
                    "username", auth.username(),
                    "authToken", auth.authToken()
            ));

        } catch (IllegalArgumentException e) {
            ctx.status(400);
            ctx.json(Map.of("message", "Error: bad request"));

        } catch (SecurityException e) {
            ctx.status(401);
            ctx.json(Map.of("message", "Error: unauthorized"));
        }
    }
}
