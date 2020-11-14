package com.forgestorm.server.game.rpg;

import com.forgestorm.server.ServerMain;
import com.forgestorm.server.io.todo.FactionLoader;

import java.util.Map;

public class FactionManager {

    private final Map<Byte, com.forgestorm.server.io.todo.FactionLoader.FactionData> factionDataMap;

    public FactionManager() {
        ServerMain.getInstance().getFileManager().loadFactionData();
        factionDataMap = ServerMain.getInstance().getFileManager().getFactionData().getByteFactionDataMap();
    }

    public Byte getFactionByName(String factionName) {
        factionName = factionName.replace(" ", "_");
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
