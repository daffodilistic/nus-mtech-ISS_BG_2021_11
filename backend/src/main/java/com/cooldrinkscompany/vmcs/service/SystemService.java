package com.cooldrinkscompany.vmcs.service;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
//import java.util.logging.Level;
import java.util.logging.Logger;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import com.cooldrinkscompany.vmcs.controller.ControllerManageDrink;
import com.cooldrinkscompany.vmcs.controller.ControllerSetSystemStatus;
import com.cooldrinkscompany.vmcs.pojo.ProductDAOImpl;
import com.cooldrinkscompany.vmcs.pojo.SessionManager;
import com.google.gson.Gson;

import io.helidon.common.reactive.Multi;
import io.helidon.config.Config;
import io.helidon.webserver.*;
import io.helidon.webserver.Routing;
import io.helidon.webserver.ServerRequest;
import io.helidon.webserver.ServerResponse;
import io.helidon.webserver.Service;

public class SystemService implements Service {
    private static final Logger LOGGER = Logger.getLogger(SystemService.class.getName());
    private static final JsonBuilderFactory JSON_FACTORY = Json.createBuilderFactory(Collections.emptyMap());

    private final ProductDAOImpl productDao;
    private final Config config;

    public SystemService(ProductDAOImpl productDao) {
        this.productDao = productDao;
        this.config = Config.create();
    }

    @Override
    public void update(Routing.Rules rules) {
        rules
                .post(PathMatcher.create("/login"), this::login)
                .post(PathMatcher.create("/logout"), this::logout)
                .get(PathMatcher.create("/viewDoorStatus"), this::viewDoorStatus)
                .post(PathMatcher.create("/setDoorStatus"), this::toggleDoor);
    }

    private boolean validatePassword(String inputPassword) {
        String realPassword = this.config.get("password").asMap().get().get("password");
        if (inputPassword.equals(realPassword)) {
            return true;
        }
        return false;
    }

    private void login(ServerRequest request, ServerResponse response) {
        request.content().as(JsonObject.class).thenAccept(json -> {
            String inputPassword = json.getString("password", null);
            boolean isValidLogin = validatePassword(inputPassword);
            JsonObjectBuilder builder = JSON_FACTORY.createObjectBuilder().add("success", isValidLogin);
            if (isValidLogin) {
                String loginResponse = ControllerSetSystemStatus.setLoggedIn(this.productDao);
                JsonObject returnObject = builder.add("message", loginResponse).build();
                response.send(returnObject);
            } else {
                JsonObject returnObject = builder.add("message", "Failed to login with password " + inputPassword)
                        .build();
                response.send(returnObject);
            }
        }).exceptionally(e -> {
            LOGGER.info("[login] Exception: " + e.getMessage());
            e.printStackTrace();

            StringWriter stackTrace = new StringWriter();
            e.printStackTrace(new PrintWriter(stackTrace));

            Map<String, Object> data = new HashMap<String, Object>();
            data.put("error", "Failed login!");
            data.put("message", e.getMessage());

            // LOGGER.info("[insertCoin] Data: " + new Gson().toJson(data));

            response.addHeader("Content-Type", "application/json").send(new Gson().toJson(data));
            return null;
        });
    }

    private void logout(ServerRequest request, ServerResponse response) {
        if (ControllerSetSystemStatus.getStatus(this.productDao, "isUnlocked")) {
            JsonObject returnObject = JSON_FACTORY.createObjectBuilder()
                    .add("Status:", "Log out Failed. Please lock the door first.").build();
            response.send(returnObject);
        } else {
            String logoutResponse = ControllerSetSystemStatus.setStatus(this.productDao, "isLoggedIn", false);
            JsonObject returnObject = JSON_FACTORY.createObjectBuilder()
                    .add("Status:", logoutResponse.equals("Success") ? "Logged out" : "Logged out Failed").build();
            response.send(returnObject);
        }
    }

    private void viewDoorStatus(ServerRequest request, ServerResponse response) {
        Boolean doorStatus = ControllerSetSystemStatus.getStatus(this.productDao, "isUnlocked");
        JsonObject returnObject = JSON_FACTORY.createObjectBuilder().add("Status:", doorStatus ? "Unlocked" : "Locked")
                .build();
        response.send(returnObject);
    }

    private void toggleDoor(ServerRequest request, ServerResponse response) {
        request.content().as(JsonObject.class).thenAccept(json -> {
            boolean lockDoor = json.getBoolean("lock_door", false);
            String status = ControllerSetSystemStatus.setStatus(this.productDao, "isUnlocked", lockDoor);
            JsonObject returnObject = JSON_FACTORY.createObjectBuilder()
                    .add("success", !status.contains("Failed"))
                    .add("message", "Door is " + (lockDoor ? "locked" : "unlocked"))
                    .build();
            SessionManager.getInstance().updateMachineStatus();
            response.send(returnObject);
        }).exceptionally(e -> {
            LOGGER.info("[toggleDoor] Exception: " + e.getMessage());
            e.printStackTrace();

            StringWriter stackTrace = new StringWriter();
            e.printStackTrace(new PrintWriter(stackTrace));

            Map<String, Object> data = new HashMap<String, Object>();
            data.put("error", "Failed to lock the door!");
            data.put("message", e.getMessage());

            // LOGGER.info("[insertCoin] Data: " + new Gson().toJson(data));

            response.addHeader("Content-Type", "application/json").send(new Gson().toJson(data));
            return null;
        });
    }
}
