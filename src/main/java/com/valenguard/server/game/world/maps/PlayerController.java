package com.valenguard.server.game.world.maps;

import com.valenguard.server.game.GameConstants;
import com.valenguard.server.game.world.entity.Entity;
import com.valenguard.server.game.world.entity.EntityType;
import com.valenguard.server.game.world.entity.ItemStackDrop;
import com.valenguard.server.game.world.entity.Player;
import com.valenguard.server.network.game.packet.out.*;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

import static com.valenguard.server.util.Log.println;

public class PlayerController {

    private static final boolean PRINT_DEBUG = false;

    private final GameMap gameMap;

    @Getter
    private final List<Player> playerList = new ArrayList<>();
    private final Queue<QueueData> playerJoinQueue = new ConcurrentLinkedQueue<>();
    private final Queue<Player> playerQuitQueue = new ConcurrentLinkedQueue<>();

    @Getter
    private QueuedIdGenerator queuedIdGenerator;

    PlayerController(GameMap gameMap) {
        this.gameMap = gameMap;
        queuedIdGenerator = new QueuedIdGenerator((short) 1000);
    }

    public void tickPlayerQuit() {
        // Quitting the game map
        Iterator<Player> quitIterator = playerQuitQueue.iterator();
        int quitsProcessed = 0;
        while (quitIterator.hasNext() && quitsProcessed <= GameConstants.PLAYERS_TO_PROCESS) {
            Player player = quitIterator.next();
            println(getClass(), "<Player Quit> " + player, false, PRINT_DEBUG);
            player.setTargetEntity(null);
            gameMap.getAiEntityController().releaseEntityTargets(player);
            playerList.remove(player);
            player.gameMapDeregister();
            quitsProcessed++;
        }

        // Sending packets to everyone
        for (quitsProcessed = 0; quitsProcessed <= GameConstants.PLAYERS_TO_PROCESS; quitsProcessed++) {
            if (playerQuitQueue.isEmpty()) break;

            // Post player despawn
            Player entityToDespawn = playerQuitQueue.remove();
            for (Player packetReceiver : playerList) {
                if (packetReceiver == entityToDespawn) continue;
                new EntityDespawnPacketOut(packetReceiver, entityToDespawn).sendPacket();
            }
        }
    }

    public void tickPlayerJoin() {
        // Joining the game map
        Iterator<QueueData> joinIterator = playerJoinQueue.iterator();
        int joinsProcessed = 0;
        while (joinIterator.hasNext() && joinsProcessed <= GameConstants.PLAYERS_TO_PROCESS) {
            QueueData queueData = joinIterator.next();
            Player player = queueData.getPlayer();
            if (!queuedIdGenerator.generateId(player)) {
                println(getClass(), "Not enough space reserved to spawn player: " + player, true);
                return;
            }
            player.gameMapRegister(queueData.getWarp());
            playerList.add(player);
            new InitializeMapPacketOut(player, queueData.getWarp().getLocation().getMapName()).sendPacket();
            println(getClass(), "<Player Join> " + player, false, PRINT_DEBUG);
            joinsProcessed++;
        }

        // Sending packets to others
        for (joinsProcessed = 0; joinsProcessed <= GameConstants.PLAYERS_TO_PROCESS; joinsProcessed++) {
            if (playerJoinQueue.isEmpty()) break;

            // Tell everyone already online about the packetReceiver and the packetReceiver about everyone online.
            Player playerWhoJoined = playerJoinQueue.remove().getPlayer();

            // Post player spawn
            for (Player packetReceiver : playerList) {
                // Send all online players, the player that just spawned.
                if (!packetReceiver.equals(playerWhoJoined)) {
                    new EntitySpawnPacketOut(packetReceiver, playerWhoJoined).sendPacket();
                }

                // Send joined packetReceiver to all online players
                if (playerWhoJoined.getEntityType() == EntityType.PLAYER) {
                    new EntitySpawnPacketOut(playerWhoJoined, packetReceiver).sendPacket();
                    new EntityAttributesUpdatePacketOut(playerWhoJoined, packetReceiver).sendPacket();
                }
            }

            // Tell the packetReceiver about all the mobs currently on the map.
            gameMap.getAiEntityController().getEntities().forEach(aiEntity -> postPlayerSpawn(playerWhoJoined, aiEntity));
            gameMap.getAiEntityController().getEntities().forEach(aiEntity -> {
                // Sending additional information for the AI entity.
                if (aiEntity.isBankKeeper()) {
                    // TODO: we could toggle bank access to certain bank tellers depending
                    // TODO: on if the player has the bank teller unlocked.
                    new AiEntityDataUpdatePacketOut(
                            playerWhoJoined, aiEntity,
                            AiEntityDataUpdatePacketOut.BANK_KEEPER_INDEX).sendPacket();
                }
            });
            gameMap.getStationaryEntityController().getEntities().forEach(stationaryEntity -> postPlayerSpawn(playerWhoJoined, stationaryEntity));

            // Spawn itemStack drops!
            for (ItemStackDrop itemStackDrop : gameMap.getItemStackDropEntityController().getEntities()) {
                if (playerWhoJoined.getClientHandler().getAuthenticatedUser() == itemStackDrop.getDropOwner().getClientHandler().getAuthenticatedUser()) {
                    // ItemStackDrop Owner killed something, disconnected and reconnected. Send them the entity again.
                    postPlayerSpawn(playerWhoJoined, itemStackDrop);
                } else if (itemStackDrop.isSpawnedForAll()) {
                    // Spawn items for joined players, only if the item has been spawned for all players.
                    postPlayerSpawn(playerWhoJoined, itemStackDrop);
                }
            }
        }
    }

    public void tickPlayerShuffle(long numberOfTicksPassed) {
        if (numberOfTicksPassed % 40 == 0) {
            Collections.shuffle(playerList);
        }
    }

    public void addPlayer(Player player, Warp warp) {
        playerJoinQueue.add(new QueueData(player, warp));
    }

    public void removePlayer(Player player) {
        queuedIdGenerator.deregisterId(player);
        playerQuitQueue.add(player);
    }

    private void postPlayerSpawn(Player receiver, Entity entityToSpawn) {
        new EntitySpawnPacketOut(receiver, entityToSpawn).sendPacket();
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
