package com.valenguard.server.game;

import com.valenguard.server.ValenguardMain;
import com.valenguard.server.game.data.TmxFileParser;
import com.valenguard.server.game.entity.*;
import com.valenguard.server.game.inventory.ItemStack;
import com.valenguard.server.game.inventory.ItemStackType;
import com.valenguard.server.game.maps.GameMap;
import com.valenguard.server.game.maps.Location;
import com.valenguard.server.game.maps.MoveDirection;
import com.valenguard.server.game.maps.Warp;
import com.valenguard.server.game.rpg.Attributes;
import com.valenguard.server.game.rpg.EntityAlignment;
import com.valenguard.server.network.PlayerSessionData;
import com.valenguard.server.network.packet.out.ChatMessagePacketOut;
import com.valenguard.server.network.packet.out.InitClientSessionPacketOut;
import com.valenguard.server.network.packet.out.PingPacketOut;
import com.valenguard.server.network.shared.ClientHandler;
import com.valenguard.server.util.Log;
import lombok.Getter;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class GameManager {

    private static final boolean PRINT_DEBUG = false;
    public static final int PLAYERS_TO_PROCESS = 50;
    private static final String MAP_DIRECTORY = "src/main/resources/data/maps/";

    @Getter
    private final Map<String, GameMap> gameMaps = new HashMap<>();
    private final Queue<PlayerSessionData> playerSessionDataQueue = new ConcurrentLinkedQueue<>();

    private final Queue<ClientHandler> syncClientQuitQueue = new ConcurrentLinkedQueue<>();

    /**
     * This queue is needed because the entities need to be queued to spawn before
     * the map object is created.
     */
    private final Queue<AiEntity> aiEntitiesToSpawn = new LinkedList<>();
    private final Queue<StationaryEntity> stationaryEntities = new LinkedList<>();

    public void queueClientQuitServer(ClientHandler clientHandler) {
        syncClientQuitQueue.add(clientHandler);
    }

    public void queueMobSpawn(AiEntity aiEntity) {
        aiEntitiesToSpawn.add(aiEntity);
    }

    public void queueStationarySpawn(StationaryEntity stationaryEntity) {
        stationaryEntities.add(stationaryEntity);
    }

    private void spawnEntities() {
        AiEntity aiEntity;
        while ((aiEntity = aiEntitiesToSpawn.poll()) != null) {
            aiEntity.getGameMap().queueAiEntitySpawn(aiEntity);
        }
        StationaryEntity stationaryEntity;
        while ((stationaryEntity = stationaryEntities.poll()) != null) {
            stationaryEntity.getGameMap().queueStationarySpawn(stationaryEntity);
        }
    }

    public void init() {
        loadAllMaps();
    }

    public void initializeNewPlayer(PlayerSessionData playerSessionData) {
        playerSessionDataQueue.add(playerSessionData);
    }

    /**
     * Ran on the game thread. Dump players from network.
     */
    public void processPlayerJoin() {
        PlayerSessionData playerSessionData;
        while ((playerSessionData = playerSessionDataQueue.poll()) != null) {
            playerJoinServer(playerSessionData);
        }
    }

    private int tempColor = 0;

    private void playerJoinServer(PlayerSessionData playerSessionData) {

        ValenguardMain.getInstance().getOutStreamManager().addClient(playerSessionData.getClientHandler());

        //TODO: GET LAST LOGIN INFO FROM DATABASE, UNLESS PLAYER IS TRUE "NEW PLAYER."
        GameMap gameMap = gameMaps.get(PlayerConstants.STARTING_MAP);

        // Below we create a starting currentMapLocation for a new packetReceiver.
        // The Y cord is subtracted from the height of the map.
        // The reason for this is because on the Tiled Editor
        // the Y cord is reversed.  This just makes our job
        // easier if we want to quickly grab a cord from the
        // Tiled Editor without doing the subtraction ourselves.
        Location location = new Location(PlayerConstants.STARTING_MAP,
                PlayerConstants.STARTING_X_CORD,
                (short) (gameMap.getMapHeight() - PlayerConstants.STARTING_Y_CORD));

        Player player = initializePlayer(playerSessionData);

        Log.println(getClass(), "Sending initialize server id: " + playerSessionData.getServerID(), false, PRINT_DEBUG);

        new InitClientSessionPacketOut(player, true, playerSessionData.getServerID()).sendPacket();
        new PingPacketOut(player).sendPacket();
        new ChatMessagePacketOut(player, "[Server] Welcome to Valenguard: Retro MMO!").sendPacket();


        gameMap.addPlayer(player, new Warp(location, MoveDirection.SOUTH));

        // Give test items
        for (int itemId = 0; itemId <= ValenguardMain.getInstance().getItemStackManager().numberOfItems() - 1; itemId++) {
            ItemStack itemStack = ValenguardMain.getInstance().getItemStackManager().makeItemStack(itemId, 1);

            if (itemStack.getItemStackType() == ItemStackType.GOLD) itemStack.setAmount(111);

            player.giveItemStack(itemStack);

        }

        tempColor++;
        if (tempColor > 15) tempColor = 0;

        for (GameMap mapSearch : gameMaps.values()) {
            for (Player playerSearch : mapSearch.getPlayerList()) {
                if (playerSearch == player) continue;
                new ChatMessagePacketOut(playerSearch, player.getServerEntityId() + " has joined the server.").sendPacket();
            }
        }

        Log.println(getClass(), "PlayerJoin: " + player.getClientHandler().getSocket().getInetAddress().getHostAddress() + ", Online Players: " + (getTotalPlayersOnline() + 1));
    }

    private Player initializePlayer(final PlayerSessionData playerSessionData) {
        // todo this big chunk of data needs to be read from a database
        Player player = new Player();
        player.setEntityType(EntityType.PLAYER);
        player.setServerEntityId(playerSessionData.getServerID());
        player.setMoveSpeed(PlayerConstants.DEFAULT_MOVE_SPEED);
        player.setClientHandler(playerSessionData.getClientHandler());
        player.setName("Player " + Short.toString(playerSessionData.getServerID()));
        playerSessionData.getClientHandler().setPlayer(player);
        short[] initialPlayerTextureIds = new short[4];
        initialPlayerTextureIds[Appearance.BODY] = 0;
        initialPlayerTextureIds[Appearance.HEAD] = 0;
        initialPlayerTextureIds[Appearance.ARMOR] = -1;
        initialPlayerTextureIds[Appearance.HELM] = -1;
        player.setAppearance(new Appearance((byte) tempColor, initialPlayerTextureIds));
        player.initEquipment();

        // Setup base packetReceiver attributes
        Attributes baseAttributes = new Attributes();
        baseAttributes.setArmor(PlayerConstants.BASE_ARMOR);
        baseAttributes.setDamage(PlayerConstants.BASE_DAMAGE);

        // Setup health
        player.setCurrentHealth(PlayerConstants.BASE_HP);
        player.setMaxHealth(PlayerConstants.BASE_HP);

        player.setAttributes(baseAttributes);

        player.setEntityAlignment(EntityAlignment.FRIENDLY);

        player.getSkills().MINING.addExperience(50); // Initializes the packetReceiver with 50 mining experience.
        player.getSkills().MELEE.addExperience(170);

        return player;
    }

    public void processPlayerQuit() {
        syncClientQuitQueue.forEach(clientHandler -> playerQuitServer(clientHandler.getPlayer()));
        syncClientQuitQueue.clear();
    }

    private void playerQuitServer(Player player) {
        for (GameMap mapSearch : gameMaps.values()) {
            for (Player playerSearch : mapSearch.getPlayerList()) {
                if (playerSearch == player) continue;
                new ChatMessagePacketOut(playerSearch, player.getServerEntityId() + " has quit the server.").sendPacket();
            }
        }
        // TODO: Save packetReceiver specific data
        GameMap gameMap = player.getGameMap();
        gameMap.removePlayer(player);
        ValenguardMain.getInstance().getTradeManager().ifTradeExistCancel(player, "[Server] Trade canceled. Player quit server.");
        ValenguardMain.getInstance().getOutStreamManager().removeClient(player.getClientHandler());

        Log.println(getClass(), "PlayerQuit: " + player.getClientHandler().getSocket().getInetAddress().getHostAddress() + ", Online Players: " + (getTotalPlayersOnline() - 1));
    }

    public void playerSwitchGameMap(Player player) {
        String currentMapName = player.getMapName();
        Warp warp = player.getWarp();
        Log.println(getClass(), "ToMap: " + warp.getLocation().getMapName() + ", FromMap: " + player.getMapName(), true, PRINT_DEBUG);
        checkArgument(!warp.getLocation().getMapName().equalsIgnoreCase(currentMapName),
                "The packetReceiver is trying to switch to a game map they are already on. Map: " + warp.getLocation().getMapName());

        gameMaps.get(currentMapName).removePlayer(player);
        gameMaps.get(warp.getLocation().getMapName()).addPlayer(player, warp);
        player.setWarp(null);
    }

    public void gameMapTick(long numberOfTicksPassed) {
        spawnEntities();
        gameMaps.values().forEach(GameMap::tickStationaryEntities);
        gameMaps.values().forEach(GameMap::tickAiEntity);
        gameMaps.values().forEach(GameMap::tickItemStackDrop);
        gameMaps.values().forEach(GameMap::tickPlayer);
        gameMaps.values().forEach(GameMap::sendPlayersPacket);
        gameMaps.values().forEach(gameMap -> gameMap.tickPlayerShuffle(numberOfTicksPassed));
    }

    private int getTotalPlayersOnline() {
        int onlinePlayers = 0;
        for (GameMap gameMap : gameMaps.values()) onlinePlayers = onlinePlayers + gameMap.getPlayerCount();
        return onlinePlayers;
    }

    private void loadAllMaps() {
        File[] files = new File(MAP_DIRECTORY).listFiles((d, name) -> name.endsWith(".tmx"));
        checkNotNull(files, "No game maps were loaded.");

        for (File file : files) {
            String mapName = file.getName().replace(".tmx", "");
            gameMaps.put(mapName, TmxFileParser.parseGameMap(MAP_DIRECTORY, mapName));
        }

        Log.println(getClass(), "Tmx Maps Loaded: " + files.length);
        fixWarpHeights();
    }

    private void fixWarpHeights() {
        for (GameMap gameMap : gameMaps.values()) {
            for (short i = 0; i < gameMap.getMapWidth(); i++) {
                for (short j = 0; j < gameMap.getMapHeight(); j++) {
                    if (gameMap.isOutOfBounds(i, j)) continue;
                    Warp warp = gameMap.getMap()[i][j].getWarp();
                    if (warp == null) continue;
                    warp.getLocation().setY((short) (gameMaps.get(warp.getLocation().getMapName()).getMapHeight() - warp.getLocation().getY() - 1));
                }
            }
        }
    }

    public GameMap getGameMap(String mapName) throws RuntimeException {
        checkNotNull(gameMaps.get(mapName), "Tried to get the map " + mapName + ", but it doesn't exist or was not loaded.");
        return gameMaps.get(mapName);
    }

    public void sendToAllButPlayer(Player player, Consumer<ClientHandler> callback) {
        player.getGameMap().getPlayerList().forEach(playerOnMap -> {
            if (player.equals(playerOnMap)) return;
            callback.accept(playerOnMap.getClientHandler());
        });
    }

    public void forAllPlayers(Consumer<Player> callback) {
        gameMaps.values().forEach(gameMap -> gameMap.getPlayerList().forEach(callback));
    }

    public void forAllPlayersFiltered(Consumer<Player> callback, Predicate<Player> predicate) {
        gameMaps.values().forEach(gameMap -> gameMap.getPlayerList().stream().filter(predicate).forEach(callback));
    }

    public void forAllAiEntitiesFiltered(Consumer<Entity> callback, Predicate<AiEntity> predicate) {
        gameMaps.values().forEach(gameMap -> gameMap.getAiEntityMap().values().stream().filter(predicate).forEach(callback));
    }

    public Player findPlayer(short playerId) {
        for (GameMap gameMap : gameMaps.values()) {
            for (Player player : gameMap.getPlayerList()) {
                if (player.getServerEntityId() == playerId) {
                    return player;
                }
            }
        }
        return null;
    }
}
