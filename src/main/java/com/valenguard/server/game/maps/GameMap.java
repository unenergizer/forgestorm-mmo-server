package com.valenguard.server.game.maps;

import com.valenguard.server.game.GameManager;
import com.valenguard.server.game.entity.Entity;
import com.valenguard.server.game.entity.EntityType;
import com.valenguard.server.game.entity.Npc;
import com.valenguard.server.game.entity.Player;
import com.valenguard.server.network.packet.out.EntityDespawnPacket;
import com.valenguard.server.network.packet.out.EntitySpawnPacket;
import com.valenguard.server.network.packet.out.InitializeMapPacket;
import com.valenguard.server.util.Log;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

import static com.google.common.base.Preconditions.checkArgument;

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

    GameMap(String mapName, int mapWidth, int mapHeight, Tile[][] map) {
        this.mapName = mapName;
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
        this.map = map;
    }

    boolean allSpawned = false;

    public void addNpc(Npc npc) {
        mobList.add(npc);
    }

    public void tickNPC() {
        // TODO: spawns, respawns, despawns, etc etc etc...
        // TODO REMOVE THIS GOD DAMN SHIT
        if (!allSpawned) {
            if (!mapName.equals("maintown")) return;

            int maxCrashTest = 0;

            for (int i = 0; i < maxCrashTest; i++) {
                mobList.add(thisIsBadGen(i,"ID: " + i));
            }

            Log.println(getClass(), "Total NPCs: " + mobList.size(), true, true);

            allSpawned = true;
        }
    }

    private short thisIsBadGenId = 100;

    private Npc thisIsBadGen(int i, String name) {
        Npc npc = new Npc();

        float speed = new Random().nextFloat();
        if (speed <= .2f) speed = .2f;

        npc.setServerEntityId(thisIsBadGenId++);
        npc.setMoveSpeed(speed);
        npc.setName(name);
        npc.setEntityType(EntityType.NPC);
        npc.gameMapRegister(new Warp(new Location("maintown",
                17,
                50 - 28 - 1), MoveDirection.DOWN));

        Log.println(getClass(), "Adding npc to be spawned. ID: " + i, false);
        return npc;
    }

    public void tickGroundItems(GameMap gameMap) {
        // TODO: tick ground items, despan, spawn, allow pickup etc...
    }

    public void tickPlayer() {
        // Remove players
        Iterator<Player> quitIterator = playerQuitQueue.iterator();
        int quitsProcessed = 0;
        while (quitIterator.hasNext() && quitsProcessed <= GameManager.PLAYERS_TO_PROCESS) {
            playerQuitGameMap(quitIterator.next());
            quitsProcessed++;
        }

        // Add players
        Iterator<QueueData> joinIterator = playerJoinQueue.iterator();
        int joinsProcessed = 0;
        while (joinIterator.hasNext() && joinsProcessed <= GameManager.PLAYERS_TO_PROCESS) {
            playerJoinGameMap(joinIterator.next());
            joinsProcessed++;
        }
    }

    public void sendPlayersPacket() {

        // TODO check to see if the entity is registered for a respawn
        if (playerJoinQueue.size() >= 1) {
            for (Entity entity : mobList) {
                // postEntityDespawn(entity);
            }

            for (Entity entity : mobList) {
                Log.println(getClass(), "Sending npc to be spawned");
                postEntitySpawn(entity);
            }
        }

        for (int quitsProcessed = 0; quitsProcessed <= GameManager.PLAYERS_TO_PROCESS; quitsProcessed++) {
            if (playerQuitQueue.isEmpty()) break;
            postEntityDespawn(playerQuitQueue.remove());
        }

        for (int joinsProcessed = 0; joinsProcessed <= GameManager.PLAYERS_TO_PROCESS; joinsProcessed++) {
            if (playerJoinQueue.isEmpty()) break;
            postEntitySpawn(playerJoinQueue.remove().getPlayer());
        }
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

        new InitializeMapPacket(player, queueData.getWarp().getLocation().getMapName()).sendPacket();
    }

    private void playerQuitGameMap(Player player) {
        playerList.remove(player);
        player.gameMapDeregister();
    }

    private void postEntitySpawn(Entity entityToSpawn) {
        for (Player packetReceiver : playerList) {
            if (!packetReceiver.equals(entityToSpawn)) {
                new EntitySpawnPacket(packetReceiver, entityToSpawn).sendPacket();
            }
            if (entityToSpawn.getEntityType() == EntityType.PLAYER) {
                new EntitySpawnPacket((Player) entityToSpawn, packetReceiver).sendPacket();
            }
        }
    }

    private void postEntityDespawn(Entity entityToDespawn) {
        for (Player packetReceiver : playerList) {
            if (packetReceiver == entityToDespawn) continue;
            new EntityDespawnPacket(packetReceiver, entityToDespawn).sendPacket();
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
        checkArgument(!isOutOfBounds(location));
        return location.getGameMap().getMap()[location.getX()][location.getY()];
    }

    public Location getLocation(MoveDirection direction) {
        if (direction == MoveDirection.DOWN) return new Location(mapName, 0, -1);
        if (direction == MoveDirection.UP) return new Location(mapName, 0, 1);
        if (direction == MoveDirection.LEFT) return new Location(mapName, -1, 0);
        if (direction == MoveDirection.RIGHT) return new Location(mapName, 1, 0);
        if (direction == MoveDirection.NONE) return new Location(mapName, 0, 0);
        throw new RuntimeException("Tried to get a location, but direction could not be determined. MapName: " + mapName + ", MoveDirection: " + direction);
    }

    @Getter
    @AllArgsConstructor
    private class QueueData {
        private final Player player;
        private final Warp warp;
    }
}
