package com.homeservice.dao;

import com.homeservice.config.DBConnection;
import com.homeservice.model.Booking;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookingDAO {

    public int createBooking(Booking b) {
        String sql = "INSERT INTO bookings (customer_id, provider_id, category_id, service_date, time_slot, address, problem_description, status, total_cost) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, b.getCustomerId());
            ps.setInt(2, b.getProviderId());
            ps.setInt(3, b.getCategoryId());
            ps.setDate(4, b.getServiceDate());
            ps.setString(5, b.getTimeSlot());
            ps.setString(6, b.getAddress());
            ps.setString(7, b.getProblemDescription());
            ps.setString(8, b.getStatus() != null ? b.getStatus() : "PENDING");
            ps.setDouble(9, b.getTotalCost());
            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet gk = ps.getGeneratedKeys()) {
                    if (gk.next()) {
                        b.setId(gk.getInt(1));
                        return b.getId();
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public Booking getBookingById(int id) {
        String sql = "SELECT b.*, " +
                     "c.full_name AS customer_name, c.phone AS customer_phone, c.email AS customer_email, " +
                     "p.full_name AS provider_name, p.phone AS provider_phone, " +
                     "cat.name AS category_name, " +
                     "(SELECT COUNT(*) FROM reviews r WHERE r.booking_id = b.id) AS has_review " +
                     "FROM bookings b " +
                     "JOIN users c ON b.customer_id = c.id " +
                     "JOIN users p ON b.provider_id = p.id " +
                     "JOIN service_categories cat ON b.category_id = cat.id " +
                     "WHERE b.id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Booking> getBookingsByCustomer(int customerId) {
        List<Booking> list = new ArrayList<>();
        String sql = "SELECT b.*, " +
                     "c.full_name AS customer_name, c.phone AS customer_phone, c.email AS customer_email, " +
                     "p.full_name AS provider_name, p.phone AS provider_phone, " +
                     "cat.name AS category_name, " +
                     "(SELECT COUNT(*) FROM reviews r WHERE r.booking_id = b.id) AS has_review " +
                     "FROM bookings b " +
                     "JOIN users c ON b.customer_id = c.id " +
                     "JOIN users p ON b.provider_id = p.id " +
                     "JOIN service_categories cat ON b.category_id = cat.id " +
                     "WHERE b.customer_id = ? " +
                     "ORDER BY b.service_date DESC, b.id DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Booking> getBookingsByProvider(int providerId) {
        List<Booking> list = new ArrayList<>();
        String sql = "SELECT b.*, " +
                     "c.full_name AS customer_name, c.phone AS customer_phone, c.email AS customer_email, " +
                     "p.full_name AS provider_name, p.phone AS provider_phone, " +
                     "cat.name AS category_name, " +
                     "(SELECT COUNT(*) FROM reviews r WHERE r.booking_id = b.id) AS has_review " +
                     "FROM bookings b " +
                     "JOIN users c ON b.customer_id = c.id " +
                     "JOIN users p ON b.provider_id = p.id " +
                     "JOIN service_categories cat ON b.category_id = cat.id " +
                     "WHERE b.provider_id = ? " +
                     "ORDER BY b.service_date DESC, b.id DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, providerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Booking> getAllBookings() {
        List<Booking> list = new ArrayList<>();
        String sql = "SELECT b.*, " +
                     "c.full_name AS customer_name, c.phone AS customer_phone, c.email AS customer_email, " +
                     "p.full_name AS provider_name, p.phone AS provider_phone, " +
                     "cat.name AS category_name, " +
                     "(SELECT COUNT(*) FROM reviews r WHERE r.booking_id = b.id) AS has_review " +
                     "FROM bookings b " +
                     "JOIN users c ON b.customer_id = c.id " +
                     "JOIN users p ON b.provider_id = p.id " +
                     "JOIN service_categories cat ON b.category_id = cat.id " +
                     "ORDER BY b.id DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean updateStatus(int bookingId, String newStatus) {
        String sql = "UPDATE bookings SET status = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setInt(2, bookingId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public int countTotalBookings() {
        String sql = "SELECT COUNT(*) FROM bookings";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int countBookingsByStatus(String status) {
        String sql = "SELECT COUNT(*) FROM bookings WHERE status = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public double getTotalRevenue() {
        String sql = "SELECT COALESCE(SUM(total_cost), 0) FROM bookings WHERE status = 'COMPLETED'";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    public double getProviderEarnings(int providerId) {
        String sql = "SELECT COALESCE(SUM(total_cost), 0) FROM bookings WHERE provider_id = ? AND status = 'COMPLETED'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, providerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    private Booking mapRow(ResultSet rs) throws SQLException {
        Booking b = new Booking();
        b.setId(rs.getInt("id"));
        b.setCustomerId(rs.getInt("customer_id"));
        b.setCustomerName(rs.getString("customer_name"));
        b.setCustomerPhone(rs.getString("customer_phone"));
        b.setCustomerEmail(rs.getString("customer_email"));
        b.setProviderId(rs.getInt("provider_id"));
        b.setProviderName(rs.getString("provider_name"));
        b.setProviderPhone(rs.getString("provider_phone"));
        b.setCategoryId(rs.getInt("category_id"));
        b.setCategoryName(rs.getString("category_name"));
        b.setServiceDate(rs.getDate("service_date"));
        b.setTimeSlot(rs.getString("time_slot"));
        b.setAddress(rs.getString("address"));
        b.setProblemDescription(rs.getString("problem_description"));
        b.setStatus(rs.getString("status"));
        b.setTotalCost(rs.getDouble("total_cost"));
        b.setCreatedAt(rs.getTimestamp("created_at"));
        b.setUpdatedAt(rs.getTimestamp("updated_at"));
        b.setHasReview(rs.getInt("has_review") > 0);
        return b;
    }
}
