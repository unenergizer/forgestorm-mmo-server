package com.valenguard.server.game.task;

import com.valenguard.server.ValenguardMain;
import com.valenguard.server.game.PlayerConstants;
import com.valenguard.server.game.entity.*;
import com.valenguard.server.game.inventory.ItemStack;
import com.valenguard.server.game.maps.GameMap;
import com.valenguard.server.game.maps.Location;
import com.valenguard.server.game.maps.MoveDirection;
import com.valenguard.server.game.maps.Warp;
import com.valenguard.server.game.rpg.Attributes;
import com.valenguard.server.game.rpg.ExperiencePacketInfo;
import com.valenguard.server.game.rpg.skills.SkillOpcodes;
import com.valenguard.server.network.packet.out.*;

import static com.valenguard.server.util.Log.println;

public class CombatTickUpdates {

    private static final int IDLE_COMBAT_TIMEOUT = 13;

    public void tickCombat(long numberOfTicksPassed) {
        for (GameMap gameMap : ValenguardMain.getInstance().getGameManager().getGameMaps().values()) {
            tickGameMapCombat(gameMap, numberOfTicksPassed);
        }
    }

    private void tickGameMapCombat(GameMap gameMap, long numberOfTicksPassed) {
        if (numberOfTicksPassed % 60 == 0) {

            // Now do combat
            for (AiEntity aiEntity : gameMap.getAiEntityController().getEntities()) {
                if (aiEntity.getTargetEntity() == null) continue;

                MovingEntity targetEntity = aiEntity.getTargetEntity();
                // Check for distance
                if (aiEntity.getCurrentMapLocation().isWithinDistance(targetEntity.getFutureMapLocation(), (short) 1)
                        || aiEntity.getCurrentMapLocation().isWithinDistance(targetEntity.getCurrentMapLocation(), (short) 1)) {

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

                    if (targetEntity instanceof Player) {
                        idleTooLong((Player) targetEntity);
                    }

                }
            }
        }
    }

    private void idleTooLong(Player player) {

        if (player.getCombatIdleTime() > IDLE_COMBAT_TIMEOUT) {
            println(getClass(), "The player is no longer in combat!");
            player.setTargetEntity(null);
            return;
        }

        player.setCombatIdleTime(player.getCombatIdleTime() + 1);
    }

    private void sendCombatMessage(GameMap gameMap, AiEntity attackerEntity, MovingEntity targetEntity, boolean aiTookDamage) {
        Attributes attackerEntityAttributes = attackerEntity.getAttributes();
        Attributes targetEntityAttributes = targetEntity.getAttributes();

        if (targetEntity instanceof Player) {
            new ChatMessagePacketOut((Player) targetEntity, "Enemy HP: " + attackerEntity.getCurrentHealth() + " Damage Delt: " + targetEntityAttributes.getDamage()).sendPacket();
            new ChatMessagePacketOut((Player) targetEntity, "Your HP: " + targetEntity.getCurrentHealth() + " Damage Delt: " + attackerEntityAttributes.getDamage()).sendPacket();
        }

        gameMap.forAllPlayers(player -> {
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
        killerEntity.setTargetEntity(null);

        if (deadEntity instanceof Player) {
            /*
             * A PLAYER DEATH -------------------------------------------------
             */
            Player deadPlayer = (Player) deadEntity;
            Location teleportLocation = new Location(PlayerConstants.STARTING_MAP, PlayerConstants.RESPAWN_X_CORD, (short) (gameMap.getMapHeight() - PlayerConstants.RESPAWN_Y_CORD));
            MoveDirection facingDirection = MoveDirection.SOUTH;

            // Check to see if the packetReceiver needs to change maps!
            if (!isGraveYardMap(deadPlayer.getCurrentMapLocation())) {
                println(getClass(), "Warping packetReceiver to graveyard map!");

                // Warp packetReceiver to graveyard
                deadPlayer.setWarp(new Warp(teleportLocation, facingDirection));
            } else {
                println(getClass(), "Teleporting packetReceiver to graveyard!");

                // Teleport packetReceiver
                deadPlayer.getLatestMoveRequests().clear();
                deadPlayer.gameMapRegister(new Warp(teleportLocation, facingDirection));

                // Send all players in map the teleport packet
                gameMap.forAllPlayers(player -> new MovingEntityTeleportPacketOut(player, deadEntity, teleportLocation, facingDirection).sendPacket());

                // Send other players info about the reheal (if they are still on the same map)
                gameMap.forAllPlayers(player -> new EntityHealPacketOut(player, deadEntity, player.getMaxHealth() - player.getCurrentHealth()).sendPacket());
            }

            // Reheal Player
            deadPlayer.setCurrentHealth(deadPlayer.getMaxHealth());

            new ChatMessagePacketOut(deadPlayer, "You died! Respawning you in graveyard!").sendPacket();

        } else {
            /*
             * A NON PLAYER DEATH -------------------------------------------------
             */
            AiEntity aiEntity = (AiEntity) deadEntity;
            gameMap.getAiEntityController().queueEntityDespawn(aiEntity); // A mob died, despawn them!

            // If a AI entity kills and AI entity, do not drop ItemStack
            if (!(killerEntity instanceof Player)) return;

            // Give experience
            new ChatMessagePacketOut((Player) killerEntity, "You killed the enemy").sendPacket();
            new SkillExperiencePacketOut((Player) killerEntity, new ExperiencePacketInfo(SkillOpcodes.MELEE, aiEntity.getExpDrop())).sendPacket();

            // Give packetReceiver drop table item
            if (aiEntity.getDropTable() != null) {
                ItemStack itemStack = ValenguardMain.getInstance().getDropTableManager().dropItemOnMap(aiEntity.getDropTable(), 1);

                ItemStackDrop itemStackDrop = new ItemStackDrop();
                itemStackDrop.setEntityType(EntityType.ITEM_STACK);
                itemStackDrop.setName(itemStack.getName());
                itemStackDrop.setCurrentMapLocation(new Location(deadEntity.getCurrentMapLocation()));
                itemStackDrop.setAppearance(new Appearance((byte) 0, new short[]{(short) itemStack.getItemId()}));
                itemStackDrop.setItemStack(itemStack);
                itemStackDrop.setKiller((Player) killerEntity);
                itemStackDrop.setServerEntityId(gameMap.getLastItemStackDrop());

                gameMap.setLastItemStackDrop((short) (gameMap.getLastItemStackDrop() + 1));

                gameMap.getItemStackDropEntityController().queueEntitySpawn(itemStackDrop);
            }
        }
    }

    private boolean isGraveYardMap(Location location) {
        return location.getMapName().equals(PlayerConstants.STARTING_MAP);
    }
}
