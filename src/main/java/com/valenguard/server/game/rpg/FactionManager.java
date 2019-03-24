package com.valenguard.server.game.rpg;

import com.valenguard.server.io.FactionLoader;

import java.util.Map;

public class FactionManager {

    private final Map<Byte, FactionLoader.FactionData> factionDataMap = new FactionLoader().loadFactionInfo();

    public Byte getFactionByName(String factionName) {
        for (Map.Entry<Byte, FactionLoader.FactionData> entry : factionDataMap.entrySet()) {
            if (factionName.equals(entry.getValue().getFactionName())) return entry.getKey();
        }
        return null;
    }

    public byte[] getFactionEnemies(byte b) {
        return factionDataMap.get(b).getEnemyFactions();
    }

    int getNumberOfFactions() {
        return factionDataMap.size();
    }
}