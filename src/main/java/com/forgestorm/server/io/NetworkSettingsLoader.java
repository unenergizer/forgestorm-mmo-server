package com.forgestorm.server.io;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Map;

import static com.forgestorm.server.util.Log.println;

public class NetworkSettingsLoader {

    private static final boolean PRINT_DEBUG = false;

    public NetworkSettings loadNetworkSettings() {
        Yaml yaml = new Yaml();

        InputStream inputStream = getClass().getResourceAsStream(FilePaths.NETWORK_SETTINGS.getFilePath());

        Map<String, Map<String, Object>> root = yaml.load(inputStream);

        int loginPort = (Integer) root.get("login").get("port");
        String gameIp = (String) root.get("game").get("ip");
        int gamePort = (Integer) root.get("game").get("port");

        println(getClass(), "LoginSettings: " + loginPort, false, PRINT_DEBUG);
        println(getClass(), "GameSettings: " + gameIp + ":" + gamePort, false, PRINT_DEBUG);

        return new NetworkSettings(loginPort, gameIp, gamePort);
    }

    @Getter
    @AllArgsConstructor
    public class NetworkSettings {
        private int loginPort;
        private String gameIp;
        private int gamePort;
    }
}
