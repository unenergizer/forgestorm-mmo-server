package com.valenguard.server.game.mysql;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;

import java.sql.SQLException;

import static com.valenguard.server.util.Log.println;

public class DatabaseConnection {

    private static DatabaseConnection instance;

    @Getter
    private HikariDataSource hikariDataSource;
    private DatabaseSettings databaseSettings;

    private DatabaseConnection() {
    }

    /**
     * Gets the main instance of this class.
     *
     * @return A singleton instance of this class.
     */
    public static DatabaseConnection getInstance() {
        if (instance == null) instance = new DatabaseConnection();
        return instance;
    }

    public void openDatabase(DatabaseSettings databaseSettings) {
        this.databaseSettings = databaseSettings;

        HikariConfig config = new HikariConfig();
        config.setDriverClassName("com.mysql.jdbc.Driver");
        config.setJdbcUrl("jdbc:mysql://" + databaseSettings.getIp() + ":" + databaseSettings.getPort() + "/" + databaseSettings.getDatabase());
        config.setUsername(databaseSettings.getUsername());
        config.setPassword(databaseSettings.getPassword());
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        hikariDataSource = new HikariDataSource(config);

        testConnection();
    }

    private void testConnection() {
        try {
            hikariDataSource.getConnection();
            println(getClass(), "Database Connection Successful");
        } catch (SQLException | RuntimeException e) {
            println(getClass(), "Could not connect to MySQL Database!", true);
            println(getClass(), "ServerName: " + databaseSettings.getIp(), true);
            println(getClass(), "Port: " + databaseSettings.getPort(), true);
            println(getClass(), "DatabaseName: " + databaseSettings.getDatabase(), true);
            println(getClass(), "User: " + databaseSettings.getUsername(), true);
            println(getClass(), "Password: " + databaseSettings.getPassword(), true);
        }
    }

    public void close() {
        hikariDataSource.close();
        println(getClass(), "Shut down");
    }
}
