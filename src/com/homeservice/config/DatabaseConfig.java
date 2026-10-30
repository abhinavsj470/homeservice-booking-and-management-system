package com.homeservice.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class DatabaseConfig {
    private static final String CONFIG_FILE = "db.properties";
    private static Properties properties = new Properties();

    private static String host = "aws-0-ap-northeast-1.pooler.supabase.com";
    private static int port = 5432;
    private static String dbName = "postgres";
    private static String user = "postgres.naosqqmsqmfmztrcxtep";
    private static String password = "Homeservice21234£";
    private static boolean ssl = true;

    static {
        loadConfig();
    }

    public static boolean isPostgres() {
        return port == 5432 || port == 6543 || (host != null && (host.contains("supabase") || host.contains("postgres")));
    }

    public static File getConfigFile() {
        // 1. Current working directory
        File local = new File(CONFIG_FILE);
        if (local.exists()) {
            return local;
        }

        // 2. Directory where the application JAR / EXE is located
        try {
            File codeSource = new File(
                    DatabaseConfig.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            File parentDir = codeSource.isDirectory() ? codeSource : codeSource.getParentFile();
            if (parentDir != null) {
                File nextToJar = new File(parentDir, CONFIG_FILE);
                if (nextToJar.exists())
                    return nextToJar;

                File parentOfApp = parentDir.getParentFile();
                if (parentOfApp != null) {
                    File nextToExe = new File(parentOfApp, CONFIG_FILE);
                    if (nextToExe.exists())
                        return nextToExe;
                }
            }
        } catch (Exception ignored) {
        }

        return local;
    }

    public static void loadConfig() {
        File file = getConfigFile();
        if (file.exists()) {
            try (java.io.InputStreamReader isr = new java.io.InputStreamReader(new FileInputStream(file), java.nio.charset.StandardCharsets.UTF_8)) {
                properties.load(isr);
                host = properties.getProperty("db.host", host);
                port = Integer.parseInt(properties.getProperty("db.port", String.valueOf(port)));
                dbName = properties.getProperty("db.name", dbName);
                user = properties.getProperty("db.user", user);
                password = properties.getProperty("db.password", password);
                ssl = Boolean.parseBoolean(properties.getProperty("db.ssl", "true"));
            } catch (IOException e) {
                System.err.println("Warning: Could not load " + file.getAbsolutePath() + ", using defaults.");
            }
        } else {
            saveDefaultConfig();
        }
    }

    private static void saveDefaultConfig() {
        properties.setProperty("db.host", host);
        properties.setProperty("db.port", String.valueOf(port));
        properties.setProperty("db.name", dbName);
        properties.setProperty("db.user", user);
        properties.setProperty("db.password", password);
        properties.setProperty("db.ssl", String.valueOf(ssl));
        try (java.io.OutputStreamWriter osw = new java.io.OutputStreamWriter(new FileOutputStream(getConfigFile()), java.nio.charset.StandardCharsets.UTF_8)) {
            properties.store(osw, "Home Service Booking and Management System - Database Configuration");
        } catch (IOException e) {
            // Ignore error writing default config
        }
    }

    public static void updateAndSaveConfig(String newHost, int newPort, String newDbName, String newUser,
            String newPassword, boolean newSsl) {
        host = newHost != null ? newHost.trim() : "localhost";
        port = newPort;
        dbName = newDbName != null ? newDbName.trim() : "postgres";
        user = newUser != null ? newUser.trim() : "postgres";
        password = newPassword != null ? newPassword : "";
        ssl = newSsl;

        properties.setProperty("db.host", host);
        properties.setProperty("db.port", String.valueOf(port));
        properties.setProperty("db.name", dbName);
        properties.setProperty("db.user", user);
        properties.setProperty("db.password", password);
        properties.setProperty("db.ssl", String.valueOf(ssl));

        try (java.io.OutputStreamWriter osw = new java.io.OutputStreamWriter(new FileOutputStream(getConfigFile()), java.nio.charset.StandardCharsets.UTF_8)) {
            properties.store(osw, "Home Service Booking and Management System - Database Configuration");
        } catch (IOException e) {
            System.err.println("Error saving updated config: " + e.getMessage());
        }

        // Close any cached connection so new config is used immediately
        DBConnection.closeConnection();
    }

    public static String getHost() {
        return host;
    }

    public static int getPort() {
        return port;
    }

    public static String getDbName() {
        return dbName;
    }

    public static String getUser() {
        return user;
    }

    public static String getPassword() {
        return password;
    }

    public static boolean isSsl() {
        return ssl;
    }

    public static String getSslParameters() {
        boolean isRemote = host != null && !host.equalsIgnoreCase("localhost") && !host.equals("127.0.0.1");
        if (ssl || isRemote) {
            return "sslMode=REQUIRED&allowPublicKeyRetrieval=true";
        } else {
            return "sslMode=DISABLED&allowPublicKeyRetrieval=true";
        }
    }

    public static String getBaseUrl() {
        if (isPostgres()) {
            return "jdbc:postgresql://" + host + ":" + port + "/postgres?sslmode=" + (ssl ? "require" : "prefer")
                    + "&prepareThreshold=0&tcpKeepAlive=true";
        }
        return "jdbc:mysql://" + host + ":" + port + "/?" + getSslParameters()
                + "&serverTimezone=UTC&connectTimeout=15000&socketTimeout=30000";
    }

    public static String getDatabaseUrl() {
        if (isPostgres()) {
            return "jdbc:postgresql://" + host + ":" + port + "/" + dbName + "?sslmode=" + (ssl ? "require" : "prefer")
                    + "&prepareThreshold=0&tcpKeepAlive=true";
        }
        return "jdbc:mysql://" + host + ":" + port + "/" + dbName + "?" + getSslParameters()
                + "&serverTimezone=UTC&connectTimeout=15000&socketTimeout=30000";
    }
}
