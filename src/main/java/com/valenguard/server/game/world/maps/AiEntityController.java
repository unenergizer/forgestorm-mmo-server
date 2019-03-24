package com.valenguard.server.game.world.maps;

import com.valenguard.server.Server;
import com.valenguard.server.game.task.UpdateMovements;
import com.valenguard.server.game.world.entity.AiEntity;
import com.valenguard.server.game.world.entity.MovingEntity;

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
        Server.getInstance().getGameLoop().getAiEntityRespawnTimerTask().addAiEntity(entity);
    }

    @Override
    public void tick() {
        entitySpawnQueue.forEach(this::entitySpawnRegistration);
        entityDespawnQueue.forEach(this::entityDespawnRegistration);

        AiEntity entity;
        while ((entity = entitySpawnQueue.poll()) != null) {
            entitySpawn(entity);

            // Find AiEntity a combat target
            UpdateMovements updateMovements = Server.getInstance().getGameLoop().getUpdateMovements();
            updateMovements.initEntityTargeting(entity);
        }

        while ((entity = entityDespawnQueue.poll()) != null) {
            entityDespawn(entity);
        }
    }
}