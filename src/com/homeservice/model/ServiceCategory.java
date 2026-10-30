package com.homeservice.model;

import java.sql.Timestamp;

public class ServiceCategory {
    private int id;
    private String name;
    private String description;
    private String iconName;
    private double basePrice;
    private Timestamp createdAt;

    public ServiceCategory() {}

    public ServiceCategory(int id, String name, String description, String iconName, double basePrice, Timestamp createdAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.iconName = iconName;
        this.basePrice = basePrice;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getIconName() { return iconName; }
    public void setIconName(String iconName) { this.iconName = iconName; }

    public double getBasePrice() { return basePrice; }
    public void setBasePrice(double basePrice) { this.basePrice = basePrice; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return name;
    }
}
