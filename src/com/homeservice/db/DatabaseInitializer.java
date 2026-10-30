package com.homeservice.db;

import com.homeservice.config.DBConnection;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {

    public static void initialize() {
        DBConnection.ensureDatabaseExists();
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            DatabaseMetaData meta = conn.getMetaData();
            boolean isPg = meta.getDatabaseProductName().toLowerCase().contains("postgres");

            if (isPg) {
                // PostgreSQL DDL (Supabase compatible)
                stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS users (" +
                    "  id SERIAL PRIMARY KEY," +
                    "  full_name VARCHAR(100) NOT NULL," +
                    "  email VARCHAR(100) NOT NULL UNIQUE," +
                    "  password VARCHAR(255) NOT NULL," +
                    "  phone VARCHAR(20)," +
                    "  role VARCHAR(20) NOT NULL CHECK (role IN ('CUSTOMER', 'PROVIDER', 'ADMIN'))," +
                    "  address TEXT," +
                    "  status VARCHAR(20) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE'))," +
                    "  avatar_data BYTEA," +
                    "  about_status VARCHAR(255) DEFAULT 'Hey there! I am using Home Service.'," +
                    "  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ");"
                );

                stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS service_categories (" +
                    "  id SERIAL PRIMARY KEY," +
                    "  name VARCHAR(100) NOT NULL UNIQUE," +
                    "  description TEXT," +
                    "  icon_name VARCHAR(50) DEFAULT 'wrench'," +
                    "  base_price DECIMAL(10, 2) DEFAULT 0.00," +
                    "  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ");"
                );

                stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS provider_profiles (" +
                    "  id SERIAL PRIMARY KEY," +
                    "  user_id INT NOT NULL UNIQUE," +
                    "  category_id INT NOT NULL," +
                    "  experience_years INT DEFAULT 1," +
                    "  hourly_rate DECIMAL(10, 2) NOT NULL," +
                    "  rating_avg DECIMAL(3, 2) DEFAULT 5.00," +
                    "  total_reviews INT DEFAULT 0," +
                    "  bio TEXT," +
                    "  is_available BOOLEAN DEFAULT TRUE," +
                    "  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE," +
                    "  FOREIGN KEY (category_id) REFERENCES service_categories(id) ON DELETE RESTRICT" +
                    ");"
                );

                stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS bookings (" +
                    "  id SERIAL PRIMARY KEY," +
                    "  customer_id INT NOT NULL," +
                    "  provider_id INT NOT NULL," +
                    "  category_id INT NOT NULL," +
                    "  service_date DATE NOT NULL," +
                    "  time_slot VARCHAR(50) NOT NULL," +
                    "  address TEXT NOT NULL," +
                    "  problem_description TEXT NOT NULL," +
                    "  status VARCHAR(30) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'ACCEPTED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED', 'REJECTED'))," +
                    "  total_cost DECIMAL(10, 2) NOT NULL," +
                    "  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "  FOREIGN KEY (customer_id) REFERENCES users(id) ON DELETE CASCADE," +
                    "  FOREIGN KEY (provider_id) REFERENCES users(id) ON DELETE CASCADE," +
                    "  FOREIGN KEY (category_id) REFERENCES service_categories(id) ON DELETE RESTRICT" +
                    ");"
                );

                stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS reviews (" +
                    "  id SERIAL PRIMARY KEY," +
                    "  booking_id INT NOT NULL UNIQUE," +
                    "  customer_id INT NOT NULL," +
                    "  provider_id INT NOT NULL," +
                    "  rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5)," +
                    "  comment TEXT," +
                    "  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "  FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE," +
                    "  FOREIGN KEY (customer_id) REFERENCES users(id) ON DELETE CASCADE," +
                    "  FOREIGN KEY (provider_id) REFERENCES users(id) ON DELETE CASCADE" +
                    ");"
                );

                try {
                    stmt.executeUpdate("ALTER TABLE users ADD COLUMN IF NOT EXISTS avatar_data BYTEA;");
                } catch (SQLException ignored) {}
                try {
                    stmt.executeUpdate("ALTER TABLE users ADD COLUMN IF NOT EXISTS about_status VARCHAR(255) DEFAULT 'Hey there! I am using Home Service.';");
                } catch (SQLException ignored) {}

            } else {
                // MySQL DDL (Aiven / Local MySQL compatible)
                stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS users (" +
                    "  id INT AUTO_INCREMENT PRIMARY KEY," +
                    "  full_name VARCHAR(100) NOT NULL," +
                    "  email VARCHAR(100) NOT NULL UNIQUE," +
                    "  password VARCHAR(255) NOT NULL," +
                    "  phone VARCHAR(20)," +
                    "  role ENUM('CUSTOMER', 'PROVIDER', 'ADMIN') NOT NULL," +
                    "  address TEXT," +
                    "  status ENUM('ACTIVE', 'INACTIVE') DEFAULT 'ACTIVE'," +
                    "  avatar_data MEDIUMBLOB," +
                    "  about_status VARCHAR(255) DEFAULT 'Hey there! I am using Home Service.'," +
                    "  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;"
                );

                stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS service_categories (" +
                    "  id INT AUTO_INCREMENT PRIMARY KEY," +
                    "  name VARCHAR(100) NOT NULL UNIQUE," +
                    "  description TEXT," +
                    "  icon_name VARCHAR(50) DEFAULT 'wrench'," +
                    "  base_price DECIMAL(10, 2) DEFAULT 0.00," +
                    "  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;"
                );

                stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS provider_profiles (" +
                    "  id INT AUTO_INCREMENT PRIMARY KEY," +
                    "  user_id INT NOT NULL UNIQUE," +
                    "  category_id INT NOT NULL," +
                    "  experience_years INT DEFAULT 1," +
                    "  hourly_rate DECIMAL(10, 2) NOT NULL," +
                    "  rating_avg DECIMAL(3, 2) DEFAULT 5.00," +
                    "  total_reviews INT DEFAULT 0," +
                    "  bio TEXT," +
                    "  is_available BOOLEAN DEFAULT TRUE," +
                    "  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE," +
                    "  FOREIGN KEY (category_id) REFERENCES service_categories(id) ON DELETE RESTRICT" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;"
                );

                stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS bookings (" +
                    "  id INT AUTO_INCREMENT PRIMARY KEY," +
                    "  customer_id INT NOT NULL," +
                    "  provider_id INT NOT NULL," +
                    "  category_id INT NOT NULL," +
                    "  service_date DATE NOT NULL," +
                    "  time_slot VARCHAR(50) NOT NULL," +
                    "  address TEXT NOT NULL," +
                    "  problem_description TEXT NOT NULL," +
                    "  status ENUM('PENDING', 'ACCEPTED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED', 'REJECTED') DEFAULT 'PENDING'," +
                    "  total_cost DECIMAL(10, 2) NOT NULL," +
                    "  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                    "  FOREIGN KEY (customer_id) REFERENCES users(id) ON DELETE CASCADE," +
                    "  FOREIGN KEY (provider_id) REFERENCES users(id) ON DELETE CASCADE," +
                    "  FOREIGN KEY (category_id) REFERENCES service_categories(id) ON DELETE RESTRICT" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;"
                );

                stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS reviews (" +
                    "  id INT AUTO_INCREMENT PRIMARY KEY," +
                    "  booking_id INT NOT NULL UNIQUE," +
                    "  customer_id INT NOT NULL," +
                    "  provider_id INT NOT NULL," +
                    "  rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5)," +
                    "  comment TEXT," +
                    "  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "  FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE," +
                    "  FOREIGN KEY (customer_id) REFERENCES users(id) ON DELETE CASCADE," +
                    "  FOREIGN KEY (provider_id) REFERENCES users(id) ON DELETE CASCADE" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;"
                );

                try {
                    stmt.executeUpdate("ALTER TABLE users ADD COLUMN avatar_data MEDIUMBLOB;");
                } catch (SQLException ignored) {}
                try {
                    stmt.executeUpdate("ALTER TABLE users ADD COLUMN about_status VARCHAR(255) DEFAULT 'Hey there! I am using Home Service.';");
                } catch (SQLException ignored) {}
            }

            seedInitialData(conn, isPg);
            System.out.println("Database schema initialized and seed data verified successfully on " + (isPg ? "PostgreSQL (Supabase)" : "MySQL") + ".");

        } catch (SQLException e) {
            System.err.println("Database initialization error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void seedInitialData(Connection conn, boolean isPg) throws SQLException {
        // 1. Seed Categories if empty
        try (Statement s = conn.createStatement();
             ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM service_categories")) {
            if (rs.next() && rs.getInt(1) == 0) {
                String sql = "INSERT INTO service_categories (name, description, icon_name, base_price) VALUES (?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    Object[][] categories = {
                        {"Plumbing", "Professional pipe repair, leak fixing, fixture install & drainage cleaning", "wrench", 499.00},
                        {"Electrical Work", "Wiring, short circuit fixes, switches, fans, lights & panel maintenance", "zap", 449.00},
                        {"Cleaning", "Full house deep cleaning, kitchen & bathroom sanitization, sofa cleaning", "sparkles", 799.00},
                        {"Carpentry", "Furniture repair, lock assembly, door alignments & custom wood fittings", "hammer", 599.00},
                        {"Painting", "Interior & exterior wall painting, waterproof priming & touch-ups", "paint-roller", 1299.00},
                        {"Appliance Repair", "Refrigerators, washing machines, microwaves, water purifiers & ovens", "tv", 549.00},
                        {"AC Servicing", "Filter cleaning, gas recharge, cooling efficiency check & installation", "wind", 699.00},
                        {"Handyman", "Drill & hang work, door latch repair, curtain rod fitting, furniture assembly & general handyman repairs", "tool", 399.00}
                    };
                    for (Object[] cat : categories) {
                        ps.setString(1, (String) cat[0]);
                        ps.setString(2, (String) cat[1]);
                        ps.setString(3, (String) cat[2]);
                        ps.setDouble(4, (Double) cat[3]);
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
            }
        }

        // Ensure "Handyman" category exists and is synced
        try {
            boolean hasHandyman = false;
            try (Statement s = conn.createStatement();
                 ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM service_categories WHERE name='Handyman'")) {
                if (rs.next() && rs.getInt(1) > 0) {
                    hasHandyman = true;
                }
            }

            if (!hasHandyman) {
                int updated = 0;
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE service_categories SET name='Handyman', description=?, icon_name='tool', base_price=399.00 WHERE name='Household Maintenance'")) {
                    ps.setString(1, "Drill & hang work, door latch repair, curtain rod fitting, furniture assembly & general handyman repairs");
                    updated = ps.executeUpdate();
                }

                if (updated == 0) {
                    try (PreparedStatement ps = conn.prepareStatement(
                            "INSERT INTO service_categories (name, description, icon_name, base_price) VALUES ('Handyman', ?, 'tool', 399.00)")) {
                        ps.setString(1, "Drill & hang work, door latch repair, curtain rod fitting, furniture assembly & general handyman repairs");
                        ps.executeUpdate();
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Note on Handyman category sync: " + e.getMessage());
        }

        // 2. Seed Admin user if empty
        try (Statement s = conn.createStatement();
             ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM users WHERE email='admin@homeservice.com'")) {
            if (rs.next() && rs.getInt(1) == 0) {
                String sql = "INSERT INTO users (full_name, email, password, phone, role, address) VALUES (?, ?, ?, ?, 'ADMIN', ?)";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, "System Administrator");
                    ps.setString(2, "admin@homeservice.com");
                    ps.setString(3, "admin123");
                    ps.setString(4, "9000000000");
                    ps.setString(5, "Central Administration HQ");
                    ps.executeUpdate();
                }
            }
        }

        // 3. Seed Sample Service Providers for each category
        Object[][] providers = {
            {"Rohit Verma", "rohit.plumber@service.com", "provider123", "9876500001", "Koramangala, Bangalore", "Plumbing", 6, 450.00, "Licensed master plumber with 6+ years specializing in pipe leaks and kitchen fixtures."},
            {"Arun Kumar", "arun.electrician@service.com", "provider123", "9876500002", "Indiranagar, Bangalore", "Electrical Work", 8, 500.00, "Certified electrical technician experienced in domestic rewiring, panels, and invertors."},
            {"Priya Sharma", "priya.cleaner@service.com", "provider123", "9876500003", "HSR Layout, Bangalore", "Cleaning", 4, 350.00, "Deep hygiene specialist equipped with eco-friendly cleaning tools and steam disinfection."},
            {"Suresh Nair", "suresh.carpenter@service.com", "provider123", "9876500004", "Whitefield, Bangalore", "Carpentry", 10, 550.00, "Skilled artisan with 10 years experience in modular furniture, custom shelves and door fittings."},
            {"Deepak Mishra", "deepak.painter@service.com", "provider123", "9876500006", "JP Nagar, Bangalore", "Painting", 5, 500.00, "Expert residential painter skilled in waterproof coating, smooth finish, and spray priming."},
            {"Manoj Patil", "manoj.repair@service.com", "provider123", "9876500007", "BTM Layout, Bangalore", "Appliance Repair", 7, 520.00, "Authorized technician for refrigerators, washing machines, microwaves, and purifiers."},
            {"Vikram Singh", "vikram.ac@service.com", "provider123", "9876500005", "Marathahalli, Bangalore", "AC Servicing", 7, 600.00, "Specialist in all HVAC split/window units, jet servicing and compressor diagnostics."},
            {"Rajesh Gupta", "rajesh.handyman@service.com", "provider123", "9876500008", "Electronic City, Bangalore", "Handyman", 6, 400.00, "Handyman pro for drilling, wall mount installations, lock replacements, and general repairs."}
        };

        for (Object[] p : providers) {
            int userId = -1;
            try (PreparedStatement check = conn.prepareStatement("SELECT id FROM users WHERE email=?")) {
                check.setString(1, (String) p[1]);
                try (ResultSet rsc = check.executeQuery()) {
                    if (rsc.next()) {
                        userId = rsc.getInt(1);
                    }
                }
            }

            if (userId < 0) {
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO users (full_name, email, password, phone, role, address) VALUES (?, ?, ?, ?, 'PROVIDER', ?)",
                        Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, (String) p[0]);
                    ps.setString(2, (String) p[1]);
                    ps.setString(3, (String) p[2]);
                    ps.setString(4, (String) p[3]);
                    ps.setString(5, (String) p[4]);
                    ps.executeUpdate();
                    try (ResultSet gk = ps.getGeneratedKeys()) {
                        if (gk.next()) userId = gk.getInt(1);
                    }
                }
            }

            // find category id
            int catId = 1;
            try (PreparedStatement psCat = conn.prepareStatement("SELECT id FROM service_categories WHERE name=?")) {
                psCat.setString(1, (String) p[5]);
                try (ResultSet rsc = psCat.executeQuery()) {
                    if (rsc.next()) catId = rsc.getInt(1);
                }
            }

            // check if provider profile exists
            if (userId > 0) {
                boolean hasProfile = false;
                try (PreparedStatement checkProf = conn.prepareStatement("SELECT id FROM provider_profiles WHERE user_id=?")) {
                    checkProf.setInt(1, userId);
                    try (ResultSet rsp = checkProf.executeQuery()) {
                        hasProfile = rsp.next();
                    }
                }
                if (!hasProfile) {
                    try (PreparedStatement psProf = conn.prepareStatement(
                            "INSERT INTO provider_profiles (user_id, category_id, experience_years, hourly_rate, rating_avg, total_reviews, bio, is_available) VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {
                        psProf.setInt(1, userId);
                        psProf.setInt(2, catId);
                        psProf.setInt(3, (Integer) p[6]);
                        psProf.setDouble(4, (Double) p[7]);
                        psProf.setDouble(5, 4.9);
                        psProf.setInt(6, 1);
                        psProf.setString(7, (String) p[8]);
                        psProf.setBoolean(8, true);
                        psProf.executeUpdate();
                    }
                } else {
                    try (PreparedStatement psUpCat = conn.prepareStatement(
                            "UPDATE provider_profiles SET category_id=? WHERE user_id=? AND category_id != ?")) {
                        psUpCat.setInt(1, catId);
                        psUpCat.setInt(2, userId);
                        psUpCat.setInt(3, catId);
                        psUpCat.executeUpdate();
                    }
                }
            }
        }

        // 4. Seed Sample Customer if empty
        try (Statement s = conn.createStatement();
             ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM users WHERE role='CUSTOMER'")) {
            if (rs.next() && rs.getInt(1) == 0) {
                int custId = -1;
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO users (full_name, email, password, phone, role, address) VALUES (?, ?, ?, ?, 'CUSTOMER', ?)",
                        Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, "Abhinav Customer");
                    ps.setString(2, "customer@example.com");
                    ps.setString(3, "password123");
                    ps.setString(4, "9876543210");
                    ps.setString(5, "Flat 402, Sunshine Heights, Outer Ring Road, Bangalore");
                    ps.executeUpdate();
                    try (ResultSet gk = ps.getGeneratedKeys()) {
                        if (gk.next()) custId = gk.getInt(1);
                    }
                }

                // Create a couple sample bookings for demonstration
                if (custId > 0) {
                    int rohitId = -1;
                    try (PreparedStatement ps = conn.prepareStatement("SELECT id FROM users WHERE email='rohit.plumber@service.com'")) {
                        try (ResultSet r = ps.executeQuery()) {
                            if (r.next()) rohitId = r.getInt(1);
                        }
                    }
                    int arunId = -1;
                    try (PreparedStatement ps = conn.prepareStatement("SELECT id FROM users WHERE email='arun.electrician@service.com'")) {
                        try (ResultSet r = ps.executeQuery()) {
                            if (r.next()) arunId = r.getInt(1);
                        }
                    }

                    String dateMinus2 = isPg ? "CURRENT_DATE - INTERVAL '2 days'" : "CURDATE() - INTERVAL 2 DAY";
                    String datePlus1 = isPg ? "CURRENT_DATE + INTERVAL '1 day'" : "CURDATE() + INTERVAL 1 DAY";
                    String dateNow = isPg ? "CURRENT_DATE" : "CURDATE()";

                    if (rohitId > 0) {
                        int bookingId = -1;
                        try (PreparedStatement psb = conn.prepareStatement(
                                "INSERT INTO bookings (customer_id, provider_id, category_id, service_date, time_slot, address, problem_description, status, total_cost) " +
                                "VALUES (?, ?, 1, " + dateMinus2 + ", '11:00 AM - 01:00 PM', 'Flat 402, Sunshine Heights, Bangalore', 'Kitchen sink pipe leaking and water tap valve loose', 'COMPLETED', 450.00)",
                                Statement.RETURN_GENERATED_KEYS)) {
                            psb.setInt(1, custId);
                            psb.setInt(2, rohitId);
                            psb.executeUpdate();
                            try (ResultSet gk = psb.getGeneratedKeys()) {
                                if (gk.next()) bookingId = gk.getInt(1);
                            }
                        }

                        if (bookingId > 0) {
                            try (PreparedStatement psr = conn.prepareStatement(
                                    "INSERT INTO reviews (booking_id, customer_id, provider_id, rating, comment) VALUES (?, ?, ?, 5, ?)")) {
                                psr.setInt(1, bookingId);
                                psr.setInt(2, custId);
                                psr.setInt(3, rohitId);
                                psr.setString(4, "Excellent plumbing service! Rohit arrived on time, was polite, and fixed the kitchen leak quickly.");
                                psr.executeUpdate();
                            }
                        }

                        // Also add an upcoming pending booking
                        try (PreparedStatement psb = conn.prepareStatement(
                                "INSERT INTO bookings (customer_id, provider_id, category_id, service_date, time_slot, address, problem_description, status, total_cost) " +
                                "VALUES (?, ?, 1, " + datePlus1 + ", '02:00 PM - 04:00 PM', 'Flat 402, Sunshine Heights, Bangalore', 'Bathroom shower mixer replacement', 'PENDING', 450.00)")) {
                            psb.setInt(1, custId);
                            psb.setInt(2, rohitId);
                            psb.executeUpdate();
                        }
                    }

                    if (arunId > 0) {
                        // Add an in-progress booking
                        try (PreparedStatement psb = conn.prepareStatement(
                                "INSERT INTO bookings (customer_id, provider_id, category_id, service_date, time_slot, address, problem_description, status, total_cost) " +
                                "VALUES (?, ?, 2, " + dateNow + ", '09:00 AM - 11:00 AM', 'Flat 402, Sunshine Heights, Bangalore', 'Living room ceiling fan spark and circuit breaker tripping', 'IN_PROGRESS', 500.00)")) {
                            psb.setInt(1, custId);
                            psb.setInt(2, arunId);
                            psb.executeUpdate();
                        }
                    }
                }
            }
        }
    }
}
