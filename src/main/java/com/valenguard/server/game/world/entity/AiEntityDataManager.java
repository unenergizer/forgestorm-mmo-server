package com.valenguard.server.game.world.entity;

import com.valenguard.server.io.AiEntityLoader;

import java.util.List;

public class AiEntityDataManager {

    private AiEntityLoader.AiEntityData[] aiEntityData;

    public void start() {
        AiEntityLoader aiEntityLoader = new AiEntityLoader();
        List<AiEntityLoader.AiEntityData> loadedMovingAiEntityData = aiEntityLoader.loadMovingEntities();
        aiEntityData = new AiEntityLoader.AiEntityData[loadedMovingAiEntityData.size()];
        loadedMovingAiEntityData.toArray(aiEntityData);
    }

    public AiEntityLoader.AiEntityData getEntityData(int entityDataID) {
        return aiEntityData[entityDataID];
    }
}
