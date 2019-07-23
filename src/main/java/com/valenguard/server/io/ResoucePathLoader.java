package com.valenguard.server.io;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Map;

public class ResoucePathLoader {

    public ResourcePaths loadResourcePaths() {
        Yaml yaml = new Yaml();

        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(new File("ResourcePaths.yaml"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Map<String, Object> root = yaml.load(inputStream);
        String path = (String) root.get("path");

        return new ResourcePaths(path);
    }

    @Getter
    @AllArgsConstructor
    public class ResourcePaths {
        private String path;
    }
}
