package com.homeservice.service;

import com.homeservice.dao.BookingDAO;
import com.homeservice.dao.ServiceCategoryDAO;
import com.homeservice.dao.UserDAO;
import com.homeservice.model.Role;

import java.util.HashMap;
import java.util.Map;

public class StatisticsService {
    private final BookingDAO bookingDAO = new BookingDAO();
    private final UserDAO userDAO = new UserDAO();
    private final ServiceCategoryDAO categoryDAO = new ServiceCategoryDAO();

    public int getTotalBookings() {
        return bookingDAO.countTotalBookings();
    }

    public int getTotalCustomers() {
        return userDAO.countUsersByRole(Role.CUSTOMER);
    }

    public int getTotalProviders() {
        return userDAO.countUsersByRole(Role.PROVIDER);
    }

    public int getTotalCategories() {
        return categoryDAO.getAllCategories().size();
    }

    public double getTotalRevenue() {
        return bookingDAO.getTotalRevenue();
    }

    public Map<String, Integer> getBookingStatusDistribution() {
        Map<String, Integer> map = new HashMap<>();
        String[] statuses = {"PENDING", "ACCEPTED", "IN_PROGRESS", "COMPLETED", "CANCELLED", "REJECTED"};
        for (String s : statuses) {
            map.put(s, bookingDAO.countBookingsByStatus(s));
        }
        return map;
    }

    public double getProviderEarnings(int providerId) {
        return bookingDAO.getProviderEarnings(providerId);
    }
}
