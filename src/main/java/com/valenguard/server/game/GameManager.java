package com.valenguard.server.game;

import com.valenguard.server.game.entity.*;
import com.valenguard.server.game.maps.*;
import com.valenguard.server.network.PlayerSessionData;
import com.valenguard.server.network.packet.out.InitClientSessionPacket;
import com.valenguard.server.network.packet.out.PingOut;
import com.valenguard.server.network.shared.ClientHandler;
import com.valenguard.server.util.Log;
import com.valenguard.server.util.RandomUtil;
import lombok.Getter;

import java.io.File;
import java.util.HashMap;
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
    private static final String MAP_DIRECTORY = "src/main/resources/maps/";

    @Getter
    private final Map<String, GameMap> gameMaps = new HashMap<>();
    private final Queue<PlayerSessionData> playerSessionDataQueue = new ConcurrentLinkedQueue<>();

    private final Queue<Npc> npcToAdd = new ConcurrentLinkedQueue<>();

    public void init() {
        loadAllMaps();
    }

    public void initializeNewPlayer(PlayerSessionData playerSessionData) {
        playerSessionDataQueue.add(playerSessionData);
    }

    public void queueNpcAdd(Npc npc) {
        npcToAdd.add(npc);
    }

    private void processEntities() {
        for (Npc npc : npcToAdd) {
            gameMaps.get(npc.getMapName()).addNpc(npc);
        }
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

    private void playerJoinServer(PlayerSessionData playerSessionData) {
        //TODO: GET LAST LOGIN INFO FROM DATABASE, UNLESS PLAYER IS TRUE "NEW PLAYER."
        GameMap gameMap = gameMaps.get(NewPlayerConstants.STARTING_MAP);

        // Below we create a starting currentMapLocation for a new player.
        // The Y cord is subtracted from the height of the map.
        // The reason for this is because on the Tiled Editor
        // the Y cord is reversed.  This just makes our job
        // easier if we want to quickly grab a cord from the
        // Tiled Editor without doing the subtraction ourselves.
        Location location = new Location(NewPlayerConstants.STARTING_MAP,
                NewPlayerConstants.STARTING_X_CORD,
                gameMap.getMapHeight() - NewPlayerConstants.STARTING_Y_CORD);

        Player player = new Player();
        player.setEntityType(EntityType.PLAYER);
        player.setServerEntityId(playerSessionData.getServerID());
        player.setMoveSpeed(NewPlayerConstants.DEFAULT_MOVE_SPEED);
        player.setClientHandler(playerSessionData.getClientHandler());
        player.setName(Short.toString(playerSessionData.getServerID()));
        playerSessionData.getClientHandler().setPlayer(player);
        player.setAppearance(new Appearance(new short[]{(short) RandomUtil.getNewRandom(0, GameConstants.HUMAN_MAX_HEADS), (short) RandomUtil.getNewRandom(0, GameConstants.HUMAN_MAX_BODIES)}));

        Log.println(getClass(), "Sending initialize server id: " + playerSessionData.getServerID(), false, PRINT_DEBUG);

        new InitClientSessionPacket(player, true, playerSessionData.getServerID()).sendPacket();
        new PingOut(player).sendPacket();

        gameMap.addPlayer(player, new Warp(location, MoveDirection.DOWN));
    }

    public void playerQuitServer(Player player) {
        // TODO: Save player specific data
        GameMap gameMap = player.getGameMap();
        gameMap.removePlayer(player);
    }

    public void playerSwitchGameMap(Player player) {
        String currentMapName = player.getMapName();
        Warp warp = player.getWarp();
        Log.println(getClass(), "ToMap: " + warp.getLocation().getMapName() + ", FromMap: " + player.getMapName(), true, PRINT_DEBUG);
        checkArgument(!warp.getLocation().getMapName().equalsIgnoreCase(currentMapName),
                "The player is trying to switch to a game map they are already on. Map: " + warp.getLocation().getMapName());

        gameMaps.get(currentMapName).removePlayer(player);
        gameMaps.get(warp.getLocation().getMapName()).addPlayer(player, warp);
        player.setWarp(null);
    }

    public void gameMapTick() {
        gameMaps.values().forEach(GameMap::tickNPC);
//        gameMaps.values().forEach(GameMap::tickGroundItems);
        gameMaps.values().forEach(GameMap::tickPlayer);
        gameMaps.values().forEach(GameMap::sendPlayersPacket);
    }

    public int getTotalPlayersOnline() {
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
        processEntities();
    }

    private void fixWarpHeights() {
        gameMaps.values().forEach(gameMap -> {
            for (int i = 0; i < gameMap.getMapWidth(); i++) {
                for (int j = 0; j < gameMap.getMapHeight(); j++) {
                    if (gameMap.isOutOfBounds(i, j)) continue;
                    Warp warp = gameMap.getMap()[i][j].getWarp();
                    if (warp == null) continue;
                    warp.getLocation().setY(gameMaps.get(warp.getLocation().getMapName()).getMapHeight() - warp.getLocation().getY() - 1);
                }
            }
        });
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

    public void forAllMobsFiltered(Consumer<Entity> callback, Predicate<Entity> predicate) {
        gameMaps.values().forEach(gameMap -> gameMap.getMobList().stream().filter(predicate).forEach(callback));
    }
}
