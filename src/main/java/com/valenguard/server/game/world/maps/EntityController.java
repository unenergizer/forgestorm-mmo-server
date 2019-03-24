package com.valenguard.server.game.world.maps;

import com.valenguard.server.game.world.entity.Entity;
import com.valenguard.server.game.world.entity.EntityType;
import com.valenguard.server.game.world.entity.Player;
import com.valenguard.server.network.game.packet.out.EntityAttributesUpdatePacketOut;
import com.valenguard.server.network.game.packet.out.EntityDespawnPacketOut;
import com.valenguard.server.network.game.packet.out.EntitySpawnPacketOut;
import lombok.Getter;

import java.util.*;

public abstract class EntityController<T extends Entity> {

    private final GameMap gameMap;

    @Getter
    protected final Map<Short, T> entityHashMap = new HashMap<>();
    final Queue<T> entitySpawnQueue = new LinkedList<>();
    final Queue<T> entityDespawnQueue = new LinkedList<>();

    EntityController(GameMap gameMap) {
        this.gameMap = gameMap;
    }

    public void queueEntitySpawn(T entity) {
        entitySpawnQueue.add(entity);
    }

    public void queueEntityDespawn(T entity) {
        entityDespawnQueue.add(entity);
    }

    void entitySpawnRegistration(T entity) {
        entityHashMap.put(entity.getServerEntityId(), entity);
    }

    void entityDespawnRegistration(T entity) {
        entityHashMap.remove(entity.getServerEntityId());
        postEntityDespawn(entity);
    }

    public abstract void postEntityDespawn(T entity);

    public abstract void tick();

    public boolean containsKey(Object key) {
        return entityHashMap.containsKey(key);
    }

    public Collection<T> getEntities() {
        return entityHashMap.values();
    }

    public Entity getEntity(Object key) {
        return entityHashMap.get(key);
    }

    void entitySpawn(Entity entityToSpawn) {
        for (Player packetReceiver : gameMap.getPlayerController().getPlayerList()) {

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

    void entityDespawn(Entity entityToDespawn) {
        for (Player packetReceiver : gameMap.getPlayerController().getPlayerList()) {
            if (packetReceiver == entityToDespawn) continue;
            new EntityDespawnPacketOut(packetReceiver, entityToDespawn).sendPacket();
        }
    }
}
