package com.cooldrinkscompany.vmcs.service;

import java.util.Collections;
//import java.util.logging.Level;
import java.util.logging.Logger;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObject;

import com.cooldrinkscompany.vmcs.controller.ControllerManageDrink;
import com.cooldrinkscompany.vmcs.pojo.ProductDAOImpl;
import io.helidon.common.reactive.Multi;
import io.helidon.webserver.*;
import io.helidon.webserver.Routing;
import io.helidon.webserver.ServerRequest;
import io.helidon.webserver.ServerResponse;
import io.helidon.webserver.Service;

public class DrinksService implements Service {
    private static final Logger LOGGER = Logger.getLogger(DrinksService.class.getName());
    private static final JsonBuilderFactory JSON_FACTORY = Json.createBuilderFactory(Collections.emptyMap());

    private final ProductDAOImpl productDao;

    public DrinksService(ProductDAOImpl productDao) {
        this.productDao = productDao;
    }

    @Override
    public void update(Routing.Rules rules) {

        rules
        .get("/", this::listDrinks)
        .get(PathMatcher.create("/viewDrinkQty/*"), this::viewDrinkQty)
        .get(PathMatcher.create("/setDrinkQty/*"), this::setDrinkQty)
        .get(PathMatcher.create("/viewDrinkPrice/*"), this::viewDrinkPrice)
        .get(PathMatcher.create("/setDrinkPrice/*"), this::setDrinkPrice);
    }
    
    private void listDrinks(ServerRequest request, ServerResponse response) {
        Multi<JsonObject> rows = this.productDao.getDbClient().execute(exec -> exec.createQuery("SELECT * FROM drinks").execute())
                .map(it -> it.as(JsonObject.class));

        rows.collectList().thenAccept(list -> {
            JsonArrayBuilder arrayBuilder = JSON_FACTORY.createArrayBuilder();
            list.forEach(arrayBuilder::add);
            JsonArray array = arrayBuilder.build();
            response.send(Json.createObjectBuilder().add("drinks", array).build());
        });
    }

    private void viewDrinkQty(ServerRequest request, ServerResponse response){
        LOGGER.info("start viewing drinks");
        String drinkName = request.path().toString().replace("/viewDrinkQty/","");
        int qty = ControllerManageDrink.queryDrinkQty(this.productDao, drinkName);
        JsonObject returnObject = JSON_FACTORY.createObjectBuilder()
                .add("Drink Qty:", qty)
                .build();
        response.send(returnObject);
    }

    private void setDrinkQty(ServerRequest request, ServerResponse response){
        LOGGER.info("start setting drinks qty");
        String drinkParams = request.path().toString().replace("/setDrinkQty/","");
        String[] drinkTypeAndQty = drinkParams.split(":");
        if(drinkTypeAndQty.length != 2){
            JsonObject returnObject = JSON_FACTORY.createObjectBuilder()
                    .add("Status:","Invalid Input! Must be DrinkType:Qty format")
                    .build();
            response.send(returnObject);
        }else{
            String drinkType=drinkTypeAndQty[0];
            String drinkQty=drinkTypeAndQty[1];
            String status = ControllerManageDrink.setDrinkQty(this.productDao, drinkType, drinkQty);
            JsonObject returnObject = JSON_FACTORY.createObjectBuilder()
                    .add("Status:", status)
                    .build();
            response.send(returnObject);
        }
    }

    private void viewDrinkPrice(ServerRequest request, ServerResponse response){
        LOGGER.info("start viewing drinks");
        String drinkName = request.path().toString().replace("/viewDrinkPrice/","");
        double price = ControllerManageDrink.queryDrinkPrice(this.productDao, drinkName);
        JsonObject returnObject = JSON_FACTORY.createObjectBuilder()
                .add("Drink Price:", price)
                .build();
        response.send(returnObject);
    }

    private void setDrinkPrice(ServerRequest request, ServerResponse response){
        LOGGER.info("start setting drinks price");
        String drinkParams = request.path().toString().replace("/setDrinkPrice/","");
        String[] drinkTypeAndPrice = drinkParams.split(":");
        if(drinkTypeAndPrice.length != 2){
            JsonObject returnObject = JSON_FACTORY.createObjectBuilder()
                    .add("Status:","Invalid Input! Must be DrinkType:Price format")
                    .build();
            response.send(returnObject);
        }else{
            String drinkType=drinkTypeAndPrice[0];
            String drinkPrice=drinkTypeAndPrice[1];
            String status = ControllerManageDrink.setDrinkPrice(this.productDao, drinkType, drinkPrice);
            JsonObject returnObject = JSON_FACTORY.createObjectBuilder()
                    .add("Status:", status)
                    .build();
            response.send(returnObject);
        }
    }
}
