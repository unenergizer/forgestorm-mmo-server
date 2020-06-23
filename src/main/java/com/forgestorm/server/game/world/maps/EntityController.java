package com.forgestorm.server.game.world.maps;

import com.forgestorm.server.game.world.entity.*;
import com.forgestorm.server.network.game.packet.out.AiEntityDataUpdatePacketOut;
import com.forgestorm.server.network.game.packet.out.EntityAttributesUpdatePacketOut;
import com.forgestorm.server.network.game.packet.out.EntityDespawnPacketOut;
import com.forgestorm.server.network.game.packet.out.EntitySpawnPacketOut;
import lombok.Getter;

import java.util.*;

import static com.forgestorm.server.util.Log.println;

@SuppressWarnings("SuspiciousMethodCalls")
public abstract class EntityController<T extends Entity> {

    private final GameMap gameMap;

    @Getter
    private QueuedIdGenerator queuedIdGenerator;

    @Getter
    protected final Map<Short, T> entityHashMap = new HashMap<>();
    final Queue<T> entitySpawnQueue = new LinkedList<>();
    final Queue<T> entityDespawnQueue = new LinkedList<>();

    EntityController(GameMap gameMap, short maximumSpawnsAllowed) {
        this.gameMap = gameMap;
        queuedIdGenerator = new QueuedIdGenerator(maximumSpawnsAllowed);
    }

    public void queueEntitySpawn(T entity) {
        if (!queuedIdGenerator.generateId(entity)) {
            println(getClass(), "Not enough space reserved to spawn entity: " + entity);
            return;
        }
        entitySpawnQueue.add(entity);
    }

    public void queueEntityDespawn(T entity) {
        queuedIdGenerator.deregisterId(entity);
        entityDespawnQueue.add(entity);
    }

    void entitySpawnRegistration(T entity) {
        entityHashMap.put(entity.getServerEntityId(), entity);
    }

    void entityDespawnRegistration(T entity) {
        entityHashMap.remove(entity.getServerEntityId());
        postEntityDespawn(entity);
    }

    protected abstract void postEntityDespawn(T entity);

    public abstract void tick();

    public boolean doesNotContainKey(Object key) {
        return !entityHashMap.containsKey(key);
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

                // We need to see if the entity to spawn is an ItemStackDrop
                if (entityToSpawn instanceof ItemStackDrop) {
                    ItemStackDrop itemStackDrop = (ItemStackDrop) entityToSpawn;

                    // If the item is available to everyone, send it to them.
                    if (itemStackDrop.isSpawnedForAll()) {
                        new EntitySpawnPacketOut(packetReceiver, entityToSpawn).sendPacket();
                    } else {
                        // Does this item have an owner? If so, only send them the item.
                        if (packetReceiver == itemStackDrop.getDropOwner()) {
                            new EntitySpawnPacketOut(packetReceiver, entityToSpawn).sendPacket();
                        }
                    }
                } else {
                    // Entity to spawn is not an ItemStackDrop
                    new EntitySpawnPacketOut(packetReceiver, entityToSpawn).sendPacket();
                }

                // Sending additional information for the AI entity.
                if (entityToSpawn instanceof AiEntity) {
                    if (((AiEntity) entityToSpawn).isBankKeeper()) {
                        // TODO: we could toggle bank access to certain bank tellers depending
                        // TODO: on if the player has the bank teller unlocked.
                        new AiEntityDataUpdatePacketOut(
                                packetReceiver,
                                (AiEntity) entityToSpawn,
                                AiEntityDataUpdatePacketOut.BANK_KEEPER_INDEX).sendPacket();
                    }
                }
            }

            // Send joined player to all online players
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

    public void removeEntity(Entity entity) {
        entityDespawn(entity);
        if (entity instanceof AiEntity) ((AiEntity) entity).clearCombatTargets();
        entityHashMap.remove(entity.getServerEntityId());
    }
}
