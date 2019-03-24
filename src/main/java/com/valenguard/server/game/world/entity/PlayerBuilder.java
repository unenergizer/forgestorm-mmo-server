package com.valenguard.server.game.world.entity;

import com.valenguard.server.database.sql.PlayerDataSQL;
import com.valenguard.server.database.sql.PlayerInventorySQL;
import com.valenguard.server.game.PlayerConstants;
import com.valenguard.server.game.rpg.Reputation;
import com.valenguard.server.game.world.item.inventory.InventoryActions;
import com.valenguard.server.game.world.item.inventory.InventorySlot;
import com.valenguard.server.game.world.maps.Warp;
import com.valenguard.server.network.game.PlayerSessionData;
import com.valenguard.server.network.game.packet.out.ChatMessagePacketOut;
import com.valenguard.server.network.game.packet.out.InitClientSessionPacketOut;
import com.valenguard.server.network.game.packet.out.InventoryPacketOut;
import com.valenguard.server.network.game.packet.out.PingPacketOut;

public class PlayerBuilder {

    public Player loadPlayer(final PlayerSessionData playerSessionData) {
        Player player = new Player(playerSessionData.getClientHandler());
        playerSessionData.getClientHandler().setPlayer(player);

        // Setting Entity Specific Data
        player.setServerEntityId(playerSessionData.getServerID());
        player.setEntityType(EntityType.PLAYER);
        player.setName(playerSessionData.getUsername());

        // Setting MovingEntity Specific Data
        player.setMoveSpeed(PlayerConstants.DEFAULT_MOVE_SPEED);

        // Setting Player Specific Data
        new PlayerDataSQL().loadSQL(player);
        new PlayerInventorySQL().loadSQL(player);

        // TODO: Attributes to be calculated later from character stats
        player.getAttributes().setArmor(PlayerConstants.BASE_ARMOR);
        player.getAttributes().setDamage(PlayerConstants.BASE_DAMAGE);
        player.setMaxHealth(PlayerConstants.BASE_HP);

        // TODO: Load faction reputation from the database
        short setThisFactionRep = 0;
        Reputation reputation = player.getReputation();
        for (int i = 0; i < reputation.getReputationData().length; i++) {
            reputation.getReputationData()[i] = setThisFactionRep;
        }

        // TODO: Load experience from the database
        player.getSkills().MINING.addExperience(50); // Initializes the packetReceiver with 50 mining experience.
        player.getSkills().MELEE.addExperience(170);

        return player;
    }

    public void playerJoinWorld(Player player) {
        new InitClientSessionPacketOut(player, true, player.getServerEntityId()).sendPacket();
        new PingPacketOut(player).sendPacket();
        new ChatMessagePacketOut(player, "[Server] Welcome to Valenguard: Retro MMO!").sendPacket();

        // Add player to World
        player.getGameMap().getPlayerController().addPlayer(player, new Warp(player.getCurrentMapLocation(), player.getFacingDirection()));

        // Send player bag ItemStacks
        for (InventorySlot inventorySlot : player.getPlayerBag().getBagSlots()) {
            if (inventorySlot.getItemStack() != null) {
                new InventoryPacketOut(player, new InventoryActions(InventoryActions.SET_BAG, inventorySlot.getSlotIndex(), inventorySlot.getItemStack())).sendPacket();
            }
        }

        // Send player equipment ItemStacks
        for (InventorySlot inventorySlot : player.getPlayerEquipment().getEquipmentSlots()) {
            if (inventorySlot.getItemStack() != null) {
                new InventoryPacketOut(player, new InventoryActions(InventoryActions.SET_EQUIPMENT, inventorySlot.getSlotIndex(), inventorySlot.getItemStack())).sendPacket();
            }
        }
    }
}
