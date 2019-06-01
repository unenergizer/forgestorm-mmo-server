package com.valenguard.server.game.world.entity;

import com.valenguard.server.Server;
import com.valenguard.server.game.rpg.Attributes;
import com.valenguard.server.game.world.maps.Location;
import com.valenguard.server.game.world.maps.MoveDirection;
import com.valenguard.server.game.world.maps.Warp;
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

    public AiEntity generateEntity(int entityDataID, Location location) {
        AiEntityLoader.AiEntityData aiEntityData = Server.getInstance().getAiEntityDataManager().getEntityData(entityDataID);

        AiEntity aiEntity = null;

        if (aiEntityData.getEntityType() == EntityType.MONSTER) {
            aiEntity = new Monster();
            ((Monster) aiEntity).setAlignment(aiEntityData.getEntityAlignment());
        } else if (aiEntityData.getEntityType() == EntityType.NPC) {
            aiEntity = new NPC();
            ((NPC) aiEntity).setFaction(Server.getInstance().getFactionManager().getFactionByName(aiEntityData.getFaction()));
        }

        aiEntity.setName(aiEntityData.getName());
        aiEntity.setEntityType(aiEntityData.getEntityType());
        aiEntity.setCurrentHealth(aiEntityData.getHealth());
        aiEntity.setMaxHealth(aiEntityData.getHealth());
        aiEntity.setExpDrop(aiEntityData.getExpDrop());
        aiEntity.setDropTable(aiEntityData.getDropTable());
        aiEntity.setMoveSpeed(aiEntityData.getWalkSpeed());
        aiEntity.setBankKeeper(aiEntityData.isBankKeeper());

        aiEntity.setMovementInfo(aiEntityData.getProbabilityStill(), aiEntityData.getProbabilityWalkStart(), 0, 0, 96, 54);
        aiEntity.setSpawnWarp(new Warp(location, MoveDirection.SOUTH));
        aiEntity.gameMapRegister(aiEntity.getSpawnWarp());

        // Setup appearance
        int colorID = 0;
        if (aiEntityData.getSkinColor() != null) {
            colorID = aiEntityData.getSkinColor();
        }

        Appearance appearance = new Appearance(aiEntity);
        aiEntity.setAppearance(appearance);
        if (aiEntityData.getHairTexture() != null) {
            appearance.setHairTexture((byte) (int) aiEntityData.getHairTexture());
            appearance.setSkinColor(colorID);
        } else {
            appearance.setMonsterBodyTexture((byte) (int) aiEntityData.getMonsterBodyTexture());
        }

        // Setup basic attributes.
        Attributes attributes = new Attributes();
        attributes.setDamage(aiEntityData.getDamage());
        aiEntity.setAttributes(attributes);

        if (aiEntityData.getShopID() != null) aiEntity.setShopId((short) (int) aiEntityData.getShopID());

        return aiEntity;
    }
}
