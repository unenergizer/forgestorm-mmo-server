package com.valenguard.server.game.maps;

import com.valenguard.server.game.GameManager;
import com.valenguard.server.game.entity.Entity;
import com.valenguard.server.game.entity.EntityType;
import com.valenguard.server.game.entity.ItemStackDrop;
import com.valenguard.server.game.entity.Player;
import com.valenguard.server.network.game.packet.out.EntityAttributesUpdatePacketOut;
import com.valenguard.server.network.game.packet.out.EntityDespawnPacketOut;
import com.valenguard.server.network.game.packet.out.EntitySpawnPacketOut;
import com.valenguard.server.network.game.packet.out.InitializeMapPacketOut;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

public class PlayerController {

    private final GameMap gameMap;

    @Getter
    private final List<Player> playerList = new ArrayList<>();
    private final Queue<QueueData> playerJoinQueue = new ConcurrentLinkedQueue<>();
    private final Queue<Player> playerQuitQueue = new ConcurrentLinkedQueue<>();

    PlayerController(GameMap gameMap) {
        this.gameMap = gameMap;
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

    public void tickPlayerShuffle(long numberOfTicksPassed) {
        if (numberOfTicksPassed % 40 == 0) {
            Collections.shuffle(playerList);
        }
    }

    public void sendPlayersPacket() {

        for (int quitsProcessed = 0; quitsProcessed <= GameManager.PLAYERS_TO_PROCESS; quitsProcessed++) {
            if (playerQuitQueue.isEmpty()) break;
            postPlayerDespawn(playerQuitQueue.remove());
        }

        for (int joinsProcessed = 0; joinsProcessed <= GameManager.PLAYERS_TO_PROCESS; joinsProcessed++) {
            if (playerJoinQueue.isEmpty()) break;
            // Tell everyone already online about the packetReceiver and the packetReceiver about everyone online.
            Player playerWhoJoined = playerJoinQueue.remove().getPlayer();
            postPlayerSpawn(playerWhoJoined);
            // Tell the packetReceiver about all the mobs currently on the map.
            gameMap.getAiEntityController().getEntities().forEach(mob -> postPlayerSpawn(playerWhoJoined, mob));
            gameMap.getStationaryEntityController().getEntities().forEach(stationaryEntity -> postPlayerSpawn(playerWhoJoined, stationaryEntity));

            // Spawn itemStack drops!
            for (ItemStackDrop itemStackDrop : gameMap.getItemStackDropEntityController().getEntities()) {
                if (playerWhoJoined == itemStackDrop.getDropOwner()) {
                    // Edge case where the packetReceiver killed something, disconnected and reconnected.
                    // TODO: When packetReceiver logs out, send them their dropped items...
                    // TODO: right now because real UUID's aren't set, this will never be true!
                    postPlayerSpawn(playerWhoJoined, itemStackDrop);
                } else if (itemStackDrop.isSpawnedForAll()) {
                    // Spawn items for joined players, only if the item has been spawned for all players.
                    postPlayerSpawn(playerWhoJoined, itemStackDrop);
                }
            }
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

        new InitializeMapPacketOut(player, queueData.getWarp().getLocation().getMapName()).sendPacket();
    }

    private void playerQuitGameMap(Player player) {
        player.setTargetEntity(null);
        gameMap.getAiEntityController().releaseEntityTargets(player);
        playerList.remove(player);
        player.gameMapDeregister();
    }

    private void postPlayerSpawn(Entity entityToSpawn) {
        for (Player packetReceiver : playerList) {

            // Send all online players, the entity that just spawned.
            if (!packetReceiver.equals(entityToSpawn)) {
                new EntitySpawnPacketOut(packetReceiver, entityToSpawn).sendPacket();
            }

            // Send joined packetReceiver to all online players
            if (entityToSpawn.getEntityType() == EntityType.PLAYER) {
                new EntitySpawnPacketOut((Player) entityToSpawn, packetReceiver).sendPacket();
                new EntityAttributesUpdatePacketOut((Player) entityToSpawn, packetReceiver).sendPacket();
            }
        }
    }

    private void postPlayerSpawn(Player receiver, Entity entityToSpawn) {
        new EntitySpawnPacketOut(receiver, entityToSpawn).sendPacket();
    }

    private void postPlayerDespawn(Entity entityToDespawn) {
        for (Player packetReceiver : playerList) {
            if (packetReceiver == entityToDespawn) continue;
            new EntityDespawnPacketOut(packetReceiver, entityToDespawn).sendPacket();
        }
    }

    public int getPlayerCount() {
        return playerList.size();
    }

    public Player findPlayer(short uuid) {
        for (Player player : playerList) {
            if (player.getServerEntityId() == uuid) return player;
        }
        return null;
    }

    public void forAllPlayers(Consumer<Player> callback) {
        playerList.forEach(callback);
    }

    @Getter
    @AllArgsConstructor
    private class QueueData {
        private final Player player;
        private final Warp warp;
    }
}
