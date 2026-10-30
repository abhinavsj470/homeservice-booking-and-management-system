package com.homeservice.dao;

import com.homeservice.config.DBConnection;
import com.homeservice.model.ServiceCategory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceCategoryDAO {

    public List<ServiceCategory> getAllCategories() {
        List<ServiceCategory> list = new ArrayList<>();
        String sql = "SELECT * FROM service_categories ORDER BY name ASC";
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

    public ServiceCategory findById(int id) {
        String sql = "SELECT * FROM service_categories WHERE id = ?";
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

    public ServiceCategory findByName(String name) {
        String sql = "SELECT * FROM service_categories WHERE name = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name.trim());
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

    public int create(ServiceCategory cat) {
        String sql = "INSERT INTO service_categories (name, description, icon_name, base_price) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, cat.getName());
            ps.setString(2, cat.getDescription());
            ps.setString(3, cat.getIconName() != null ? cat.getIconName() : "wrench");
            ps.setDouble(4, cat.getBasePrice());
            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet gk = ps.getGeneratedKeys()) {
                    if (gk.next()) {
                        cat.setId(gk.getInt(1));
                        return cat.getId();
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public boolean update(ServiceCategory cat) {
        String sql = "UPDATE service_categories SET name = ?, description = ?, icon_name = ?, base_price = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cat.getName());
            ps.setString(2, cat.getDescription());
            ps.setString(3, cat.getIconName());
            ps.setDouble(4, cat.getBasePrice());
            ps.setInt(5, cat.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM service_categories WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private ServiceCategory mapRow(ResultSet rs) throws SQLException {
        ServiceCategory cat = new ServiceCategory();
        cat.setId(rs.getInt("id"));
        cat.setName(rs.getString("name"));
        cat.setDescription(rs.getString("description"));
        cat.setIconName(rs.getString("icon_name"));
        cat.setBasePrice(rs.getDouble("base_price"));
        cat.setCreatedAt(rs.getTimestamp("created_at"));
        return cat;
    }
}
