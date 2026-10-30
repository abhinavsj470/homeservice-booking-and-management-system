package com.homeservice.model;

public enum Role {
    CUSTOMER,
    PROVIDER,
    ADMIN;

    public static Role fromString(String val) {
        if (val == null) return CUSTOMER;
        for (Role r : values()) {
            if (r.name().equalsIgnoreCase(val.trim())) return r;
        }
        return CUSTOMER;
    }
}
