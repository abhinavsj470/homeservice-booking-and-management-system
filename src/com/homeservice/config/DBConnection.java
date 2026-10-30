package com.homeservice.config;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DBConnection {
    private static Connection physicalConnection = null;

    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException ignored) {
        }
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException ignored) {
        }
    }

    public static synchronized Connection getConnection() throws SQLException {
        if (physicalConnection == null || physicalConnection.isClosed() || !isConnectionAlive()) {
            ensureDatabaseExists();
            if (physicalConnection != null && !physicalConnection.isClosed()) {
                try { physicalConnection.close(); } catch (Exception ignored) {}
            }
            physicalConnection = DriverManager.getConnection(
                    DatabaseConfig.getDatabaseUrl(),
                    DatabaseConfig.getUser(),
                    DatabaseConfig.getPassword());
        }

        // Return a proxy that ignores close() so DAOs with try-with-resources
        // keep the physical authenticated SSL connection alive and reuse it.
        return (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class<?>[]{Connection.class},
                (proxy, method, args) -> {
                    if ("close".equals(method.getName())) {
                        return null; // Keep physical connection open!
                    }
                    if ("isClosed".equals(method.getName())) {
                        return physicalConnection == null || physicalConnection.isClosed();
                    }
                    try {
                        return method.invoke(physicalConnection, args);
                    } catch (InvocationTargetException e) {
                        Throwable cause = e.getCause();
                        if (cause instanceof SQLException) {
                            throw (SQLException) cause;
                        }
                        throw e;
                    }
                }
        );
    }

    private static boolean isConnectionAlive() {
        if (physicalConnection == null)
            return false;
        try {
            return physicalConnection.isValid(2);
        } catch (SQLException e) {
            return false;
        }
    }

    public static boolean isConnected() {
        return isConnectionAlive();
    }

    public static boolean testConnection(String host, int port, String dbName, String user, String password,
            boolean ssl) throws SQLException {
        boolean isPg = port == 5432 || port == 6543 || (host != null && (host.contains("supabase") || host.contains("postgres")));
        String url;
        if (isPg) {
            String sslParam = (ssl || (host != null && !host.equalsIgnoreCase("localhost") && !host.equals("127.0.0.1"))) ? "sslmode=require" : "sslmode=prefer";
            url = "jdbc:postgresql://" + host + ":" + port + "/" + dbName + "?" + sslParam
                    + "&prepareThreshold=0&tcpKeepAlive=true";
        } else {
            boolean isRemote = host != null && !host.equalsIgnoreCase("localhost") && !host.equals("127.0.0.1");
            String sslParam = (ssl || isRemote) ? "sslMode=REQUIRED&allowPublicKeyRetrieval=true" : "sslMode=DISABLED&allowPublicKeyRetrieval=true";
            url = "jdbc:mysql://" + host + ":" + port + "/" + dbName + "?" + sslParam
                    + "&serverTimezone=UTC&connectTimeout=15000&socketTimeout=30000";
        }
        try (Connection testConn = DriverManager.getConnection(url, user, password)) {
            return testConn != null && testConn.isValid(5);
        }
    }

    public static void ensureDatabaseExists() {
        if (DatabaseConfig.isPostgres()) {
            return;
        }
        String host = DatabaseConfig.getHost();
        if (host == null || (!host.equalsIgnoreCase("localhost") && !host.equals("127.0.0.1"))) {
            return;
        }
        try (Connection serverConn = DriverManager.getConnection(
                DatabaseConfig.getBaseUrl(),
                DatabaseConfig.getUser(),
                DatabaseConfig.getPassword());
                Statement stmt = serverConn.createStatement()) {
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DatabaseConfig.getDbName()
                    + " CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;");
        } catch (SQLException e) {
            System.err.println("Note on DB check: " + e.getMessage());
        }
    }

    public static synchronized void closeConnection() {
        try {
            if (physicalConnection != null && !physicalConnection.isClosed()) {
                physicalConnection.close();
                physicalConnection = null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
