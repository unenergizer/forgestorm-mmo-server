package com.valenguard.server.game.world.task;

import com.valenguard.server.Server;
import com.valenguard.server.game.abilities.WaitingAbility;
import com.valenguard.server.game.world.entity.AiEntity;
import com.valenguard.server.game.world.entity.MovingEntity;
import com.valenguard.server.game.world.entity.Player;
import com.valenguard.server.game.world.maps.GameMap;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class AbilityUpdateTask implements AbstractTask {

    private static final int IDLE_COMBAT_TIMEOUT = 13;

    @Override
    public void tick(long ticksPassed) {
        for (GameMap gameMap : Server.getInstance().getGameManager().getGameMapProcessor().getGameMaps().values()) {
            tickGameMapCombat(gameMap, ticksPassed);
        }
    }

    private void tickGameMapCombat(GameMap gameMap, long numberOfTicksPassed) {
        Collection<AiEntity> aiEntityList = gameMap.getAiEntityController().getEntities();
        List<Player> playerList = gameMap.getPlayerController().getPlayerList();

        // Checks for every tick
        aiEntityList.forEach(this::updateCooldowns);
        playerList.forEach(this::updateCooldowns);

        for (AiEntity aiEntity : aiEntityList) {
            if (aiEntity.getTargetEntity() == null) continue;

            MovingEntity targetEntity = aiEntity.getTargetEntity();

            if (aiEntity.getCurrentHealth() <= 0) {
                processEntityDeath(gameMap, targetEntity, aiEntity);
            } else {
                // Do AiEntity combat
                Server.getInstance().getAbilityManager().performAiEntityAbility(aiEntity, targetEntity);
            }

            if (targetEntity.getCurrentHealth() <= 0) {
                processEntityDeath(gameMap, aiEntity, targetEntity);
            }
        }
    }

    private void updateCooldowns(MovingEntity movingEntity) {
        Iterator<Map.Entry<Short, Integer>> iterator = movingEntity.getAbilitiesToCooldown().entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Short, Integer> cooldown = iterator.next();
            int cooldownRemaining = cooldown.getValue() - 1;
            if (cooldownRemaining <= 0) {
                if (movingEntity instanceof Player) {

                    // The player cast an ability before it was finished cooling down.
                    // We saved the ability cast and will perform it now for the player.
                    WaitingAbility queuedAbility = ((Player) movingEntity).getQueuedAbilities().get(cooldown.getKey());
                    if (queuedAbility != null) {
                        Server.getInstance().getAbilityManager().performPlayerAbility(cooldown.getKey(), movingEntity, queuedAbility.getTargetEntity());
                        ((Player) movingEntity).getQueuedAbilities().remove(cooldown.getKey());
                    }
                }

                iterator.remove();
            } else {
                cooldown.setValue(cooldownRemaining);
            }
        }
    }

    private void idleTooLong(MovingEntity movingEntity) {

        if (movingEntity.getCombatIdleTime() > IDLE_COMBAT_TIMEOUT) {
            movingEntity.setInCombat(false);
            movingEntity.setTargetEntity(null);
            return;
        }

        movingEntity.setCombatIdleTime(movingEntity.getCombatIdleTime() + 1);
    }

    private void processEntityDeath(GameMap gameMap, MovingEntity killerEntity, MovingEntity deadEntity) {
        // Remove the deadEntity from all entities target!
        gameMap.getAiEntityController().releaseEntityTargets(deadEntity);
        deadEntity.setTargetEntity(null);
        deadEntity.setInCombat(false);

        killerEntity.setTargetEntity(null);
        killerEntity.setInCombat(false);

        if (deadEntity instanceof Player) {
            Player deadPlayer = (Player) deadEntity;
            deadPlayer.killPlayer();
        } else {
            AiEntity deadAiEntity = (AiEntity) deadEntity;
            deadAiEntity.killAiEntity(killerEntity);
        }
    }
}
