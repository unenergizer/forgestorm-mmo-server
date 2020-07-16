package com.forgestorm.server.database;

import com.forgestorm.server.io.DatabaseSettingsLoader;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;

import java.sql.Connection;
import java.sql.SQLException;

import static com.forgestorm.server.util.Log.println;

@SuppressWarnings("SpellCheckingInspection")
public class DatabaseManager {

    @Getter
    private HikariDataSource hikariDataSource;
    private DatabaseSettingsLoader.DatabaseSettings databaseSettings;

    public void start() {
        openDatabase(new DatabaseSettingsLoader().loadNetworkSettings());
    }

    private void openDatabase(DatabaseSettingsLoader.DatabaseSettings databaseSettings) {
        this.databaseSettings = databaseSettings;

        println(getClass(), "Initializing database...");

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://" + databaseSettings.getIp() + ":" + databaseSettings.getPort() + "/" + databaseSettings.getDatabase() + "?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC");
        config.setUsername(databaseSettings.getUsername());
        config.setPassword(databaseSettings.getPassword());
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        hikariDataSource = new HikariDataSource(config);

        testConnection();
    }

    private void testConnection() {
        try (Connection ignored = hikariDataSource.getConnection()) {
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

    public void exit() {
        println(getClass(), "Stopping...");
        hikariDataSource.close();
    }
}
