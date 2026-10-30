package com.homeservice.dao;

import com.homeservice.config.DBConnection;
import com.homeservice.model.ServiceProviderProfile;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceProviderDAO {

    public ServiceProviderProfile findByUserId(int userId) {
        String sql = "SELECT p.*, u.full_name, u.email, u.phone, u.address, c.name AS category_name " +
                     "FROM provider_profiles p " +
                     "JOIN users u ON p.user_id = u.id " +
                     "JOIN service_categories c ON p.category_id = c.id " +
                     "WHERE p.user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
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

    public List<ServiceProviderProfile> getProvidersByCategory(int categoryId) {
        List<ServiceProviderProfile> list = new ArrayList<>();
        String sql = "SELECT p.*, u.full_name, u.email, u.phone, u.address, c.name AS category_name " +
                     "FROM provider_profiles p " +
                     "JOIN users u ON p.user_id = u.id " +
                     "JOIN service_categories c ON p.category_id = c.id " +
                     "WHERE p.category_id = ? AND u.status = 'ACTIVE' AND p.is_available = TRUE " +
                     "ORDER BY p.rating_avg DESC, p.experience_years DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, categoryId);
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

    public List<ServiceProviderProfile> getAllProviders() {
        List<ServiceProviderProfile> list = new ArrayList<>();
        String sql = "SELECT p.*, u.full_name, u.email, u.phone, u.address, c.name AS category_name " +
                     "FROM provider_profiles p " +
                     "JOIN users u ON p.user_id = u.id " +
                     "JOIN service_categories c ON p.category_id = c.id " +
                     "ORDER BY p.rating_avg DESC";
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

    public int createProfile(ServiceProviderProfile p) {
        String sql = "INSERT INTO provider_profiles (user_id, category_id, experience_years, hourly_rate, rating_avg, total_reviews, bio, is_available) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, p.getUserId());
            ps.setInt(2, p.getCategoryId());
            ps.setInt(3, p.getExperienceYears());
            ps.setDouble(4, p.getHourlyRate());
            ps.setDouble(5, p.getRatingAvg() > 0 ? p.getRatingAvg() : 5.0);
            ps.setInt(6, p.getTotalReviews());
            ps.setString(7, p.getBio());
            ps.setBoolean(8, p.isAvailable());
            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet gk = ps.getGeneratedKeys()) {
                    if (gk.next()) {
                        p.setId(gk.getInt(1));
                        return p.getId();
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public boolean updateProfile(ServiceProviderProfile p) {
        String sql = "UPDATE provider_profiles SET category_id = ?, experience_years = ?, hourly_rate = ?, bio = ?, is_available = ? WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, p.getCategoryId());
            ps.setInt(2, p.getExperienceYears());
            ps.setDouble(3, p.getHourlyRate());
            ps.setString(4, p.getBio());
            ps.setBoolean(5, p.isAvailable());
            ps.setInt(6, p.getUserId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateAvailability(int userId, boolean isAvailable) {
        String sql = "UPDATE provider_profiles SET is_available = ? WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, isAvailable);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void recalculateRating(int providerUserId) {
        String sql = "UPDATE provider_profiles p " +
                     "SET rating_avg = COALESCE((SELECT AVG(rating) FROM reviews WHERE provider_id = ?), 5.0), " +
                     "    total_reviews = (SELECT COUNT(*) FROM reviews WHERE provider_id = ?) " +
                     "WHERE p.user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, providerUserId);
            ps.setInt(2, providerUserId);
            ps.setInt(3, providerUserId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private ServiceProviderProfile mapRow(ResultSet rs) throws SQLException {
        ServiceProviderProfile p = new ServiceProviderProfile();
        p.setId(rs.getInt("id"));
        p.setUserId(rs.getInt("user_id"));
        p.setProviderName(rs.getString("full_name"));
        p.setProviderEmail(rs.getString("email"));
        p.setProviderPhone(rs.getString("phone"));
        p.setProviderAddress(rs.getString("address"));
        p.setCategoryId(rs.getInt("category_id"));
        p.setCategoryName(rs.getString("category_name"));
        p.setExperienceYears(rs.getInt("experience_years"));
        p.setHourlyRate(rs.getDouble("hourly_rate"));
        p.setRatingAvg(rs.getDouble("rating_avg"));
        p.setTotalReviews(rs.getInt("total_reviews"));
        p.setBio(rs.getString("bio"));
        p.setAvailable(rs.getBoolean("is_available"));
        p.setCreatedAt(rs.getTimestamp("created_at"));
        return p;
    }
}
