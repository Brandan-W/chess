package handler;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import model.AuthData;
import service.RegisterService;

import java.util.Map;

public class RegisterHandler implements Handler {

    private final RegisterService registerService;

    public RegisterHandler(RegisterService registerService) {
        this.registerService = registerService;
    }

    @Override
    public void handle(Context ctx) throws Exception {
        try {
            // Read the registration request
            Map<String, String> request = new Gson().fromJson(
                    ctx.body(),
                    new TypeToken<Map<String, String>>() {}.getType()
            );

            String username = request.get("username");
            String password = request.get("password");
            String email = request.get("email");

            // Register the user
            AuthData auth = registerService.register(username, password, email);

            // Return username and authentication token
            ctx.status(200);
            ctx.json(Map.of(
                    "username", auth.username(),
                    "authToken", auth.authToken()
            ));

        } catch (IllegalArgumentException e) {
            ctx.status(400);
            ctx.json(Map.of("message", "Error: bad request"));

        } catch (IllegalStateException e) {
            ctx.status(403);
            ctx.json(Map.of("message", "Error: username already taken"));
        }
    }
}
