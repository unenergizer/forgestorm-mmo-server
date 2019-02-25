package com.valenguard.server.game.entity;

import com.valenguard.server.game.GameConstants;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.valenguard.server.util.Log.println;

public class EntityRespawnTimer {

    private static final boolean PRINT_DEBUG = false;
    private final List<RespawnTimer> respawnTimers = new ArrayList<>();

    public void addMob(MovingEntity movingEntity) {
        respawnTimers.add(new RespawnTimer(movingEntity, GameConstants.GENERAL_RESPAWN_TIME));
    }

    private int timerTick = 0; // combat speed

    public void tickRespawnTime() {

        timerTick++;
        if (timerTick >= 20) {
            timerTick = 0;

            Iterator<RespawnTimer> iterator = respawnTimers.iterator();
            while (iterator.hasNext()) {
                RespawnTimer timer = iterator.next();

                timer.respawnTime--;
                if (timer.respawnTime <= 0) {
                    MovingEntity movingEntity = timer.movingEntity;

                    // Spawn to original location
                    movingEntity.gameMapRegister(movingEntity.getSpawnWarp());

                    // Reset health
                    movingEntity.setCurrentHealth(movingEntity.getMaxHealth());

                    // Finally, do the respawn!l
                    movingEntity.getCurrentMapLocation().getGameMap().queueAiEntitySpawn(movingEntity);

                    iterator.remove();
                    println(getClass(), "Respawning Entity: " + movingEntity.getName() + " " + movingEntity.getServerEntityId(), false, PRINT_DEBUG);
                }
            }
        }
    }

    @AllArgsConstructor
    private class RespawnTimer {
        private final MovingEntity movingEntity;
        private int respawnTime;
    }

}
