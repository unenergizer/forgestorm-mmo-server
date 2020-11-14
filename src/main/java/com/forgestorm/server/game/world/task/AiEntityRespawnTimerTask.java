package com.forgestorm.server.game.world.task;

import com.forgestorm.server.game.GameConstants;
import com.forgestorm.server.game.world.entity.AiEntity;
import com.forgestorm.server.game.world.maps.MoveDirection;
import com.forgestorm.server.game.world.maps.Warp;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.forgestorm.server.util.Log.println;

public class AiEntityRespawnTimerTask implements AbstractTask {

    private static final boolean PRINT_DEBUG = false;
    private final List<RespawnTimer> respawnTimers = new ArrayList<>();

    private int timerTick = 0; // active speed

    public void addAiEntity(AiEntity aiEntity) {
        if (aiEntity.isInstantRespawn()) {
            respawnTimers.add(new RespawnTimer(aiEntity, 1));
            aiEntity.setInstantRespawn(false); // Reset to false
        } else {
            respawnTimers.add(new RespawnTimer(aiEntity, GameConstants.GENERAL_RESPAWN_TIME));
        }
    }

    @Override
    public void tick(long ticksPassed) {
        timerTick++;
        if (timerTick >= 20) {
            timerTick = 0;

            Iterator<RespawnTimer> iterator = respawnTimers.iterator();
            while (iterator.hasNext()) {
                RespawnTimer timer = iterator.next();

                timer.respawnTime--;
                if (timer.respawnTime <= 0) {
                    AiEntity aiEntity = timer.aiEntity;

                    // Spawn to original location
                    aiEntity.gameWorldRegister(new Warp(aiEntity.getDefaultSpawnLocation(), MoveDirection.SOUTH));

                    // Reset health
                    aiEntity.setCurrentHealth(aiEntity.getMaxHealth());

                    // Finally, do the respawn!
                    aiEntity.getCurrentWorldLocation().getGameWorld().getAiEntityController().queueEntitySpawn(aiEntity);

                    iterator.remove();
                    println(getClass(), "Respawning Entity: " + aiEntity.getName() + " " + aiEntity.getServerEntityId(), false, PRINT_DEBUG);
                }
            }
        }
    }

    @AllArgsConstructor
    private class RespawnTimer {
        private final AiEntity aiEntity;
        private int respawnTime;
    }
}
