package com.forgestorm.server.io;

import com.forgestorm.server.ServerMain;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;

import static com.forgestorm.server.util.Log.println;
import static com.google.common.base.Preconditions.checkArgument;

public class ResourcePathLoader {

    @Getter
    private static String localFilePath;

    @Getter
    private static String resourcePath;

    public ResourcePathLoader() {
        // Define local path
        try {
            localFilePath = new File(ServerMain.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getPath();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        // Get YamlSettings file.
        ResourcePaths resourcePaths = loadResourcePaths();

        // Find resource directory
        File directory = new File(localFilePath + resourcePaths.path.replace("/", File.separator));
        if (!directory.exists()) {
            boolean result = directory.mkdir();
            println(getClass(), "Directory does not exist. Creating it now...");
            println(getClass(), "Directory created: " + result);
        }
        println(getClass(), "Resource Absolute Path: " + directory.getAbsolutePath());

        resourcePath = directory.getPath();

        FileWriter fw;
        try {
            fw = new FileWriter(resourcePath + File.separator + "Something.txt");
            fw.write("This is a test file...");
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private ResourcePaths loadResourcePaths() {
        Yaml yaml = new Yaml();

        File externalPath = new File(localFilePath + File.separator + "ResourcePaths.yaml");

        if (!externalPath.exists()) {
            File internalPath = new File("src/main/resources/data/".replace("/", File.separator) + "ResourcePaths.yaml");

            InputStream inputStream = null;
            try {
                inputStream = new FileInputStream(internalPath);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            checkArgument(createLocalYaml(inputStream, externalPath.getPath()), "Could not create ResourcePaths.yaml file!");
        }

        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(new File(localFilePath + File.separator + "ResourcePaths.yaml"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Map<String, Object> root = yaml.load(inputStream);
        String path = (String) root.get("path");

        return new ResourcePaths(path);
    }

    private boolean createLocalYaml(InputStream source, String destination) {
        boolean succeess = true;

        println(getClass(), "Copying ->" + source + "\n\tto ->" + destination);

        try {
            Files.copy(source, Paths.get(destination), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            succeess = false;
        }
        return succeess;
    }

    @Getter
    @AllArgsConstructor
    private class ResourcePaths {
        private String path;
    }
}
