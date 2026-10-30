package com.homeservice.dao;

import com.homeservice.config.DBConnection;
import com.homeservice.model.Review;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReviewDAO {

    public int createReview(Review r) {
        String sql = "INSERT INTO reviews (booking_id, customer_id, provider_id, rating, comment) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, r.getBookingId());
            ps.setInt(2, r.getCustomerId());
            ps.setInt(3, r.getProviderId());
            ps.setInt(4, r.getRating());
            ps.setString(5, r.getComment());
            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet gk = ps.getGeneratedKeys()) {
                    if (gk.next()) {
                        r.setId(gk.getInt(1));
                    }
                }
                // Update the provider's overall rating average and count
                new ServiceProviderDAO().recalculateRating(r.getProviderId());
                return r.getId();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public List<Review> getReviewsByProvider(int providerId) {
        List<Review> list = new ArrayList<>();
        String sql = "SELECT r.*, c.full_name AS customer_name, p.full_name AS provider_name " +
                     "FROM reviews r " +
                     "JOIN users c ON r.customer_id = c.id " +
                     "JOIN users p ON r.provider_id = p.id " +
                     "WHERE r.provider_id = ? " +
                     "ORDER BY r.created_at DESC";
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

    public List<Review> getReviewsByCustomer(int customerId) {
        List<Review> list = new ArrayList<>();
        String sql = "SELECT r.*, c.full_name AS customer_name, p.full_name AS provider_name " +
                     "FROM reviews r " +
                     "JOIN users c ON r.customer_id = c.id " +
                     "JOIN users p ON r.provider_id = p.id " +
                     "WHERE r.customer_id = ? " +
                     "ORDER BY r.created_at DESC";
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

    public List<Review> getAllReviews() {
        List<Review> list = new ArrayList<>();
        String sql = "SELECT r.*, c.full_name AS customer_name, p.full_name AS provider_name " +
                     "FROM reviews r " +
                     "JOIN users c ON r.customer_id = c.id " +
                     "JOIN users p ON r.provider_id = p.id " +
                     "ORDER BY r.created_at DESC";
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

    public boolean deleteReview(int reviewId) {
        int providerId = -1;
        String selectSql = "SELECT provider_id FROM reviews WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement psSel = conn.prepareStatement(selectSql)) {
            psSel.setInt(1, reviewId);
            try (ResultSet rs = psSel.executeQuery()) {
                if (rs.next()) providerId = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String sql = "DELETE FROM reviews WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, reviewId);
            boolean ok = ps.executeUpdate() > 0;
            if (ok && providerId > 0) {
                new ServiceProviderDAO().recalculateRating(providerId);
            }
            return ok;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private Review mapRow(ResultSet rs) throws SQLException {
        Review r = new Review();
        r.setId(rs.getInt("id"));
        r.setBookingId(rs.getInt("booking_id"));
        r.setCustomerId(rs.getInt("customer_id"));
        r.setCustomerName(rs.getString("customer_name"));
        r.setProviderId(rs.getInt("provider_id"));
        r.setProviderName(rs.getString("provider_name"));
        r.setRating(rs.getInt("rating"));
        r.setComment(rs.getString("comment"));
        r.setCreatedAt(rs.getTimestamp("created_at"));
        return r;
    }
}
