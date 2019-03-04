package com.valenguard.server.game.entity;

import com.valenguard.server.game.GameConstants;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.valenguard.server.util.Log.println;

public class AiEntityRespawnTimer {

    private static final boolean PRINT_DEBUG = false;
    private final List<RespawnTimer> respawnTimers = new ArrayList<>();

    public void addAiEntity(AiEntity movingEntity) {
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
                    AiEntity aiEntity = timer.movingEntity;

                    // Spawn to original location
                    aiEntity.gameMapRegister(aiEntity.getSpawnWarp());

                    // Reset health
                    aiEntity.setCurrentHealth(aiEntity.getMaxHealth());

                    // Finally, do the respawn!
                    aiEntity.getCurrentMapLocation().getGameMap().queueAiEntitySpawn(aiEntity);

                    iterator.remove();
                    println(getClass(), "Respawning Entity: " + aiEntity.getName() + " " + aiEntity.getServerEntityId(), false, PRINT_DEBUG);
                }
            }
        }
    }

    @AllArgsConstructor
    private class RespawnTimer {
        private final AiEntity movingEntity;
        private int respawnTime;
    }

}
