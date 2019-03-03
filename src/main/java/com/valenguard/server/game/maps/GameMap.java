package com.valenguard.server.game.maps;

import com.valenguard.server.ValenguardMain;
import com.valenguard.server.game.GameManager;
import com.valenguard.server.game.PlayerConstants;
import com.valenguard.server.game.entity.*;
import com.valenguard.server.game.inventory.ItemStack;
import com.valenguard.server.game.rpg.Attributes;
import com.valenguard.server.game.rpg.ExperiencePacketInfo;
import com.valenguard.server.game.rpg.skills.SkillOpcodes;
import com.valenguard.server.network.packet.out.*;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

import static com.google.common.base.Preconditions.checkArgument;
import static com.valenguard.server.util.Log.println;

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
    private final Map<Short, MovingEntity> aiEntityMap = new HashMap<>();
    private final Queue<MovingEntity> aiEntitySpawnQueue = new LinkedList<>();
    private final Queue<MovingEntity> aiEntityDespawnQueue = new LinkedList<>();

    @Getter
    private final Map<Short, StationaryEntity> stationaryEntityMap = new HashMap<>();
    private final Queue<StationaryEntity> stationaryEntitiesSpawnQueue = new LinkedList<>();
    private final Queue<StationaryEntity> stationaryEntitiesDespawnQueue = new LinkedList<>();

    @Getter
    private final Map<Short, ItemStackDrop> itemStackDropMap = new HashMap<>();
    private final Queue<ItemStackDrop> itemStackDropSpawnQueue = new LinkedList<>();
    private final Queue<ItemStackDrop> itemStackDropDespawnQueue = new LinkedList<>();

    public GameMap(String mapName, int mapWidth, int mapHeight, Tile[][] map) {
        this.mapName = mapName;
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
        this.map = map;
    }

    /*
     * STATIONARY ENTITY ////////////////////////////////////////////////////////////////////////
     */

    public void queueStationarySpawn(StationaryEntity stationaryEntity) {
        stationaryEntitiesSpawnQueue.add(stationaryEntity);
    }

    public void tickStationaryEntities() {
        stationaryEntitiesSpawnQueue.forEach(stationaryEntity -> stationaryEntityMap.put(stationaryEntity.getServerEntityId(), stationaryEntity));
        stationaryEntitiesDespawnQueue.forEach(stationaryEntity -> stationaryEntityMap.remove(stationaryEntity.getServerEntityId()));

        StationaryEntity stationaryEntity;
        while ((stationaryEntity = stationaryEntitiesSpawnQueue.poll()) != null) {
            postEntitySpawn(stationaryEntity);
        }

        while ((stationaryEntity = stationaryEntitiesDespawnQueue.poll()) != null) {
            postEntityDespawn(stationaryEntity);
        }
    }

    /*
     * NPC ////////////////////////////////////////////////////////////////////////
     */

    public void queueAiEntitySpawn(MovingEntity movingEntity) {
        aiEntitySpawnQueue.add(movingEntity);
    }

    private void queueAiEntityDespawn(MovingEntity movingEntity) {
        aiEntityDespawnQueue.add(movingEntity);
    }

    private void aiEntitySpawnRegistration(MovingEntity movingEntity) {
        aiEntityMap.put(movingEntity.getServerEntityId(), movingEntity);
    }

    private void aiEntityDespawnRegistration(MovingEntity movingEntity) {
        aiEntityMap.remove(movingEntity.getServerEntityId());

        // Toggle respawns
        ValenguardMain.getInstance().getEntityRespawnTimer().addMob(movingEntity);
    }

    public void tickMOB() {
        aiEntitySpawnQueue.forEach(this::aiEntitySpawnRegistration);
        aiEntityDespawnQueue.forEach(this::aiEntityDespawnRegistration);

        MovingEntity mob;
        while ((mob = aiEntitySpawnQueue.poll()) != null) {
            postEntitySpawn(mob);
        }

        while ((mob = aiEntityDespawnQueue.poll()) != null) {
            postEntityDespawn(mob);
        }
    }

    /*
     * ItemStackDrop //////////////////////////////////////////////////////////////////////////
     */

    private void queueItemStackDropSpawn(ItemStackDrop itemStackDrop) {
        itemStackDropSpawnQueue.add(itemStackDrop);
    }

    public void queueItemStackDropDespawn(ItemStackDrop itemStackDrop) {
        itemStackDropDespawnQueue.add(itemStackDrop);
    }

    public void tickItemStackDrop() {
        itemStackDropSpawnQueue.forEach(itemStackDrop -> itemStackDropMap.put(itemStackDrop.getServerEntityId(), itemStackDrop));
        itemStackDropDespawnQueue.forEach(itemStackDrop -> itemStackDropMap.remove(itemStackDrop.getServerEntityId()));

        ItemStackDrop itemStackDrop;
        while ((itemStackDrop = itemStackDropSpawnQueue.poll()) != null) {
            ValenguardMain.getInstance().getGameLoop().getItemTickUpdates().addItemToGround(itemStackDrop);
            new EntitySpawnPacketOut(itemStackDrop.getKiller(), itemStackDrop).sendPacket();
        }

        while ((itemStackDrop = itemStackDropDespawnQueue.poll()) != null) {
            postEntityDespawn(itemStackDrop);
        }
    }

    /*
     * Player ////////////////////////////////////////////////////////////////////////////////
     */

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
            for (MovingEntity movingEntity : aiEntityMap.values()) {
                if (movingEntity.getTargetEntity() == null) continue;

                MovingEntity targetEntity = movingEntity.getTargetEntity();
                // Check for distance
                if (movingEntity.getCurrentMapLocation().isWithinDistance(targetEntity.getFutureMapLocation(), (short) 1)
                        || movingEntity.getCurrentMapLocation().isWithinDistance(targetEntity.getCurrentMapLocation(), (short) 1)) {

                    // Entity vs Target
                    Attributes movingEntityAttributes = movingEntity.getAttributes();
                    Attributes targetEntityAttributes = targetEntity.getAttributes();
                    targetEntity.setCurrentHealth(targetEntity.getCurrentHealth() - movingEntityAttributes.getDamage());

                    // Target vs Entity
                    if (targetEntity.getTargetEntity() == movingEntity && targetEntity instanceof Player) {
                        movingEntity.setCurrentHealth(movingEntity.getCurrentHealth() - targetEntityAttributes.getDamage());
                    } else {
                        movingEntity.setCurrentHealth(movingEntity.getCurrentHealth() - targetEntityAttributes.getDamage());
                    }

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
        }
        if (targetEntity instanceof Player) {
            new ChatMessagePacketOut((Player) targetEntity, "Enemy HP: " + attackerEntity.getCurrentHealth() + " Damage Delt: " + targetEntityAttributes.getDamage()).sendPacket();
            new ChatMessagePacketOut((Player) targetEntity, "Your HP: " + targetEntity.getCurrentHealth() + " Damage Delt: " + attackerEntityAttributes.getDamage()).sendPacket();
        }
        forAllPlayers(player -> {
            new EntityDamagePacketOut(player, attackerEntity, attackerEntity.getCurrentHealth(), targetEntityAttributes.getDamage()).sendPacket();
            new EntityDamagePacketOut(player, targetEntity, targetEntity.getCurrentHealth(), attackerEntityAttributes.getDamage()).sendPacket();
        });
    }

    private short lastItemStackDrop = 2000;

    private void finishCombat(MovingEntity killerEntity, MovingEntity deadEntity) {

        // Remove the deadEntity from all entities target!
        releaseEntityTargets(deadEntity);
        deadEntity.setTargetEntity(null);

        if (killerEntity instanceof Player) {
            new ChatMessagePacketOut((Player) killerEntity, "You killed the enemy").sendPacket();
            new SkillExperiencePacketOut((Player) killerEntity, new ExperiencePacketInfo(SkillOpcodes.MELEE, 10)).sendPacket();
        }

        if (deadEntity instanceof Player) {
            // Player Died. Lets respawn them!

            Location teleportLocation = new Location(PlayerConstants.STARTING_MAP, PlayerConstants.RESPAWN_X_CORD, (short) (mapHeight - PlayerConstants.RESPAWN_Y_CORD));
            MoveDirection facingDirection = MoveDirection.SOUTH;

            // Do a reheal
            Player deadPlayer = (Player) deadEntity;
            deadPlayer.setCurrentHealth(deadPlayer.getMaxHealth());

            // Check to see if the player needs to change maps!
            if (!deadPlayer.getMapName().equals(PlayerConstants.STARTING_MAP)) {
                println(getClass(), "Warping player to graveyard map!");

                // Warp player to graveyard
                deadPlayer.setWarp(new Warp(teleportLocation, facingDirection));
            } else {
                println(getClass(), "Teleporting player to graveyard!");

                // Teleport player
                deadEntity.setCurrentMapLocation(teleportLocation);
                deadEntity.setFutureMapLocation(teleportLocation);
                deadEntity.setFacingDirection(facingDirection);

                // Send all players in map the teleport packet
                forAllPlayers(player -> new PlayerTeleportPacketOut(player, deadEntity, teleportLocation, facingDirection).sendPacket());

                // Send other players info about the reheal (if they are still on the same map)
                forAllPlayers(player -> new EntityHealPacketOut(player, deadEntity, player.getMaxHealth()).sendPacket());
            }

            new ChatMessagePacketOut((Player) deadEntity, "You died! Respawning you in graveyard!").sendPacket();

        } else {
            queueAiEntityDespawn(deadEntity); // A mob died, despawn them!

            // If a AI entity kills and AI entity, do not drop ItemStack
            if (!(killerEntity instanceof Player)) return;

            // Give player drop table item
            if (((AIEntity) deadEntity).getDropTableID() != null) {
                ItemStack itemStack = ValenguardMain.getInstance().getDropTableManager().dropItemOnMap(((AIEntity) deadEntity).getDropTableID(), 1);

                ItemStackDrop itemStackDrop = new ItemStackDrop();
                itemStackDrop.setServerEntityId(lastItemStackDrop++);
                itemStackDrop.setEntityType(EntityType.ITEM_STACK);
                itemStackDrop.setName(itemStack.getName());
                itemStackDrop.setCurrentMapLocation(new Location(deadEntity.getCurrentMapLocation()));
                itemStackDrop.setAppearance(new Appearance((byte) 0, new short[]{(short) itemStack.getItemId()}));
                itemStackDrop.setItemStack(itemStack);
                itemStackDrop.setKiller((Player) killerEntity);

                queueItemStackDropSpawn(itemStackDrop);
            }
        }
    }

    private void releaseEntityTargets(MovingEntity targetToRemove) {
        for (MovingEntity movingEntity : aiEntityMap.values()) {
            if (movingEntity.getTargetEntity() != null
                    && movingEntity.getTargetEntity().equals(targetToRemove)) {
                movingEntity.setTargetEntity(null);
            }
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
            postEntityDespawn(playerQuitQueue.remove());
        }

        for (int joinsProcessed = 0; joinsProcessed <= GameManager.PLAYERS_TO_PROCESS; joinsProcessed++) {
            if (playerJoinQueue.isEmpty()) break;
            // Tell everyone already online about the player and the player about everyone online.
            Player playerWhoJoined = playerJoinQueue.remove().getPlayer();
            postEntitySpawn(playerWhoJoined);
            // Tell the player about all the mobs currently on the map.
            aiEntityMap.values().forEach(mob -> postEntitySpawn(playerWhoJoined, mob));
            stationaryEntityMap.values().forEach(stationaryEntity -> postEntitySpawn(playerWhoJoined, stationaryEntity));

            // Spawn itemStack drops!
            for (ItemStackDrop itemStackDrop : itemStackDropMap.values()) {
                if (playerWhoJoined == itemStackDrop.getKiller()) {
                    // Edge case where the player killed something, disconnected and reconnected.
                    // TODO: When player logs out, send them their dropped items...
                    // TODO: right now because real UUID's aren't set, this will never be true!
                    postEntitySpawn(playerWhoJoined, itemStackDrop);
                } else if (itemStackDrop.isSpawnedForAll()) {
                    // Spawn items for joined players, only if the item has been spawned for all players.
                    postEntitySpawn(playerWhoJoined, itemStackDrop);
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
        releaseEntityTargets(player);
        playerList.remove(player);
        player.gameMapDeregister();
    }

    private void postEntitySpawn(Entity entityToSpawn) {
        for (Player packetReceiver : playerList) {

            // Send all online players, the entity that just spawned.
            if (!packetReceiver.equals(entityToSpawn)) {
                new EntitySpawnPacketOut(packetReceiver, entityToSpawn).sendPacket();
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
        short x = location.getX();
        short y = location.getY();
        return x < 0 || x >= location.getGameMap().getMapWidth() || y < 0 || y >= location.getGameMap().getMapHeight();
    }

    public boolean isOutOfBounds(short x, short y) {
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
        if (direction == MoveDirection.SOUTH) return new Location(mapName, (short) 0, (short) -1);
        if (direction == MoveDirection.NORTH) return new Location(mapName, (short) 0, (short) 1);
        if (direction == MoveDirection.WEST) return new Location(mapName, (short) -1, (short) 0);
        if (direction == MoveDirection.EAST) return new Location(mapName, (short) 1, (short) 0);
        if (direction == MoveDirection.NONE) return new Location(mapName, (short) 0, (short) 0);
        throw new RuntimeException("Tried to get a location, but direction could not be determined. MapName: " + mapName + ", MoveDirection: " + direction);
    }

    public Player findPlayer(short uuid) {
        for (Player player : playerList) {
            if (player.getServerEntityId() == uuid) return player;
        }
        return null;
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