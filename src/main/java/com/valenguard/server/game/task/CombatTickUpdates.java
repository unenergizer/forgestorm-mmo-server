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

    public void tickCombat(long numberOfTicksPassed) {
        for (GameMap gameMap : ValenguardMain.getInstance().getGameManager().getGameMaps().values()) {
            tickGameMapCombat(gameMap, numberOfTicksPassed);
        }
    }

    public void tickGameMapCombat(GameMap gameMap, long numberOfTicksPassed) {
        if (numberOfTicksPassed % 60 == 0) {

            // Now do combat
            for (MovingEntity movingEntity : gameMap.getAiEntityMap().values()) {
                if (movingEntity.getTargetEntity() == null) continue;

                MovingEntity targetEntity = movingEntity.getTargetEntity();
                // Check for distance
                if (movingEntity.getCurrentMapLocation().isWithinDistance(targetEntity.getFutureMapLocation(), (short) 1)
                        || movingEntity.getCurrentMapLocation().isWithinDistance(targetEntity.getCurrentMapLocation(), (short) 1)) {

                    // Entity vs Target
                    Attributes movingEntityAttributes = movingEntity.getAttributes();
                    Attributes targetEntityAttributes = targetEntity.getAttributes();
                    targetEntity.setCurrentHealth(targetEntity.getCurrentHealth() - movingEntityAttributes.getDamage());

                    // Target vs Entity
                    if (targetEntity.getTargetEntity() == movingEntity && targetEntity instanceof Player) {
                        movingEntity.setCurrentHealth(movingEntity.getCurrentHealth() - targetEntityAttributes.getDamage());
                    } else {
                        movingEntity.setCurrentHealth(movingEntity.getCurrentHealth() - targetEntityAttributes.getDamage());
                    }

                    sendCombatMessage(gameMap, movingEntity, targetEntity);

                    if (movingEntity.getCurrentHealth() <= 0) {
                        finishCombat(gameMap, targetEntity, movingEntity);
                    }

                    if (targetEntity.getCurrentHealth() <= 0) {
                        finishCombat(gameMap, movingEntity, targetEntity);
                    }
                }
            }
        }
    }

    private void sendCombatMessage(GameMap gameMap, MovingEntity attackerEntity, MovingEntity targetEntity) {
        Attributes attackerEntityAttributes = attackerEntity.getAttributes();
        Attributes targetEntityAttributes = targetEntity.getAttributes();

        if (attackerEntity instanceof Player) {
            new ChatMessagePacketOut((Player) attackerEntity, "Your HP: " + attackerEntity.getCurrentHealth() + " Damage Delt: " + targetEntityAttributes.getDamage()).sendPacket();
            new ChatMessagePacketOut((Player) attackerEntity, "Enemy HP: " + targetEntity.getCurrentHealth() + " Damage Delt: " + attackerEntityAttributes.getDamage()).sendPacket();
        }
        if (targetEntity instanceof Player) {
            new ChatMessagePacketOut((Player) targetEntity, "Enemy HP: " + attackerEntity.getCurrentHealth() + " Damage Delt: " + targetEntityAttributes.getDamage()).sendPacket();
            new ChatMessagePacketOut((Player) targetEntity, "Your HP: " + targetEntity.getCurrentHealth() + " Damage Delt: " + attackerEntityAttributes.getDamage()).sendPacket();
        }

        gameMap.forAllPlayers(player -> {
            new EntityDamagePacketOut(player, attackerEntity, attackerEntity.getCurrentHealth(), targetEntityAttributes.getDamage()).sendPacket();
            new EntityDamagePacketOut(player, targetEntity, targetEntity.getCurrentHealth(), attackerEntityAttributes.getDamage()).sendPacket();
        });
    }

    private void finishCombat(GameMap gameMap, MovingEntity killerEntity, MovingEntity deadEntity) {

        // Remove the deadEntity from all entities target!
        gameMap.releaseEntityTargets(deadEntity);
        deadEntity.setTargetEntity(null);

        if (killerEntity instanceof Player) {
            new ChatMessagePacketOut((Player) killerEntity, "You killed the enemy").sendPacket();
            new SkillExperiencePacketOut((Player) killerEntity, new ExperiencePacketInfo(SkillOpcodes.MELEE, 10)).sendPacket();
        }

        if (deadEntity instanceof Player) {
            // Player Died. Lets respawn them!

            Location teleportLocation = new Location(PlayerConstants.STARTING_MAP, PlayerConstants.RESPAWN_X_CORD, (short) (gameMap.getMapHeight() - PlayerConstants.RESPAWN_Y_CORD));
            MoveDirection facingDirection = MoveDirection.SOUTH;

            // Do a reheal
            Player deadPlayer = (Player) deadEntity;
            deadPlayer.setCurrentHealth(deadPlayer.getMaxHealth());

            // Check to see if the player needs to change maps!
            if (!deadPlayer.getMapName().equals(PlayerConstants.STARTING_MAP)) {
                println(getClass(), "Warping player to graveyard map!");

                // Warp player to graveyard
                deadPlayer.setWarp(new Warp(teleportLocation, facingDirection));
            } else {
                println(getClass(), "Teleporting player to graveyard!");

                // Teleport player
                deadEntity.setCurrentMapLocation(teleportLocation);
                deadEntity.setFutureMapLocation(teleportLocation);
                deadEntity.setFacingDirection(facingDirection);

                // Send all players in map the teleport packet
                gameMap.forAllPlayers(player -> new PlayerTeleportPacketOut(player, deadEntity, teleportLocation, facingDirection).sendPacket());

                // Send other players info about the reheal (if they are still on the same map)
                gameMap.forAllPlayers(player -> new EntityHealPacketOut(player, deadEntity, player.getMaxHealth()).sendPacket());
            }

            new ChatMessagePacketOut((Player) deadEntity, "You died! Respawning you in graveyard!").sendPacket();

        } else {
            gameMap.queueAiEntityDespawn(deadEntity); // A mob died, despawn them!

            // If a AI entity kills and AI entity, do not drop ItemStack
            if (!(killerEntity instanceof Player)) return;

            // Give player drop table item
            if (((AIEntity) deadEntity).getDropTableID() != null) {
                ItemStack itemStack = ValenguardMain.getInstance().getDropTableManager().dropItemOnMap(((AIEntity) deadEntity).getDropTableID(), 1);

                ItemStackDrop itemStackDrop = new ItemStackDrop();
                itemStackDrop.setEntityType(EntityType.ITEM_STACK);
                itemStackDrop.setName(itemStack.getName());
                itemStackDrop.setCurrentMapLocation(new Location(deadEntity.getCurrentMapLocation()));
                itemStackDrop.setAppearance(new Appearance((byte) 0, new short[]{(short) itemStack.getItemId()}));
                itemStackDrop.setItemStack(itemStack);
                itemStackDrop.setKiller((Player) killerEntity);
                itemStackDrop.setServerEntityId(gameMap.getLastItemStackDrop());

                println(getClass(), "<creation> ItemStackDrop ID: " + itemStackDrop.getServerEntityId());

                gameMap.setLastItemStackDrop((short) (gameMap.getLastItemStackDrop() + 1));

                gameMap.queueItemStackDropSpawn(itemStackDrop);
            }
        }
    }
}
