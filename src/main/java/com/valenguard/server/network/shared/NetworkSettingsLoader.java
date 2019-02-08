package com.valenguard.server.network.shared;

import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Map;

import static com.valenguard.server.util.Log.println;

public class NetworkSettingsLoader {

    private static final boolean PRINT_DEBUG = true;

    public NetworkSettings loadNetworkSettings() {
        Yaml yaml = new Yaml();

        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(new File("src/main/resources/data/network.yaml"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Map<String, Object> root = yaml.load(inputStream);
        String IP = (String) root.get("ip");
        int port = (Integer) root.get("port");

        println(getClass(), IP + ":" + port, false, PRINT_DEBUG);

        return new NetworkSettings(IP, port);
    }

}
