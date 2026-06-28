package com.example.restaurantapp;

import java.util.List;

public class Order {
    private String id;
    private int tableNumber;
    private List<String> items;
    private double totalPrice;
    private String status;
    private long timestamp;
    private String customerId;

    public Order() {
    }

    public Order(String id, int tableNumber, List<String> items, double totalPrice, String status, long timestamp) {
        this.id = id;
        this.tableNumber = tableNumber;
        this.items = items;
        this.totalPrice = totalPrice;
        this.status = status;
        this.timestamp = timestamp;
        this.customerId = "";
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public int getTableNumber() { return tableNumber; }
    public void setTableNumber(int tableNumber) { this.tableNumber = tableNumber; }
    public List<String> getItems() { return items; }
    public void setItems(List<String> items) { this.items = items; }
    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
}
