package com.valenguard.server.game.maps;

import com.valenguard.server.game.GameManager;
import com.valenguard.server.game.entity.Entity;
import com.valenguard.server.game.entity.Player;
import com.valenguard.server.network.packet.out.EntityDespawnPacket;
import com.valenguard.server.network.packet.out.EntitySpawnPacket;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class GameMap {

    @Getter
    private final String mapName;
    @Getter
    private final int mapWidth, mapHeight;
    @Getter
    private final Tile map[][];

    @Getter
    private final List<Player> playerList = new ArrayList<>();
    @Getter
    private final List<Entity> mobList = new ArrayList<>();
    private final Queue<QueueData> playerJoinQueue = new ConcurrentLinkedQueue<>();
    private final Queue<Player> playerQuitQueue = new ConcurrentLinkedQueue<>();

    public GameMap(String mapName, int mapWidth, int mapHeight, Tile[][] map) {
        this.mapName = mapName;
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
        this.map = map;
    }

    public void tick() {
        // Add players
        for (int joinsProcessed = 0; joinsProcessed <= GameManager.PLAYERS_TO_PROCESS; joinsProcessed++) {
            if (playerJoinQueue.isEmpty()) break;
            playerJoinGameMap(playerJoinQueue.remove());
        }

        // Remove players
        for (int quitsProcessed = 0; quitsProcessed <= GameManager.PLAYERS_TO_PROCESS; quitsProcessed++) {
            if (playerQuitQueue.isEmpty()) break;
            playerQuitGameMap(playerQuitQueue.remove());
        }

        // TODO: mob/entity/item tick
//        if (playerList.isEmpty() && mobList.isEmpty()) return;
    }

    public void addPlayer(Player player, Warp warp) {
        playerJoinQueue.add(new QueueData(player, warp));
    }

    public void removePlayer(Player player) {
        playerQuitQueue.add(player);
    }

    private void playerJoinGameMap(QueueData queueData) {
        Player player = queueData.getPlayer();
        player.gameMapRegister(queueData.getWarp());
        playerList.add(player);

        new EntitySpawnPacket(player, player).sendPacket();

        for (Player otherPlayer : playerList) {
            if (otherPlayer == player) continue;
            new EntitySpawnPacket(otherPlayer, player).sendPacket();
            new EntitySpawnPacket(player, otherPlayer).sendPacket();
        }
    }

    private void playerQuitGameMap(Player player) {
        playerList.remove(player);
        player.gameMapDeregister();

        for (Player otherPlayer : playerList) {
            if (otherPlayer == player) continue;
            new EntityDespawnPacket(otherPlayer, player).sendPacket();
        }
    }

    public int getPlayerCount() {
        return playerList.size();
    }

    public boolean isMovable(Location location) {
        return !isOutOfBounds(location) && isTraversable(location);
    }

    private boolean isTraversable(Location location) {
        if (isOutOfBounds(location)) return false;
        return location.getGameMap().getMap()[location.getX()][location.getY()].isTraversable();
    }

    private boolean isOutOfBounds(Location location) {
        int x = location.getX();
        int y = location.getY();
        return x < 0 || x >= location.getGameMap().getMapWidth() || y < 0 || y >= location.getGameMap().getMapHeight();
    }

    public boolean isOutOfBounds(int x, int y) {
        return x < 0 || x >= mapWidth || y < 0 || y >= mapHeight;
    }

    public Warp getWarpFromLocation(Location location) {
        return location.getGameMap().getMap()[location.getX()][location.getY()].getWarp();
    }

    public boolean locationHasWarp(Location location) {
        return getTileByLocation(location).getWarp() != null;
    }

    private Tile getTileByLocation(Location location) {
        if (isOutOfBounds(location)) return null;
        return location.getGameMap().getMap()[location.getX()][location.getY()];
    }

    @Getter
    @AllArgsConstructor
    private class QueueData {
        private final Player player;
        private final Warp warp;
    }
}
