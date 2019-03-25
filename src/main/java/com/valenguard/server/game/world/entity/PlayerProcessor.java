package com.valenguard.server.game.world.entity;

import com.valenguard.server.Server;
import com.valenguard.server.database.sql.PlayerDataSQL;
import com.valenguard.server.database.sql.PlayerExperienceSQL;
import com.valenguard.server.database.sql.PlayerInventorySQL;
import com.valenguard.server.database.sql.PlayerReputationSQL;
import com.valenguard.server.game.GameManager;
import com.valenguard.server.game.PlayerConstants;
import com.valenguard.server.game.world.item.inventory.InventoryActions;
import com.valenguard.server.game.world.item.inventory.InventorySlot;
import com.valenguard.server.game.world.maps.GameMap;
import com.valenguard.server.game.world.maps.Warp;
import com.valenguard.server.network.game.PlayerSessionData;
import com.valenguard.server.network.game.packet.out.ChatMessagePacketOut;
import com.valenguard.server.network.game.packet.out.InitClientSessionPacketOut;
import com.valenguard.server.network.game.packet.out.InventoryPacketOut;
import com.valenguard.server.network.game.packet.out.PingPacketOut;

public class PlayerProcessor {

    private final GameManager gameManager;

    public PlayerProcessor(final GameManager gameManager) {
        this.gameManager = gameManager;
    }

    public Player loadPlayer(final PlayerSessionData playerSessionData) {
        Player player = new Player(playerSessionData.getClientHandler());
        playerSessionData.getClientHandler().setPlayer(player);

        // Setting Entity Specific Data
        player.setServerEntityId(playerSessionData.getServerID());
        player.setEntityType(EntityType.PLAYER);
        player.setName(playerSessionData.getUsername());

        player.getAttributes().setArmor(PlayerConstants.BASE_ARMOR);
        player.getAttributes().setDamage(PlayerConstants.BASE_DAMAGE);
        player.setMaxHealth(PlayerConstants.BASE_HP);

        // Setting MovingEntity Specific Data
        player.setMoveSpeed(PlayerConstants.DEFAULT_MOVE_SPEED);

        // Setting Player Specific Data
        new PlayerDataSQL().loadSQL(player);
        new PlayerInventorySQL().loadSQL(player);
        new PlayerExperienceSQL().loadSQL(player);
        new PlayerReputationSQL().loadSQL(player);

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

    public void savePlayer(Player player) {
        new PlayerDataSQL().saveSQL(player);
        new PlayerInventorySQL().saveSQL(player);
        new PlayerExperienceSQL().saveSQL(player);
        new PlayerReputationSQL().saveSQL(player);
    }

    public void playerQuitWorld(Player player) {
        for (GameMap mapSearch : gameManager.getGameMaps().values()) {
            for (Player playerSearch : mapSearch.getPlayerController().getPlayerList()) {
                if (playerSearch == player) continue;
                new ChatMessagePacketOut(playerSearch, player.getServerEntityId() + " has quit the server.").sendPacket();
            }
        }

        GameMap gameMap = player.getGameMap();
        gameMap.getPlayerController().removePlayer(player);
        Server.getInstance().getTradeManager().ifTradeExistCancel(player, "[Server] Trade canceled. Player quit server.");
        Server.getInstance().getNetworkManager().getOutStreamManager().removeClient(player.getClientHandler());
    }
}
