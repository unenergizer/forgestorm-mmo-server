package com.forgestorm.server.game.world.maps;

import com.forgestorm.server.ServerMain;
import com.forgestorm.server.game.GameConstants;
import com.forgestorm.server.game.world.entity.AiEntity;
import com.forgestorm.server.game.world.entity.MovingEntity;
import com.forgestorm.server.game.world.task.MovementUpdateTask;

public class AiEntityController extends EntityController<AiEntity> {

    AiEntityController(GameWorld gameWorld) {
        super(gameWorld, GameConstants.MAX_AI_ENTITIES);
    }

    public void releaseEntityTargets(MovingEntity targetToRemove) {
        for (AiEntity aiEntity : entityHashMap.values()) {
            if (aiEntity.getTargetEntity() != null && aiEntity.getTargetEntity().equals(targetToRemove)) {
                aiEntity.setTargetEntity(null);
                aiEntity.setInCombat(false);
            }
        }
    }

    @Override
    public void postEntityDespawn(AiEntity entity) {
        ServerMain.getInstance().getGameLoop().getAiEntityRespawnTimerTask().addAiEntity(entity);
    }

    @Override
    public void tick() {
        entitySpawnQueue.forEach(this::entitySpawnRegistration);
        entityDespawnQueue.forEach(this::entityDespawnRegistration);

        AiEntity entity;
        while ((entity = entitySpawnQueue.poll()) != null) {
            entitySpawn(entity);

            // Find AiEntity a active target
            MovementUpdateTask movementUpdateTask = ServerMain.getInstance().getGameLoop().getMovementUpdateTask();
            movementUpdateTask.initEntityTargeting(entity);
        }

        while ((entity = entityDespawnQueue.poll()) != null) {
            entityDespawn(entity);
        }
    }
}
