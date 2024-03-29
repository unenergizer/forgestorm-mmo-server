package com.forgestorm.server.game.world.task;

import com.forgestorm.server.ServerMain;
import com.forgestorm.server.game.abilities.WaitingAbility;
import com.forgestorm.server.game.world.entity.AiEntity;
import com.forgestorm.server.game.world.entity.MovingEntity;
import com.forgestorm.server.game.world.entity.Player;
import com.forgestorm.server.game.world.maps.GameWorld;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class AbilityUpdateTask implements AbstractTask {

    private static final int IDLE_COMBAT_TIMEOUT = 13;

    @Override
    public void tick(long ticksPassed) {
        for (GameWorld gameWorld : ServerMain.getInstance().getGameManager().getGameWorldProcessor().getGameWorlds().values()) {
            tickGameMapCombat(gameWorld);
            checkDeath(gameWorld);
        }
    }

    private void checkDeath(GameWorld gameWorld) {
        // TODO: ? This code is kinda redundent and death should probably be seperated out in some way
        Collection<AiEntity> aiEntityList = gameWorld.getAiEntityController().getEntities();
        List<Player> playerList = gameWorld.getPlayerController().getPlayerList();
        for (AiEntity aiEntity : aiEntityList) {
            if (aiEntity.getCurrentHealth() <= 0) {
                processEntityDeath(gameWorld, aiEntity);
            }
        }
        for (Player player : playerList) {
            if (player.getCurrentHealth() <= 0) {
                processEntityDeath(gameWorld, player);
            }
        }
    }

    private void tickGameMapCombat(GameWorld gameWorld) {
        Collection<AiEntity> aiEntityList = gameWorld.getAiEntityController().getEntities();
        List<Player> playerList = gameWorld.getPlayerController().getPlayerList();

        // Checks for every tick
        aiEntityList.forEach(this::updateCooldowns);
        playerList.forEach(this::updateCooldowns);

        // Update AiEntity
        for (AiEntity aiEntity : aiEntityList) {
            if (aiEntity.getTargetEntity() == null) continue;

            MovingEntity targetEntity = aiEntity.getTargetEntity();

            if (aiEntity.getCurrentHealth() <= 0) {
                processEntityDeath(gameWorld, targetEntity, aiEntity);
            } else {
                // Do AiEntity combat
                ServerMain.getInstance().getAbilityManager().performAiEntityAbility(aiEntity, targetEntity);
            }

            if (targetEntity.getCurrentHealth() <= 0) {
                processEntityDeath(gameWorld, aiEntity, targetEntity);
            }
        }

        // Update Player
        for (Player playerAttacker : playerList) {
            if (playerAttacker.getTargetEntity() == null) {

                continue;
            }

            MovingEntity targetEntity = playerAttacker.getTargetEntity();

            if (playerAttacker.getCurrentHealth() <= 0) {
                processEntityDeath(gameWorld, targetEntity, playerAttacker);
            } else {
                // TODO: Does this need to happen for players? Currently happens in "AbilityRequestPacketIn"
                // Do Ability combat
//                Server.getInstance().getAbilityManager().performAiEntityAbility(playerAttacker, targetEntity);
//                Server.getInstance().getAbilityManager().performPlayerAbility(playerAttacker, targetEntity);
            }

            if (targetEntity.getCurrentHealth() <= 0) {
                processEntityDeath(gameWorld, playerAttacker, targetEntity);
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
                        ServerMain.getInstance().getAbilityManager().performPlayerAbility(cooldown.getKey(), movingEntity, queuedAbility.getTargetEntity());
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

    private void processEntityDeath(GameWorld gameWorld, MovingEntity killerEntity, MovingEntity deadEntity) {
        // Remove the deadEntity from all entities target!
        gameWorld.getAiEntityController().releaseEntityTargets(deadEntity);
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

    private void processEntityDeath(GameWorld gameWorld, MovingEntity deadEntity) {
        gameWorld.getAiEntityController().releaseEntityTargets(deadEntity);
        deadEntity.setTargetEntity(null);
        deadEntity.setInCombat(false);

        if (deadEntity instanceof Player) {
            Player deadPlayer = (Player) deadEntity;
            deadPlayer.killPlayer();
        }
    }
}
