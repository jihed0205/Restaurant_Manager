package com.example.restaurantapp;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Customer {
    private String id;
    private String name;
    private String phone;
    private String email;
    private int totalVisits;
    private double totalSpent;
    private int loyaltyPoints;
    private String tier;
    private String joinDate;
    private String lastVisit;

    public Customer() {
    }

    public Customer(String id, String name, String phone, String email) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.totalVisits = 0;
        this.totalSpent = 0.0;
        this.loyaltyPoints = 0;
        this.tier = "Bronze";
        this.joinDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
        this.lastVisit = "N/A";
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public int getTotalVisits() { return totalVisits; }
    public void setTotalVisits(int totalVisits) { this.totalVisits = totalVisits; }
    public double getTotalSpent() { return totalSpent; }
    public void setTotalSpent(double totalSpent) { this.totalSpent = totalSpent; }
    public int getLoyaltyPoints() { return loyaltyPoints; }
    public void setLoyaltyPoints(int loyaltyPoints) { 
        this.loyaltyPoints = loyaltyPoints; 
        calculateTier();
    }
    public String getTier() { return tier; }
    public void setTier(String tier) { this.tier = tier; }
    public String getJoinDate() { return joinDate; }
    public void setJoinDate(String joinDate) { this.joinDate = joinDate; }
    public String getLastVisit() { return lastVisit; }
    public void setLastVisit(String lastVisit) { this.lastVisit = lastVisit; }

    private void calculateTier() {
        if (loyaltyPoints >= 600) tier = "Platinum";
        else if (loyaltyPoints >= 300) tier = "Gold";
        else if (loyaltyPoints >= 100) tier = "Silver";
        else tier = "Bronze";
    }
}
