package com.valenguard.server.game.world.entity;

import com.valenguard.server.Server;
import com.valenguard.server.database.CharacterSaveProgressType;
import com.valenguard.server.database.sql.GamePlayerCharacterSQL;
import com.valenguard.server.database.sql.GamePlayerExperienceSQL;
import com.valenguard.server.database.sql.GamePlayerInventorySQL;
import com.valenguard.server.database.sql.GamePlayerReputationSQL;
import com.valenguard.server.game.GameManager;
import com.valenguard.server.game.MessageText;
import com.valenguard.server.game.PlayerConstants;
import com.valenguard.server.game.UserInterfaceType;
import com.valenguard.server.game.world.item.inventory.InventoryActions;
import com.valenguard.server.game.world.item.inventory.InventorySlot;
import com.valenguard.server.game.world.item.inventory.InventoryType;
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
            Player player = playerLoad(clientHandler);
            playerWorldJoin(player);
        }
    }

    private Player playerLoad(ClientHandler clientHandler) {
        Player player = clientHandler.getPlayer();

        // Setting Entity Specific Data
        player.setEntityType(EntityType.PLAYER);

        player.getAttributes().setArmor(PlayerConstants.BASE_ARMOR);
        player.getAttributes().setDamage(PlayerConstants.BASE_DAMAGE);
        player.setMaxHealth(PlayerConstants.BASE_HP);

        // Setting MovingEntity Specific Data
        player.setMoveSpeed(PlayerConstants.DEFAULT_MOVE_SPEED);

        // Setting Player Specific Data
        new GamePlayerCharacterSQL().loadSQL(clientHandler);
        new GamePlayerInventorySQL().loadSQL(clientHandler);
        new GamePlayerExperienceSQL().loadSQL(clientHandler);
        new GamePlayerReputationSQL().loadSQL(clientHandler);

        return player;
    }

    private void playerWorldJoin(Player player) {
        player.setLoggedInGameWorld(true);

        new InitScreenPacketOut(player.getClientHandler(), UserInterfaceType.GAME).sendPacket();
        new PingPacketOut(player).sendPacket();
        new ChatMessagePacketOut(player, MessageText.SERVER + "Welcome to RetroMMO!").sendPacket();

        // Add player to World
        if (player.getGameMap() == null) {
            // If the game map returns null here, we are trying to
            // add the player to a world that no longer exists.
            // Instead let's send them to the default spawn.
            player.setCurrentMapLocation(PlayerConstants.RESPAWN_LOCATION);
            player.setFutureMapLocation(PlayerConstants.RESPAWN_LOCATION);
            new ChatMessagePacketOut(player, MessageText.ERROR + "The map you were on could not be loaded. Sending you to the default spawn location.").sendPacket();
        }
        player.getGameMap().getPlayerController().addPlayer(player, new Warp(player.getCurrentMapLocation(), player.getFacingDirection()));

        // Send player bag ItemStacks
        for (InventorySlot inventorySlot : player.getPlayerBag().getInventorySlotArray()) {
            if (inventorySlot.getItemStack() != null) {
                new InventoryPacketOut(player, new InventoryActions().set(InventoryType.BAG_1, inventorySlot.getSlotIndex(), inventorySlot.getItemStack())).sendPacket();
            }
        }

        // Send player bank ItemStacks
        for (InventorySlot inventorySlot : player.getPlayerBank().getInventorySlotArray()) {
            if (inventorySlot.getItemStack() != null) {
                new InventoryPacketOut(player, new InventoryActions().set(InventoryType.BANK, inventorySlot.getSlotIndex(), inventorySlot.getItemStack())).sendPacket();
            }
        }

        // Send player equipment ItemStacks
        for (InventorySlot inventorySlot : player.getPlayerEquipment().getInventorySlotArray()) {
            if (inventorySlot.getItemStack() != null) {
                new InventoryPacketOut(player, new InventoryActions().set(InventoryType.EQUIPMENT, inventorySlot.getSlotIndex(), inventorySlot.getItemStack())).sendPacket();
            }
        }

        // Send player equipment ItemStacks
        for (InventorySlot inventorySlot : player.getPlayerHotBar().getInventorySlotArray()) {
            if (inventorySlot.getItemStack() != null) {
                new InventoryPacketOut(player, new InventoryActions().set(InventoryType.HOT_BAR, inventorySlot.getSlotIndex(), inventorySlot.getItemStack())).sendPacket();
            }
        }

        // Send other players join message
        for (GameMap mapSearch : gameManager.getGameMapProcessor().getGameMaps().values()) {
            for (Player playerSearch : mapSearch.getPlayerController().getPlayerList()) {
                if (playerSearch == player) continue;
                new ChatMessagePacketOut(playerSearch, "[GREEN]" + player.getName() + " has joined the server.").sendPacket();
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

            playerWorldQuit(player);
            savePlayer(clientHandler);
        }
        playerQuitServerQueue.clear();
    }

    private void savePlayer(ClientHandler clientHandler) {
        new GamePlayerCharacterSQL().saveSQL(clientHandler, CharacterSaveProgressType.CHARACTER_SAVED);
        new GamePlayerInventorySQL().saveSQL(clientHandler, CharacterSaveProgressType.INVENTORY_SAVED);
        new GamePlayerExperienceSQL().saveSQL(clientHandler, CharacterSaveProgressType.EXPERIENCE_SAVED);
        new GamePlayerReputationSQL().saveSQL(clientHandler, CharacterSaveProgressType.REPUTATION_SAVED);
    }

    private void playerWorldQuit(Player player) {
        player.setLoggedInGameWorld(false);

        for (GameMap mapSearch : gameManager.getGameMapProcessor().getGameMaps().values()) {
            for (Player playerSearch : mapSearch.getPlayerController().getPlayerList()) {
                if (playerSearch == player) continue;
                new ChatMessagePacketOut(playerSearch, "[ORANGE]" + player.getName() + " has quit the server.").sendPacket();
            }
        }

        GameMap gameMap = player.getGameMap();
        gameMap.getPlayerController().removePlayer(player);
        Server.getInstance().getTradeManager().ifTradeExistCancel(player, MessageText.SERVER + "Trade canceled. Player quit server.");
    }
}
