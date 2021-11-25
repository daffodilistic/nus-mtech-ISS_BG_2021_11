package com.cooldrinkscompany.vmcs.pojo;


import java.util.List;

import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.json.Json;

import javax.json.JsonBuilderFactory;
import javax.json.JsonObject;

import io.helidon.common.reactive.Single;
import io.helidon.dbclient.DbClient;


public class ProductDAOImpl implements ProductDAO {

    private static final Logger LOGGER = Logger.getLogger(ProductDAOImpl.class.getName());
    private static final JsonBuilderFactory JSON_FACTORY = Json.createBuilderFactory(Collections.emptyMap());
    public final String createDrinksTable = "CREATE TABLE public.drinks (id SERIAL PRIMARY KEY, name VARCHAR NULL, price FLOAT, quantity INT)";
    public final String createCoinsTable = "CREATE TABLE public.coins (id SERIAL PRIMARY KEY, name VARCHAR NULL, denomination FLOAT, quantity INT)";
    public final String getCoinsQty = "SELECT quantity FROM coins where name = '%s'";
    private final DbClient dbClient;

    public ProductDAOImpl(DbClient dbClient) {
        this.dbClient = dbClient;

         // MySQL init Drinks Table
         dbClient.execute(handle -> handle
         .createInsert(createDrinksTable).execute())
         .thenAccept(System.out::println).exceptionally(throwable -> {
             LOGGER.log(Level.WARNING, "Failed to create drinks table, maybe it already exists?", throwable);
             return null;
         });

        // MySQL init Coins Table
        dbClient.execute(handle -> handle
                .createInsert(createCoinsTable).execute())
                .thenAccept(System.out::println).exceptionally(throwable -> {
            LOGGER.log(Level.WARNING, "Failed to create coins table, maybe it already exists?", throwable);
            return null;
        });
    }

    public DbClient getDbClient() {
        return dbClient;
    }

    public String getQuantity(String name){
        try {
            String sql = String.format(this.getCoinsQty,name);
            LOGGER.info("getQty full sql is: " + sql);
            Single<List<JsonObject>> rows = dbClient.execute(exec -> exec.createQuery(sql).execute())
                    .map(it -> it.as(JsonObject.class)).collectList();
            JsonObject result = rows.get().get(0);
            return result.getJsonNumber("quantity").toString();
        }catch (Exception e){
            LOGGER.info(e.toString());
            return "NA";
        }
    }

//    public List<Product> getAllProduct(){
//        return null;
//    }
//
//    public Product getProduct(int ProductId){
//        return null;
//
//    }
//
//    public void addProduct( Product product){
//
//    }
//
//    public void updateProduct( Product product){
//
//    }
//
//    public void deleteProduct( int ProductId){
//
//    }

}    