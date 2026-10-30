package com.homeservice;

import com.homeservice.config.DBConnection;
import com.homeservice.dao.*;
import com.homeservice.db.DatabaseInitializer;
import com.homeservice.model.*;
import com.homeservice.service.AuthService;
import com.homeservice.service.BookingService;
import com.homeservice.service.StatisticsService;

import java.sql.Connection;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class TestRunner {
    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println(" HOME SERVICE SYSTEM - AUTOMATED TEST SUITE");
        System.out.println("==================================================");

        int passed = 0;
        int failed = 0;

        // 1. Initialize Database Schema & Seed Data
        try {
            System.out.print("[TEST 1] Initializing Database & Seed Data... ");
            DatabaseInitializer.initialize();
            try (Connection c = DBConnection.getConnection()) {
                if (c != null && !c.isClosed()) {
                    System.out.println("PASSED (Database & Tables Ready)");
                    passed++;
                } else {
                    throw new RuntimeException("Null connection returned");
                }
            }
        } catch (Exception e) {
            System.out.println("FAILED: " + e.getMessage());
            failed++;
        }

        // 2. Test Category DAO
        ServiceCategoryDAO catDAO = new ServiceCategoryDAO();
        try {
            System.out.print("[TEST 2] Verifying Service Categories... ");
            List<ServiceCategory> categories = catDAO.getAllCategories();
            if (categories.size() >= 8) {
                System.out.println("PASSED (" + categories.size() + " categories loaded)");
                passed++;
            } else {
                throw new RuntimeException("Expected at least 8 categories, found: " + categories.size());
            }
        } catch (Exception e) {
            System.out.println("FAILED: " + e.getMessage());
            failed++;
        }

        // 3. Test Authentication Service - Admin, Provider, Customer
        AuthService authService = new AuthService();
        try {
            System.out.print("[TEST 3] Testing User Authentication & Login... ");
            User admin = authService.login("admin@homeservice.com", "admin123");
            User provider = authService.login("rohit.plumber@service.com", "provider123");
            User customer = authService.login("customer@example.com", "password123");
            if (customer == null) {
                customer = authService.login("customer@example.com", "123456789");
            }

            if (admin != null && admin.getRole() == Role.ADMIN &&
                provider != null && provider.getRole() == Role.PROVIDER &&
                customer != null && customer.getRole() == Role.CUSTOMER) {
                System.out.println("PASSED (All 3 Roles Authenticated)");
                passed++;
            } else {
                throw new RuntimeException("Login credentials check failed");
            }
        } catch (Exception e) {
            System.out.println("FAILED: " + e.getMessage());
            failed++;
        }

        // 4. Test New Customer & Provider Registration
        try {
            System.out.print("[TEST 4] Testing User Registration Workflow... ");
            String uniqueEmailCust = "test.cust." + System.currentTimeMillis() + "@test.com";
            User newCust = authService.registerCustomer("Test Cust", uniqueEmailCust, "pass123", "9111111111", "Test St 10");

            String uniqueEmailProv = "test.prov." + System.currentTimeMillis() + "@test.com";
            User newProv = authService.registerProvider("Test Prov", uniqueEmailProv, "pass123", "9222222222", "Test St 20", 1, 3, 400.0, "Test bio");

            if (newCust != null && newProv != null) {
                System.out.println("PASSED (New Customer ID: " + newCust.getId() + ", Provider ID: " + newProv.getId() + ")");
                passed++;
            } else {
                throw new RuntimeException("Registration failed");
            }
        } catch (Exception e) {
            System.out.println("FAILED: " + e.getMessage());
            failed++;
        }

        // 5. Test Booking Lifecycle: Create -> Accept -> In Progress -> Complete
        BookingService bookingService = new BookingService();
        BookingDAO bookingDAO = new BookingDAO();
        try {
            System.out.print("[TEST 5] Testing End-to-End Booking Lifecycle... ");
            User cust = authService.login("customer@example.com", "password123");
            if (cust == null) cust = authService.login("customer@example.com", "123456789");
            User prov = authService.login("rohit.plumber@service.com", "provider123");

            int bId = bookingService.createBooking(
                cust.getId(), prov.getId(), 1,
                Date.valueOf(LocalDate.now().plusDays(2)),
                "02:00 PM - 04:00 PM",
                cust.getAddress(),
                "Automated test leak repair",
                450.00
            );

            Booking b = bookingDAO.getBookingById(bId);
            if (!"PENDING".equals(b.getStatus())) throw new RuntimeException("Initial status not PENDING");

            bookingService.acceptBooking(bId);
            b = bookingDAO.getBookingById(bId);
            if (!"ACCEPTED".equals(b.getStatus())) throw new RuntimeException("Status not ACCEPTED");

            bookingService.startBooking(bId);
            b = bookingDAO.getBookingById(bId);
            if (!"IN_PROGRESS".equals(b.getStatus())) throw new RuntimeException("Status not IN_PROGRESS");

            bookingService.completeBooking(bId);
            b = bookingDAO.getBookingById(bId);
            if (!"COMPLETED".equals(b.getStatus())) throw new RuntimeException("Status not COMPLETED");

            System.out.println("PASSED (Booking #" + bId + " Lifecycle Verified)");
            passed++;

            // 6. Test Rating & Review Submission
            System.out.print("[TEST 6] Testing Customer Review Submission & Rating Calculation... ");
            ReviewDAO reviewDAO = new ReviewDAO();
            Review r = new Review();
            r.setBookingId(bId);
            r.setCustomerId(cust.getId());
            r.setProviderId(prov.getId());
            r.setRating(5);
            r.setComment("Automated test: outstanding plumbing fix!");
            int rId = reviewDAO.createReview(r);

            ServiceProviderDAO provDAO = new ServiceProviderDAO();
            ServiceProviderProfile prof = provDAO.findByUserId(prov.getId());

            if (rId > 0 && prof.getTotalReviews() >= 1) {
                System.out.println("PASSED (Review #" + rId + " Created, Provider Rating: " + prof.getRatingAvg() + ")");
                passed++;
            } else {
                throw new RuntimeException("Review submission or rating recalculation failed");
            }

        } catch (Exception e) {
            System.out.println("FAILED: " + e.getMessage());
            failed++;
        }

        // 7. Test Admin Analytics & System Statistics
        try {
            System.out.print("[TEST 7] Testing System Statistics & Analytics Service... ");
            StatisticsService stats = new StatisticsService();
            int totalBookings = stats.getTotalBookings();
            int customers = stats.getTotalCustomers();
            int providers = stats.getTotalProviders();
            double revenue = stats.getTotalRevenue();
            Map<String, Integer> dist = stats.getBookingStatusDistribution();

            if (totalBookings > 0 && customers > 0 && providers > 0 && dist.containsKey("COMPLETED")) {
                System.out.println("PASSED (Bookings: " + totalBookings + ", Revenue: ₹" + revenue + ")");
                passed++;
            } else {
                throw new RuntimeException("Invalid stats computed");
            }
        } catch (Exception e) {
            System.out.println("FAILED: " + e.getMessage());
            failed++;
        }

        // 8. Test Cloud Multi-Device Avatar & Profile About Sync
        try {
            System.out.print("[TEST 8] Testing Cloud Multi-Device Avatar & About Sync... ");
            UserDAO uDAO = new UserDAO();
            byte[] mockBytes = new byte[]{ (byte)0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A }; // PNG header magic
            boolean avatarSaved = uDAO.updateAvatar(1, mockBytes);
            byte[] retrieved = uDAO.getAvatar(1);
            boolean avatarCleaned = uDAO.updateAvatar(1, null);

            boolean aboutSaved = uDAO.updateAbout(1, "Cloud sync active on all devices");
            String retrievedAbout = uDAO.getAbout(1);

            if (avatarSaved && retrieved != null && retrieved.length == mockBytes.length 
                && avatarCleaned && aboutSaved && "Cloud sync active on all devices".equals(retrievedAbout)) {
                System.out.println("PASSED (MEDIUMBLOB & Cloud About Synchronized)");
                passed++;
            } else {
                throw new RuntimeException("Avatar or about sync mismatch in database");
            }
        } catch (Exception e) {
            System.out.println("FAILED: " + e.getMessage());
            failed++;
        }

        System.out.println("==================================================");
        System.out.println(" TEST RESULTS: " + passed + " PASSED, " + failed + " FAILED");
        System.out.println("==================================================");

        if (failed > 0) {
            System.exit(1);
        }
    }
}
