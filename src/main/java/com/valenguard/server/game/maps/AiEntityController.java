package com.valenguard.server.game.maps;

import com.valenguard.server.ValenguardMain;
import com.valenguard.server.game.entity.AiEntity;
import com.valenguard.server.game.entity.MovingEntity;
import com.valenguard.server.game.task.UpdateMovements;

public class AiEntityController extends EntityController<AiEntity> {

    AiEntityController(GameMap gameMap) {
        super(gameMap);
    }

    public void releaseEntityTargets(MovingEntity targetToRemove) {
        for (AiEntity aiEntity : entityHashMap.values()) {
            if (aiEntity.getTargetEntity() != null && aiEntity.getTargetEntity().equals(targetToRemove)) {
                aiEntity.setTargetEntity(null);
            }
        }
    }

    @Override
    public void postEntityDespawn(AiEntity entity) {
        ValenguardMain.getInstance().getAiEntityRespawnTimer().addAiEntity(entity);
    }

    @Override
    public void tick() {
        entitySpawnQueue.forEach(this::entitySpawnRegistration);
        entityDespawnQueue.forEach(this::entityDespawnRegistration);

        AiEntity entity;
        while ((entity = entitySpawnQueue.poll()) != null) {
            entitySpawn(entity);

            // Find AiEntity a combat target
            UpdateMovements updateMovements = ValenguardMain.getInstance().getGameLoop().getUpdateMovements();
            updateMovements.initEntityTargeting(entity);
        }

        while ((entity = entityDespawnQueue.poll()) != null) {
            entityDespawn(entity);
        }
    }
}
