package com.valenguard.server.game.maps;

import com.valenguard.server.ValenguardMain;
import com.valenguard.server.game.GameManager;
import com.valenguard.server.game.NewPlayerConstants;
import com.valenguard.server.game.entity.*;
import com.valenguard.server.game.rpg.Attributes;
import com.valenguard.server.network.packet.out.*;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

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
    private final Queue<QueueData> playerJoinQueue = new ConcurrentLinkedQueue<>();
    private final Queue<Player> playerQuitQueue = new ConcurrentLinkedQueue<>();

    @Getter
    private final Map<Short, MovingEntity> mobList = new HashMap<>();
    private final Queue<MovingEntity> mobSpawnQueue = new LinkedList<>();
    private final Queue<MovingEntity> mobDespawnQueue = new LinkedList<>();


    @Getter
    private final Map<Short, StationaryEntity> stationaryEntitiesList = new HashMap<>();
    private final Queue<StationaryEntity> stationaryEntitiesSpawnQueue = new LinkedList<>();
    private final Queue<StationaryEntity> stationaryEntitiesDespawnQueue = new LinkedList<>();

    GameMap(String mapName, int mapWidth, int mapHeight, Tile[][] map) {
        this.mapName = mapName;
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
        this.map = map;
    }

    public void queueMobSpawn(MovingEntity movingEntity) {
        mobSpawnQueue.add(movingEntity);
    }

    private void queueMobDespawn(MovingEntity movingEntity) {
        mobDespawnQueue.add(movingEntity);
    }

    public void queueStationarySpawn(StationaryEntity stationaryEntity) {
        stationaryEntitiesSpawnQueue.add(stationaryEntity);
    }

    public void queueStationaryDespawn(StationaryEntity stationaryEntity) {
        stationaryEntitiesDespawnQueue.add(stationaryEntity);
    }

    public void tickStationaryEntities() {
        stationaryEntitiesSpawnQueue.forEach(stationaryEntity -> stationaryEntitiesList.put(stationaryEntity.getServerEntityId(), stationaryEntity));
        mobDespawnQueue.forEach(stationaryEntity -> stationaryEntitiesList.remove(stationaryEntity.getServerEntityId()));

        StationaryEntity stationaryEntity;
        while ((stationaryEntity = stationaryEntitiesDespawnQueue.poll()) != null) {
            postEntitySpawn(stationaryEntity);
        }

        while ((stationaryEntity = stationaryEntitiesSpawnQueue.poll()) != null) {
            postEntityDespawn(stationaryEntity);
        }
    }

    public void tickMOB() {
        mobSpawnQueue.forEach(this::mobSpawnRegistration);
        mobDespawnQueue.forEach(this::mobDespawnRegistration);

        MovingEntity mob;
        while ((mob = mobSpawnQueue.poll()) != null) {
            postEntitySpawn(mob);
        }

        while ((mob = mobDespawnQueue.poll()) != null) {
            postEntityDespawn(mob);
        }
    }

    private void mobSpawnRegistration(MovingEntity movingEntity) {
        mobList.put(movingEntity.getServerEntityId(), movingEntity);
    }

    private void mobDespawnRegistration(MovingEntity movingEntity) {
        mobList.remove(movingEntity.getServerEntityId());

        // Toggle respawns
        ValenguardMain.getInstance().getEntityRespawnTimer().addMob(movingEntity);
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

    private int timeTillNextHit = 0; // combat speed

    public void tickCombat() {
        timeTillNextHit++;
        if (timeTillNextHit >= 60) {
            timeTillNextHit = 0;

            // Now do combat
            for (MovingEntity movingEntity : mobList.values()) {
                if (movingEntity.getTargetEntity() == null) continue;

                MovingEntity targetEntity = movingEntity.getTargetEntity();
                // Check for distance
                if (movingEntity.getCurrentMapLocation().isWithinDistance(targetEntity.getFutureMapLocation(), 1)
                        || movingEntity.getCurrentMapLocation().isWithinDistance(targetEntity.getCurrentMapLocation(), 1)) {

                    Attributes movingEntityAttributes = movingEntity.getAttributes();
                    Attributes targetEntityAttributes = targetEntity.getAttributes();

                    movingEntity.setCurrentHealth(movingEntity.getCurrentHealth() - targetEntityAttributes.getDamage());
                    targetEntity.setCurrentHealth(targetEntity.getCurrentHealth() - movingEntityAttributes.getDamage());

                    sendCombatMessage(movingEntity, targetEntity);

                    if (movingEntity.getCurrentHealth() <= 0) {
                        finishCombat(targetEntity, movingEntity);
                    }

                    if (targetEntity.getCurrentHealth() <= 0) {
                        finishCombat(movingEntity, targetEntity);
                    }
                }
            }
        }
    }

    private void sendCombatMessage(MovingEntity attackerEntity, MovingEntity targetEntity) {
        Attributes attackerEntityAttributes = attackerEntity.getAttributes();
        Attributes targetEntityAttributes = targetEntity.getAttributes();

        if (attackerEntity instanceof Player) {
            new ChatMessagePacketOut((Player) attackerEntity, "Your HP: " + attackerEntity.getCurrentHealth() + " Damage Delt: " + targetEntityAttributes.getDamage()).sendPacket();
            new ChatMessagePacketOut((Player) attackerEntity, "Enemy HP: " + targetEntity.getCurrentHealth() + " Damage Delt: " + attackerEntityAttributes.getDamage()).sendPacket();
            new EntityDamagePacketOut((Player) attackerEntity, attackerEntity, attackerEntity.getCurrentHealth(), targetEntityAttributes.getDamage()).sendPacket();
            new EntityDamagePacketOut((Player) attackerEntity, targetEntity, targetEntity.getCurrentHealth(), attackerEntityAttributes.getDamage()).sendPacket();
        }
        if (targetEntity instanceof Player) {
            new ChatMessagePacketOut((Player) targetEntity, "Enemy HP: " + attackerEntity.getCurrentHealth() + " Damage Delt: " + targetEntityAttributes.getDamage()).sendPacket();
            new ChatMessagePacketOut((Player) targetEntity, "Your HP: " + targetEntity.getCurrentHealth() + " Damage Delt: " + attackerEntityAttributes.getDamage()).sendPacket();

            forAllPlayers(player -> {
                new EntityDamagePacketOut(player, attackerEntity, attackerEntity.getCurrentHealth(), targetEntityAttributes.getDamage()).sendPacket();
                new EntityDamagePacketOut(player, targetEntity, targetEntity.getCurrentHealth(), attackerEntityAttributes.getDamage()).sendPacket();
            });
        }
    }

    private void finishCombat(MovingEntity killerEntity, MovingEntity deadEntity) {

        // Remove the deadEntity from all entities target!
        releaseEntityTargets(deadEntity);
        deadEntity.setTargetEntity(null);

        if (killerEntity instanceof Player) {
            new ChatMessagePacketOut((Player) killerEntity, "You killed the enemy").sendPacket();
        }

        if (deadEntity instanceof Monster || deadEntity instanceof MOB) {

            queueMobDespawn(deadEntity); // A mob died, despawn them!

        } else if (deadEntity instanceof Player) {
            // Player Died. Lets respawn them!

            // TODO: Check to see if the player needs to change maps!

            Location teleportLocation = new Location(NewPlayerConstants.STARTING_MAP, NewPlayerConstants.RESPAWN_X_CORD, mapHeight - NewPlayerConstants.RESPAWN_Y_CORD);
            MoveDirection facingDirection = MoveDirection.SOUTH;

            deadEntity.setCurrentMapLocation(teleportLocation);
            deadEntity.setFutureMapLocation(teleportLocation);
            deadEntity.setFacingDirection(facingDirection);

            // Do a reheal
            Player deadPlayer = (Player) deadEntity;
            deadPlayer.setCurrentHealth(deadPlayer.getMaxHealth());

            // TODO: Get and update attributes...
//            for (EquipmentSlot equipmentSlot : deadPlayer.getPlayerEquipment().getEquipmentSlots()) {
//                if (equipmentSlot.getItemStack() != null) {
//                    resetHealth = resetHealth + equipmentSlot.getItemStack().getAttributes().getHealth();
//                }
//            }

            // TODO: Send other players info about the reheal (if they are still on the same map)

            // Send all players in map the teleport packet
            forAllPlayers(player -> new PlayerTeleportPacketOut(player, deadEntity, teleportLocation, facingDirection).sendPacket());

            forAllPlayers(player -> new EntityHealPacketOut(player, deadEntity, player.getMaxHealth()).sendPacket());

            new ChatMessagePacketOut((Player) deadEntity, "You died! Respawning you in graveyard!").sendPacket();
        }
    }

    private void releaseEntityTargets(MovingEntity targetToRemove) {
        for (MovingEntity movingEntity : mobList.values()) {
            if (movingEntity.getTargetEntity() != null
                    && movingEntity.getTargetEntity().equals(targetToRemove)) {
                movingEntity.setTargetEntity(null);
            }
        }
    }

    public void tickPlayerShuffle(long numberOfTicksPassed) {
        if ((numberOfTicksPassed % 40) == 0) {
            Collections.shuffle(playerList);
        }
    }

    public void sendPlayersPacket() {

        for (int quitsProcessed = 0; quitsProcessed <= GameManager.PLAYERS_TO_PROCESS; quitsProcessed++) {
            if (playerQuitQueue.isEmpty()) break;
            postEntityDespawn(playerQuitQueue.remove());
        }

        for (int joinsProcessed = 0; joinsProcessed <= GameManager.PLAYERS_TO_PROCESS; joinsProcessed++) {
            if (playerJoinQueue.isEmpty()) break;
            // Tell everyone already online about the player and the player about everyone online.
            Player playerWhoJoined = playerJoinQueue.remove().getPlayer();
            postEntitySpawn(playerWhoJoined);
            // Tell the player about all the mobs currently on the map.
            mobList.values().forEach(mob -> postEntitySpawn(playerWhoJoined, mob));
            stationaryEntitiesList.values().forEach(stationaryEntity -> postEntitySpawn(playerWhoJoined, stationaryEntity));
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
        releaseEntityTargets(player);
        playerList.remove(player);
        player.gameMapDeregister();
    }

    private void postEntitySpawn(Entity entityToSpawn) {
        for (Player packetReceiver : playerList) {

            // Send all online players, the entity that just spawned.
            if (!packetReceiver.equals(entityToSpawn)) {
                new EntitySpawnPacketOut(packetReceiver, entityToSpawn).sendPacket();
                // TODO: Send stats ????
//                new EntityAttributesUpdatePacketOut((Player) entityToSpawn, packetReceiver).sendPacket();
            }

            // Send joined player to all online players
            if (entityToSpawn.getEntityType() == EntityType.PLAYER) {
                new EntitySpawnPacketOut((Player) entityToSpawn, packetReceiver).sendPacket();
                new EntityAttributesUpdatePacketOut((Player) entityToSpawn, packetReceiver).sendPacket();
            }
        }
    }

    private void postEntitySpawn(Player receiver, Entity entityToSpawn) {
        new EntitySpawnPacketOut(receiver, entityToSpawn).sendPacket();
    }

    private void postEntityDespawn(Entity entityToDespawn) {
        for (Player packetReceiver : playerList) {
            if (packetReceiver == entityToDespawn) continue;
            new EntityDespawnPacketOut(packetReceiver, entityToDespawn).sendPacket();
        }
    }

    public int getPlayerCount() {
        return playerList.size();
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
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
        if (direction == MoveDirection.SOUTH) return new Location(mapName, 0, -1);
        if (direction == MoveDirection.NORTH) return new Location(mapName, 0, 1);
        if (direction == MoveDirection.WEST) return new Location(mapName, -1, 0);
        if (direction == MoveDirection.EAST) return new Location(mapName, 1, 0);
        if (direction == MoveDirection.NONE) return new Location(mapName, 0, 0);
        throw new RuntimeException("Tried to get a location, but direction could not be determined. MapName: " + mapName + ", MoveDirection: " + direction);
    }

    @Getter
    @AllArgsConstructor
    private class QueueData {
        private final Player player;
        private final Warp warp;
    }

    public void forAllPlayers(Consumer<Player> callback) {
        playerList.forEach(callback);
    }
}
