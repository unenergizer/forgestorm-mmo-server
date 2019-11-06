package com.valenguard.server.game.world.entity;

import com.valenguard.server.Server;
import com.valenguard.server.database.sql.GamePlayerCharacterSQL;
import com.valenguard.server.database.sql.GamePlayerExperienceSQL;
import com.valenguard.server.database.sql.GamePlayerInventorySQL;
import com.valenguard.server.database.sql.GamePlayerReputationSQL;
import com.valenguard.server.game.GameManager;
import com.valenguard.server.game.MessageText;
import com.valenguard.server.game.PlayerConstants;
import com.valenguard.server.game.ScreenType;
import com.valenguard.server.game.world.item.inventory.InventoryActions;
import com.valenguard.server.game.world.item.inventory.InventorySlot;
import com.valenguard.server.game.world.maps.GameMap;
import com.valenguard.server.game.world.maps.Warp;
import com.valenguard.server.network.game.packet.out.ChatMessagePacketOut;
import com.valenguard.server.network.game.packet.out.InitScreenPacketOut;
import com.valenguard.server.network.game.packet.out.InventoryPacketOut;
import com.valenguard.server.network.game.packet.out.PingPacketOut;
import com.valenguard.server.network.game.shared.ClientHandler;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class PlayerProcessor {

    private final GameManager gameManager;
    private final Queue<ClientHandler> playerJoinServerQueue = new ConcurrentLinkedQueue<>();
    private final Queue<ClientHandler> playerQuitServerQueue = new ConcurrentLinkedQueue<>();

    public PlayerProcessor(final GameManager gameManager) {
        this.gameManager = gameManager;
    }

    public void queuePlayerEnterGameWorld(ClientHandler clientHandler) {
        playerJoinServerQueue.add(clientHandler);
    }

    public void processPlayerJoinGameWorld() {
        ClientHandler clientHandler;
        while ((clientHandler = playerJoinServerQueue.poll()) != null) {
            Player player = playerLoad(clientHandler.getPlayer());
            playerWorldJoin(player);
        }
    }

    private Player playerLoad(Player player) {

        // Setting Entity Specific Data
        player.setEntityType(EntityType.PLAYER);

        player.getAttributes().setArmor(PlayerConstants.BASE_ARMOR);
        player.getAttributes().setDamage(PlayerConstants.BASE_DAMAGE);
        player.setMaxHealth(PlayerConstants.BASE_HP);

        // Setting MovingEntity Specific Data
        player.setMoveSpeed(PlayerConstants.DEFAULT_MOVE_SPEED);

        // Setting Player Specific Data
        new GamePlayerCharacterSQL().loadSQL(player);
        new GamePlayerInventorySQL().loadSQL(player);
        new GamePlayerExperienceSQL().loadSQL(player);
        new GamePlayerReputationSQL().loadSQL(player);

        return player;
    }

    private void playerWorldJoin(Player player) {
        player.setLoggedInGameWorld(true);

        new InitScreenPacketOut(player.getClientHandler(), ScreenType.GAME).sendPacket();
        new PingPacketOut(player).sendPacket();
        new ChatMessagePacketOut(player, MessageText.SERVER + "Welcome to Valenguard: Retro MMO!").sendPacket();

        // Add player to World
        player.getGameMap().getPlayerController().addPlayer(player, new Warp(player.getCurrentMapLocation(), player.getFacingDirection()));

        // Send player bag ItemStacks
        for (InventorySlot inventorySlot : player.getPlayerBag().getInventorySlotArray()) {
            if (inventorySlot.getItemStack() != null) {
                new InventoryPacketOut(player, new InventoryActions(InventoryActions.ActionType.SET_BAG, inventorySlot.getSlotIndex(), inventorySlot.getItemStack())).sendPacket();
            }
        }

        // Send player bank ItemStacks
        for (InventorySlot inventorySlot : player.getPlayerBank().getInventorySlotArray()) {
            if (inventorySlot.getItemStack() != null) {
                new InventoryPacketOut(player, new InventoryActions(InventoryActions.ActionType.SET_BANK, inventorySlot.getSlotIndex(), inventorySlot.getItemStack())).sendPacket();
            }
        }

        // Send player equipment ItemStacks
        for (InventorySlot inventorySlot : player.getPlayerEquipment().getInventorySlotArray()) {
            if (inventorySlot.getItemStack() != null) {
                new InventoryPacketOut(player, new InventoryActions(InventoryActions.ActionType.SET_EQUIPMENT, inventorySlot.getSlotIndex(), inventorySlot.getItemStack())).sendPacket();
            }
        }

        // Send other players join message
        for (GameMap mapSearch : gameManager.getGameMapProcessor().getGameMaps().values()) {
            for (Player playerSearch : mapSearch.getPlayerController().getPlayerList()) {
                if (playerSearch == player) continue;
                new ChatMessagePacketOut(playerSearch, player.getName() + " has joined the server.").sendPacket();
            }
        }

        // Send player Skill Experience
        player.getSkills().MELEE.sendSkillExperience();
        player.getSkills().MINING.sendSkillExperience();
    }

    public void queuePlayerQuitGameWorld(ClientHandler clientHandler) {
        playerQuitServerQueue.add(clientHandler);
    }

    public void processPlayerQuit() {
        for (ClientHandler clientHandler : playerQuitServerQueue) {
            Player player = clientHandler.getPlayer();

            if (player == null) return;

            savePlayer(player);
            playerWorldQuit(player);
        }
        playerQuitServerQueue.clear();
    }

    private void savePlayer(Player player) {
        new GamePlayerCharacterSQL().saveSQL(player);
        new GamePlayerInventorySQL().saveSQL(player);
        new GamePlayerExperienceSQL().saveSQL(player);
        new GamePlayerReputationSQL().saveSQL(player);
    }

    private void playerWorldQuit(Player player) {
        player.setLoggedInGameWorld(false);

        for (GameMap mapSearch : gameManager.getGameMapProcessor().getGameMaps().values()) {
            for (Player playerSearch : mapSearch.getPlayerController().getPlayerList()) {
                if (playerSearch == player) continue;
                new ChatMessagePacketOut(playerSearch, player.getName() + " has quit the server.").sendPacket();
            }
        }

        GameMap gameMap = player.getGameMap();
        gameMap.getPlayerController().removePlayer(player);
        Server.getInstance().getTradeManager().ifTradeExistCancel(player, MessageText.SERVER + "Trade canceled. Player quit server.");
    }
}
