package com.valenguard.server.game.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.valenguard.server.util.Log.println;

public class FactionLoader {

    private static final boolean PRINT_DEBUG = false;
    private static final String FILE_PATH = "src/main/resources/data/entity/Factions.yaml";

    @AllArgsConstructor
    private class LoadFactionData {
        private String factionName;
        private byte factionId;
        private List<String> enemyFactions;
    }

    @Getter
    @AllArgsConstructor
    public class FactionData {
        private String factionName;
        private byte[] enemyFactions;
    }

    public Map<Byte, FactionData> loadFactionInfo() {
        Yaml yaml = new Yaml();

        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(new File(FILE_PATH));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Map<Byte, FactionData> factionDataMap = new HashMap<>();
        Map<Integer, Map<String, Object>> root = yaml.load(inputStream);

        List<LoadFactionData> loadFactionData = new ArrayList<>();

        for (Map.Entry<Integer, Map<String, Object>> factionInfo : root.entrySet()) {
            byte factionId = (byte) (int) factionInfo.getKey();
            Map<String, Object> factionData = factionInfo.getValue();
            String factionName = (String) factionData.get("name");
            List<String> enemyFactions = (List<String>) factionData.get("enemyFactions");
            loadFactionData.add(new LoadFactionData(factionName, factionId, enemyFactions));
        }

        // Converting from faction names to faction ids.
        for (LoadFactionData loadedFactionData : loadFactionData) {
            byte[] enemyFactionIds = new byte[loadedFactionData.enemyFactions.size()];
            for (int i = 0; i < loadedFactionData.enemyFactions.size(); i++) {
                for (LoadFactionData otherFactionData : loadFactionData) {
                    if (otherFactionData.factionName.equals(loadedFactionData.enemyFactions.get(i))) {
                        enemyFactionIds[i] = otherFactionData.factionId;
                        break;
                    }
                }
            }
            factionDataMap.put(loadedFactionData.factionId, new FactionData(loadedFactionData.factionName, enemyFactionIds));
        }

        // Print Map
        println(PRINT_DEBUG);
        println(getClass(), "=================== LOADING FACTIONS ===================", false, PRINT_DEBUG);
        for (Map.Entry<Byte, FactionData> entry : factionDataMap.entrySet()) {
            println(getClass(), "ID: " + entry.getKey(), false, PRINT_DEBUG);
            println(getClass(), "Name: " + entry.getValue().getFactionName(), false, PRINT_DEBUG);

            for (Byte enemies : entry.getValue().getEnemyFactions()) {
                println(getClass(), "Enemy: [" + enemies + "] " + factionDataMap.get(enemies).getFactionName(), false, PRINT_DEBUG);
            }
            println(PRINT_DEBUG);
        }
        println(getClass(), "=================== FINISHED LOADING FACTIONS ===================", false, PRINT_DEBUG);
        println(PRINT_DEBUG);

        return factionDataMap;
    }

}
