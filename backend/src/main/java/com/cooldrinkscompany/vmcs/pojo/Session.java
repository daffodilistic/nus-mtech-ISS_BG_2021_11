package com.cooldrinkscompany.vmcs.pojo;

import java.util.ArrayList;
import java.util.UUID;

public final class Session {
    public transient int id;
    public UUID sessionId;
    public ArrayList<InsertCoin> coins;
    public ArrayList<Drink> drinks;
    public ArrayList<Object> objects;

    public Session() {
        // User should be able to start a session with no coins inserted
        // i.e. "just turned on" state
        this.coins = new ArrayList<InsertCoin>();
        this.sessionId = UUID.randomUUID();
    }

    public Session(InsertCoin coin) {
        this.id = -1;
        this.sessionId = UUID.randomUUID();
        coin.quantity += 1;
        this.coins = new ArrayList<InsertCoin>();
        this.coins.add(coin);
    }

    public void addCoin(InsertCoin coin) {
        boolean found = false;
        for (InsertCoin c : coins) {
            if (c.value == coin.value) {
                c.quantity += 1;
                found = true;
                break;
            }
        }
        if (!found) {
            coin.quantity += 1;
            this.coins.add(coin);
        }
    }

    public Session(Drink drink){
        this.id = -1;
        this.sessionId = UUID.randomUUID();
        this.drinks = new ArrayList<Drink>();
        this.drinks.add(drink);
    }

    public Session(Object obj){
        this.id = -1;
        this.sessionId = UUID.randomUUID();
        this.objects = new ArrayList<Object>();
        this.objects.add(obj);
    }
}
