package com.forgestorm.server.game.world.maps;

import com.forgestorm.server.game.GameConstants;
import com.forgestorm.server.game.world.entity.StationaryEntity;

public class StationaryEntityController extends EntityController<StationaryEntity> {

    StationaryEntityController(GameWorld gameWorld) {
        super(gameWorld, GameConstants.MAX_STATIONARY_ENTITIES);
    }

    @Override
    public void postEntityDespawn(StationaryEntity entity) {
    }

    @Override
    public void tick() {
        entitySpawnQueue.forEach(stationaryEntity -> entityHashMap.put(stationaryEntity.getServerEntityId(), stationaryEntity));
        entityDespawnQueue.forEach(stationaryEntity -> entityHashMap.remove(stationaryEntity.getServerEntityId()));

        StationaryEntity stationaryEntity;
        while ((stationaryEntity = entitySpawnQueue.poll()) != null) {
            entitySpawn(stationaryEntity);
        }

        while ((stationaryEntity = entityDespawnQueue.poll()) != null) {
            entityDespawn(stationaryEntity);
        }
    }
}
