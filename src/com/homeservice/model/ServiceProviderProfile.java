package com.homeservice.model;

import java.sql.Timestamp;

public class ServiceProviderProfile {
    private int id;
    private int userId;
    private String providerName;
    private String providerEmail;
    private String providerPhone;
    private String providerAddress;
    private int categoryId;
    private String categoryName;
    private int experienceYears;
    private double hourlyRate;
    private double ratingAvg;
    private int totalReviews;
    private String bio;
    private boolean isAvailable;
    private Timestamp createdAt;

    public ServiceProviderProfile() {
        this.isAvailable = true;
    }

    public ServiceProviderProfile(int id, int userId, int categoryId, int experienceYears, double hourlyRate, double ratingAvg, int totalReviews, String bio, boolean isAvailable, Timestamp createdAt) {
        this.id = id;
        this.userId = userId;
        this.categoryId = categoryId;
        this.experienceYears = experienceYears;
        this.hourlyRate = hourlyRate;
        this.ratingAvg = ratingAvg;
        this.totalReviews = totalReviews;
        this.bio = bio;
        this.isAvailable = isAvailable;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getProviderName() { return providerName; }
    public void setProviderName(String providerName) { this.providerName = providerName; }

    public String getProviderEmail() { return providerEmail; }
    public void setProviderEmail(String providerEmail) { this.providerEmail = providerEmail; }

    public String getProviderPhone() { return providerPhone; }
    public void setProviderPhone(String providerPhone) { this.providerPhone = providerPhone; }

    public String getProviderAddress() { return providerAddress; }
    public void setProviderAddress(String providerAddress) { this.providerAddress = providerAddress; }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public int getExperienceYears() { return experienceYears; }
    public void setExperienceYears(int experienceYears) { this.experienceYears = experienceYears; }

    public double getHourlyRate() { return hourlyRate; }
    public void setHourlyRate(double hourlyRate) { this.hourlyRate = hourlyRate; }

    public double getRatingAvg() { return ratingAvg; }
    public void setRatingAvg(double ratingAvg) { this.ratingAvg = ratingAvg; }

    public int getTotalReviews() { return totalReviews; }
    public void setTotalReviews(int totalReviews) { this.totalReviews = totalReviews; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean available) { isAvailable = available; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
