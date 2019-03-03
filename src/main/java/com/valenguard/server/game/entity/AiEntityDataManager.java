package com.valenguard.server.game.entity;

import com.valenguard.server.game.data.AiEntityLoader;

import java.util.List;

public class AiEntityDataManager {

    private AiEntityData[] aiEntityData;

    public AiEntityDataManager() {
        init();
    }

    private void init() {
        AiEntityLoader aiEntityLoader = new AiEntityLoader();
        List<AiEntityData> loadedMovingAiEntityData = aiEntityLoader.loadMovingEntities();
        aiEntityData = new AiEntityData[loadedMovingAiEntityData.size()];
        loadedMovingAiEntityData.toArray(aiEntityData);
    }

    public AiEntityData getEntityData(int entityDataID) {
        return aiEntityData[entityDataID];
    }
}
