package com.valenguard.server.game;

import com.google.common.base.Preconditions;
import com.valenguard.server.game.entity.Player;
import com.valenguard.server.game.maps.*;
import com.valenguard.server.network.PlayerSessionData;
import com.valenguard.server.network.packet.out.InitClientPacket;
import com.valenguard.server.network.packet.out.PingOut;
import com.valenguard.server.network.packet.out.PlayerSwitchMapPacket;
import com.valenguard.server.network.shared.ClientHandler;
import com.valenguard.server.util.Log;
import lombok.Getter;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class GameManager {

    public static final int PLAYERS_TO_PROCESS = 50;
    private static final String MAP_DIRECTORY = "src/main/resources/maps/";

    @Getter
    private Map<String, GameMap> gameMaps = new HashMap<>();

    public void init() {
        loadAllMaps();
    }

    public void playerJoinServer(PlayerSessionData playerSessionData) {
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
        player.setServerEntityId(playerSessionData.getServerID());
        player.setMoveSpeed(NewPlayerConstants.DEFAULT_MOVE_SPEED);
        player.setClientHandler(playerSessionData.getClientHandler());

        playerSessionData.getClientHandler().setPlayer(player);

        new InitClientPacket(player, true, playerSessionData.getServerID(), location).sendPacket();
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
        Preconditions.checkArgument(!warp.getLocation().getMapName().equalsIgnoreCase(currentMapName),
                "The player is trying to switch to a game map they are already on. Map: " + warp.getLocation().getMapName());

        gameMaps.get(currentMapName).removePlayer(player);
        new PlayerSwitchMapPacket(player, warp).sendPacket();
        gameMaps.get(warp.getLocation().getMapName()).addPlayer(player, warp);
    }

    public void tick() {
        gameMaps.values().forEach(gameMap -> gameMap.tick());
    }

    public int getTotalPlayersOnline() {
        int onlinePlayers = 0;
        for (GameMap gameMap : gameMaps.values()) onlinePlayers = onlinePlayers + gameMap.getPlayerCount();
        return onlinePlayers;
    }

    private void loadAllMaps() {
        File[] files = new File(MAP_DIRECTORY).listFiles((d, name) -> name.endsWith(".tmx"));
        Preconditions.checkNotNull(files, "No game maps were loaded.");

        for (File file : files) {
            String mapName = file.getName().replace(".tmx", "");
            gameMaps.put(mapName, TmxFileParser.parseGameMap(MAP_DIRECTORY, mapName));
        }
        Log.println(getClass(), "Tmx Maps Loaded: " + files.length);
        fixWarpHeights();
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
        Preconditions.checkNotNull(gameMaps.get(mapName), "Tried to get the map " + mapName + ", but it doesn't exist or was not loaded.");
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
}
