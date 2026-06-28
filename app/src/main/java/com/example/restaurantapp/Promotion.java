package com.example.restaurantapp;

public class Promotion {
    private String id;
    private String title;
    private String code;
    private String discountType; // Percentage, Fixed
    private double discountValue;
    private double minOrderValue;
    private String expiryDate;
    private boolean active;
    private int usageCount;

    public Promotion() {
    }

    public Promotion(String id, String title, String code, String discountType, double discountValue, 
                     double minOrderValue, String expiryDate, boolean active, int usageCount) {
        this.id = id;
        this.title = title;
        this.code = code;
        this.discountType = discountType;
        this.discountValue = discountValue;
        this.minOrderValue = minOrderValue;
        this.expiryDate = expiryDate;
        this.active = active;
        this.usageCount = usageCount;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getDiscountType() { return discountType; }
    public void setDiscountType(String discountType) { this.discountType = discountType; }
    public double getDiscountValue() { return discountValue; }
    public void setDiscountValue(double discountValue) { this.discountValue = discountValue; }
    public double getMinOrderValue() { return minOrderValue; }
    public void setMinOrderValue(double minOrderValue) { this.minOrderValue = minOrderValue; }
    public String getExpiryDate() { return expiryDate; }
    public void setExpiryDate(String expiryDate) { this.expiryDate = expiryDate; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public int getUsageCount() { return usageCount; }
    public void setUsageCount(int usageCount) { this.usageCount = usageCount; }
}
