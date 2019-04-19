package com.valenguard.server.game.world.task;

import com.valenguard.server.Server;
import com.valenguard.server.game.rpg.Attributes;
import com.valenguard.server.game.world.entity.*;
import com.valenguard.server.game.world.item.ItemStack;
import com.valenguard.server.game.world.maps.GameMap;
import com.valenguard.server.game.world.maps.ItemStackDropEntityController;
import com.valenguard.server.network.game.packet.out.ChatMessagePacketOut;
import com.valenguard.server.network.game.packet.out.EntityDamagePacketOut;

public class CombatUpdateTask implements AbstractTask {

    private static final int IDLE_COMBAT_TIMEOUT = 13;

    @Override
    public void tick(long ticksPassed) {
        for (GameMap gameMap : Server.getInstance().getGameManager().getGameMapProcessor().getGameMaps().values()) {
            tickGameMapCombat(gameMap, ticksPassed);
        }
    }

    private void tickGameMapCombat(GameMap gameMap, long numberOfTicksPassed) {
        if (numberOfTicksPassed % 60 == 0) {

            // Now do combat
            for (AiEntity aiEntity : gameMap.getAiEntityController().getEntities()) {
                if (aiEntity.getTargetEntity() == null) continue;

                MovingEntity targetEntity = aiEntity.getTargetEntity();
                // Check for distance
                // TODO: Check distance on a per move/spell basis (shield slam allows 5 blocks, hand to hand, 1 block distance)
                if (aiEntity.getCurrentMapLocation().isWithinDistance(targetEntity.getFutureMapLocation(), (short) 1)
                        || aiEntity.getCurrentMapLocation().isWithinDistance(targetEntity.getCurrentMapLocation(), (short) 1)) {

                    if (!aiEntity.isInCombat()) aiEntity.setInCombat(true);
                    if (!aiEntity.getTargetEntity().isInCombat()) aiEntity.getTargetEntity().setInCombat(true);

                    // Entity vs Target
                    Attributes movingEntityAttributes = aiEntity.getAttributes();
                    Attributes targetEntityAttributes = targetEntity.getAttributes();
                    targetEntity.setCurrentHealth(targetEntity.getCurrentHealth() - movingEntityAttributes.getDamage());

                    // Target vs Entity
                    boolean aiTookDamage = true;
                    if (targetEntity.getTargetEntity() == aiEntity && targetEntity instanceof Player) {
                        aiEntity.setCurrentHealth(aiEntity.getCurrentHealth() - targetEntityAttributes.getDamage());
                    } else if (!(targetEntity instanceof Player)) {
                        aiEntity.setCurrentHealth(aiEntity.getCurrentHealth() - targetEntityAttributes.getDamage());
                    } else {
                        aiTookDamage = false;
                    }

                    sendCombatMessage(gameMap, aiEntity, targetEntity, aiTookDamage);

                    if (aiEntity.getCurrentHealth() <= 0) {
                        finishCombat(gameMap, targetEntity, aiEntity);
                    }

                    if (targetEntity.getCurrentHealth() <= 0) {
                        finishCombat(gameMap, aiEntity, targetEntity);
                    }
                } else {

                    idleTooLong(targetEntity);

                }
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

    private void sendCombatMessage(GameMap gameMap, AiEntity attackerEntity, MovingEntity targetEntity, boolean aiTookDamage) {
        Attributes attackerEntityAttributes = attackerEntity.getAttributes();
        Attributes targetEntityAttributes = targetEntity.getAttributes();

        if (targetEntity instanceof Player) {
            new ChatMessagePacketOut((Player) targetEntity, "Enemy HP: " + attackerEntity.getCurrentHealth() + " Damage Dealt: " + targetEntityAttributes.getDamage()).sendPacket();
            new ChatMessagePacketOut((Player) targetEntity, "Your HP: " + targetEntity.getCurrentHealth() + " Damage Dealt: " + attackerEntityAttributes.getDamage()).sendPacket();
        }

        gameMap.getPlayerController().forAllPlayers(player -> {
            if (aiTookDamage) {
                new EntityDamagePacketOut(player, attackerEntity, attackerEntity.getCurrentHealth(), targetEntityAttributes.getDamage()).sendPacket();
            }
            new EntityDamagePacketOut(player, targetEntity, targetEntity.getCurrentHealth(), attackerEntityAttributes.getDamage()).sendPacket();
        });
    }

    private void finishCombat(GameMap gameMap, MovingEntity killerEntity, MovingEntity deadEntity) {

        // Remove the deadEntity from all entities target!
        gameMap.getAiEntityController().releaseEntityTargets(deadEntity);
        deadEntity.setTargetEntity(null);
        deadEntity.setInCombat(false);

        killerEntity.setTargetEntity(null);
        killerEntity.setInCombat(false);

        if (deadEntity instanceof Player) {
            /*
             * A PLAYER DEATH -------------------------------------------------
             */
            Player deadPlayer = (Player) deadEntity;
            deadPlayer.killPlayer();

        } else {
            /*
             * A NON PLAYER DEATH -------------------------------------------------
             */
            AiEntity aiEntity = (AiEntity) deadEntity;
            gameMap.getAiEntityController().queueEntityDespawn(aiEntity); // A mob died, despawn them!

            // If a AI entity kills and AI entity, do not drop ItemStack
            if (killerEntity.getEntityType() != EntityType.PLAYER) return;

            Player killerPlayer = (Player) killerEntity;

            // Give experience
            new ChatMessagePacketOut(killerPlayer, "You killed the enemy").sendPacket();
            killerPlayer.getSkills().MELEE.addExperience(aiEntity.getExpDrop());

            // Adding/Subtracting reputation
            if (aiEntity.getEntityType() == EntityType.NPC) {
                killerPlayer.getReputation().addReputation(((NPC) aiEntity).getFaction(), (short) 1000);
            }

            // Give packetReceiver drop table item
            if (aiEntity.getDropTable() != null) {
                ItemStack itemStack = Server.getInstance().getDropTableManager().getItemStack(aiEntity.getDropTable(), 1);

                ItemStackDropEntityController itemStackDropEntityController = gameMap.getItemStackDropEntityController();
                itemStackDropEntityController.queueEntitySpawn(itemStackDropEntityController.makeItemStackDrop(
                        itemStack,
                        deadEntity.getCurrentMapLocation(),
                        (Player) killerEntity
                ));
            }
        }
    }
}
