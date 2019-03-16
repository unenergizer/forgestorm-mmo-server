package com.valenguard.server.game.rpg;

import com.valenguard.server.game.data.FactionLoader;

import java.util.Map;

public class FactionManager {

    private Map<Byte, FactionLoader.FactionData> factionDataMap;

    public FactionManager() {
        init();
    }

    private void init() {
        FactionLoader factionLoader = new FactionLoader();
        factionDataMap = factionLoader.loadFactionInfo();
    }

    public String getFactionName(byte b) {
        return factionDataMap.get(b).getFactionName();
    }

    public Byte getFactionByName(String factionName) {
        for (Map.Entry<Byte, FactionLoader.FactionData> entry : factionDataMap.entrySet()) {
            if (factionName.equals(entry.getValue().getFactionName())) return entry.getKey();
        }
        return null;
    }

    public byte[] getFactionEnemies(byte b) {
        return factionDataMap.get(b).getEnemyFactions();
    }

    public int getNumberOfFactions() {
        return factionDataMap.size();
    }
}
