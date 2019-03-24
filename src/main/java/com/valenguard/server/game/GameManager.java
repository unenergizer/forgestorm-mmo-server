package com.valenguard.server.game;

import com.valenguard.server.Server;
import com.valenguard.server.database.sql.PlayerDataSQL;
import com.valenguard.server.game.rpg.Attributes;
import com.valenguard.server.game.rpg.Reputation;
import com.valenguard.server.game.world.entity.*;
import com.valenguard.server.game.world.item.ItemStack;
import com.valenguard.server.game.world.maps.GameMap;
import com.valenguard.server.game.world.maps.Warp;
import com.valenguard.server.io.FilePaths;
import com.valenguard.server.io.TmxFileParser;
import com.valenguard.server.network.game.PlayerSessionData;
import com.valenguard.server.network.game.packet.out.ChatMessagePacketOut;
import com.valenguard.server.network.game.packet.out.InitClientSessionPacketOut;
import com.valenguard.server.network.game.packet.out.PingPacketOut;
import com.valenguard.server.network.game.shared.ClientHandler;
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
            aiEntity.getGameMap().getAiEntityController().queueEntitySpawn(aiEntity);
        }
        StationaryEntity stationaryEntity;
        while ((stationaryEntity = stationaryEntities.poll()) != null) {
            stationaryEntity.getGameMap().getStationaryEntityController().queueEntitySpawn(stationaryEntity);
        }
    }

    public void start() {
        loadAllMaps();
    }

    public void initializeNewPlayer(PlayerSessionData playerSessionData) {
        playerSessionDataQueue.add(playerSessionData);
    }

    /**
     * Ran on the game thread. Dump players from network.
     */
    void processPlayerJoin() {
        PlayerSessionData playerSessionData;
        while ((playerSessionData = playerSessionDataQueue.poll()) != null) {
            playerJoinServer(playerSessionData);
        }
    }

    private int tempColor = 0;

    private void playerJoinServer(PlayerSessionData playerSessionData) {

        Server.getInstance().getNetworkManager().getOutStreamManager().addClient(playerSessionData.getClientHandler());


        //TODO: GET LAST LOGIN INFO FROM DATABASE_SETTINGS, UNLESS PLAYER IS TRUE "NEW PLAYER."

        Player player = initializePlayer(playerSessionData);

        Log.println(getClass(), "Sending initialize server id: " + playerSessionData.getServerID(), false, PRINT_DEBUG);

        new InitClientSessionPacketOut(player, true, playerSessionData.getServerID()).sendPacket();
        new PingPacketOut(player).sendPacket();
        new ChatMessagePacketOut(player, "[Server] Welcome to Valenguard: Retro MMO!").sendPacket();

        player.getGameMap().getPlayerController().addPlayer(player, new Warp(player.getCurrentMapLocation(), player.getFacingDirection()));

        // Give test items
        ItemStack starterGold = Server.getInstance().getItemStackManager().makeItemStack(0, 100);
        player.giveItemStack(starterGold);

        ItemStack starterSword = Server.getInstance().getItemStackManager().makeItemStack(4, 1);
        player.giveItemStack(starterSword);

        tempColor++;
        if (tempColor > 15) tempColor = 0;

        for (GameMap mapSearch : gameMaps.values()) {
            for (Player playerSearch : mapSearch.getPlayerController().getPlayerList()) {
                if (playerSearch == player) continue;
                new ChatMessagePacketOut(playerSearch, player.getServerEntityId() + " has joined the server.").sendPacket();
            }
        }

        Log.println(getClass(), "PlayerJoin: " + player.getClientHandler().getSocket().getInetAddress().getHostAddress() + ", Online Players: " + (getTotalPlayersOnline() + 1));
    }

    private Player initializePlayer(final PlayerSessionData playerSessionData) {
        // todo this big chunk of io needs to be read from a database
        Player player = new Player();
        player.setEntityType(EntityType.PLAYER);
        player.setServerEntityId(playerSessionData.getServerID());
        player.setMoveSpeed(PlayerConstants.DEFAULT_MOVE_SPEED);
        player.setClientHandler(playerSessionData.getClientHandler());
        player.setName(playerSessionData.getUsername()); // todo get case sensitive name from database
        playerSessionData.getClientHandler().setPlayer(player);

        new PlayerDataSQL().loadSQL(player);

        // todo load this stuff from the database as well

        player.initEquipment();

        // Setup base packetReceiver attributes
        Attributes baseAttributes = new Attributes();
        baseAttributes.setArmor(PlayerConstants.BASE_ARMOR);
        baseAttributes.setDamage(PlayerConstants.BASE_DAMAGE);

        // Setup health
        player.setMaxHealth(PlayerConstants.BASE_HP); // todo this at some point will be a calculation

        player.setAttributes(baseAttributes);

        // TODO: Setup faction rep here
        short setThisFactionRep = 0;
        Reputation reputation = player.getReputation();
        for (int i = 0; i < reputation.getReputationData().length; i++) {
            reputation.getReputationData()[i] = setThisFactionRep;
        }

        player.getSkills().MINING.addExperience(50); // Initializes the packetReceiver with 50 mining experience.
        player.getSkills().MELEE.addExperience(170);

        return player;
    }

    void processPlayerQuit() {
        syncClientQuitQueue.forEach(clientHandler -> playerQuitServer(clientHandler.getPlayer()));
        syncClientQuitQueue.clear();
    }

    private void playerQuitServer(Player player) {

        new PlayerDataSQL().saveSQL(player);

        for (GameMap mapSearch : gameMaps.values()) {
            for (Player playerSearch : mapSearch.getPlayerController().getPlayerList()) {
                if (playerSearch == player) continue;
                new ChatMessagePacketOut(playerSearch, player.getServerEntityId() + " has quit the server.").sendPacket();
            }
        }
        // TODO: Save packetReceiver specific io
        GameMap gameMap = player.getGameMap();
        gameMap.getPlayerController().removePlayer(player);
        Server.getInstance().getTradeManager().ifTradeExistCancel(player, "[Server] Trade canceled. Player quit server.");
        Server.getInstance().getNetworkManager().getOutStreamManager().removeClient(player.getClientHandler());

        Log.println(getClass(), "PlayerQuit: " + player.getClientHandler().getSocket().getInetAddress().getHostAddress() + ", Online Players: " + (getTotalPlayersOnline() - 1));
    }

    public void playerSwitchGameMap(Player player) {
        String currentMapName = player.getMapName();
        Warp warp = player.getWarp();
        Log.println(getClass(), "ToMap: " + warp.getLocation().getMapName() + ", FromMap: " + player.getMapName(), true, PRINT_DEBUG);
        checkArgument(!warp.getLocation().getMapName().equalsIgnoreCase(currentMapName),
                "The packetReceiver is trying to switch to a game map they are already on. Map: " + warp.getLocation().getMapName());

        gameMaps.get(currentMapName).getPlayerController().removePlayer(player);
        gameMaps.get(warp.getLocation().getMapName()).getPlayerController().addPlayer(player, warp);
        player.setWarp(null);
    }

    void gameMapTick(long numberOfTicksPassed) {
        spawnEntities();
        gameMaps.values().forEach(gameMap -> gameMap.getStationaryEntityController().tick());
        gameMaps.values().forEach(gameMap -> gameMap.getAiEntityController().tick());
        gameMaps.values().forEach(gameMap -> gameMap.getItemStackDropEntityController().tick());
        gameMaps.values().forEach(gameMap -> gameMap.getPlayerController().tickPlayer());
        gameMaps.values().forEach(gameMap -> gameMap.getPlayerController().sendPlayersPacket());
        gameMaps.values().forEach(gameMap -> gameMap.getPlayerController().tickPlayerShuffle(numberOfTicksPassed));
    }

    private int getTotalPlayersOnline() {
        int onlinePlayers = 0;
        for (GameMap gameMap : gameMaps.values())
            onlinePlayers = onlinePlayers + gameMap.getPlayerController().getPlayerCount();
        return onlinePlayers;
    }

    private void loadAllMaps() {
        File[] files = new File(FilePaths.MAPS.getFilePath()).listFiles((d, name) -> name.endsWith(".tmx"));
        checkNotNull(files, "No game maps were loaded.");

        for (File file : files) {
            String mapName = file.getName().replace(".tmx", "");
            gameMaps.put(mapName, TmxFileParser.parseGameMap(FilePaths.MAPS.getFilePath(), mapName));
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
        player.getGameMap().getPlayerController().getPlayerList().forEach(playerOnMap -> {
            if (player.equals(playerOnMap)) return;
            callback.accept(playerOnMap.getClientHandler());
        });
    }

    public void forAllPlayers(Consumer<Player> callback) {
        gameMaps.values().forEach(gameMap -> gameMap.getPlayerController().getPlayerList().forEach(callback));
    }

    public void forAllPlayersFiltered(Consumer<Player> callback, Predicate<Player> predicate) {
        gameMaps.values().forEach(gameMap -> gameMap.getPlayerController().getPlayerList().stream().filter(predicate).forEach(callback));
    }

    public void forAllAiEntitiesFiltered(Consumer<Entity> callback, Predicate<AiEntity> predicate) {
        gameMaps.values().forEach(gameMap -> gameMap.getAiEntityController().getEntities().stream().filter(predicate).forEach(callback));
    }

    public Player findPlayer(short playerId) {
        for (GameMap gameMap : gameMaps.values()) {
            for (Player player : gameMap.getPlayerController().getPlayerList()) {
                if (player.getServerEntityId() == playerId) {
                    return player;
                }
            }
        }
        return null;
    }
}
