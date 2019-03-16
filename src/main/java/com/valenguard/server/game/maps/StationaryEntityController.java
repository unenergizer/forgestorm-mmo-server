package com.valenguard.server.game.maps;

import com.valenguard.server.game.entity.StationaryEntity;

public class StationaryEntityController extends EntityController<StationaryEntity> {

    StationaryEntityController(GameMap gameMap) {
        super(gameMap);
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
