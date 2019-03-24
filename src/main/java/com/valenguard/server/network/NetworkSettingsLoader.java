package com.valenguard.server.network;

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
            inputStream = new FileInputStream(new File("src/main/resources/data/Network.yaml"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Map<String, Map<String,Object>> root = yaml.load(inputStream);

        int loginPort = (Integer) root.get("login").get("port");
        String gameIp = (String) root.get("game").get("ip");
        int gamePort = (Integer) root.get("game").get("port");

        println(getClass(), "LoginSettings: " + loginPort, false, PRINT_DEBUG);
        println(getClass(), "GameSettings: " + gameIp + ":" + gamePort, false, PRINT_DEBUG);

        return new NetworkSettings(loginPort, gameIp, gamePort);
    }

}
