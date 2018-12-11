package com.example.jiaxinsun.myapplication;

import java.util.Random;

public class Item {

    private String itemName;
    private int quantity;
    private String id;
    private double price;

    private int priority;
    private int quantityBought;


    public Item() {
    }


    public Item(String itemName, int quantity, String id, double price, int priority, int quantityBought) {
        this.itemName = itemName;
        this.quantity = quantity;
        this.id = id;
        this.price = price;
        this.priority = priority;
        this.quantityBought = quantityBought;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getQuantityBought() {
        return quantityBought;
    }

    public void setQuantityBought(int quantityBought) {
        this.quantityBought = quantityBought;
    }
}
