package com.forgestorm.server.io;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Map;

public class DatabaseSettingsLoader extends AsynchronousAssetLoader<DatabaseSettingsLoader.DatabaseSettingsData, DatabaseSettingsLoader.DatabaseSettingsParameter> {

    static class DatabaseSettingsParameter extends AssetLoaderParameters<DatabaseSettingsLoader.DatabaseSettingsData> {
    }

    private DatabaseSettingsData databaseSettingsData;

    public DatabaseSettingsLoader(FileHandleResolver resolver) {
        super(resolver);
    }

    @Override
    public void loadAsync(AssetManager manager, String fileName, FileHandle file, DatabaseSettingsParameter parameter) {
        Yaml yaml = new Yaml();

        InputStream inputStream = getClass().getResourceAsStream(FilePaths.DATABASE_SETTINGS.getFilePath());

        Map<String, Object> root = yaml.load(inputStream);
        String IP = (String) root.get("ip");
        int port = (Integer) root.get("port");
        String database = (String) root.get("database");
        String username = (String) root.get("username");
        String password = (String) root.get("password");

        databaseSettingsData = new DatabaseSettingsData(IP, port, database, username, password);
    }

    @Override
    public DatabaseSettingsData loadSync(AssetManager manager, String fileName, FileHandle file, DatabaseSettingsParameter parameter) {
        return databaseSettingsData;
    }

    @Override
    public Array<AssetDescriptor> getDependencies(String fileName, FileHandle file, DatabaseSettingsParameter parameter) {
        return null;
    }

    @Getter
    @AllArgsConstructor
    public class DatabaseSettingsData {
        private String ip;
        private int port;
        private String database;
        private String username;
        private String password;
    }
}
