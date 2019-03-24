package com.valenguard.server.io;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Map;

public class DatabaseSettingsLoader {

    public DatabaseSettings loadNetworkSettings() {
        Yaml yaml = new Yaml();

        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(new File(FilePaths.DATABASE_SETTINGS.getFilePath()));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Map<String, Object> root = yaml.load(inputStream);
        String IP = (String) root.get("ip");
        int port = (Integer) root.get("port");
        String database = (String) root.get("database");
        String username = (String) root.get("username");
        String password = (String) root.get("password");

        return new DatabaseSettings(IP, port, database, username, password);
    }

    @Getter
    @AllArgsConstructor
    public class DatabaseSettings {
        private String ip;
        private int port;
        private String database;
        private String username;
        private String password;
    }
}
