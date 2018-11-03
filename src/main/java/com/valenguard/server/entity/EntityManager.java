package com.valenguard.server.entity;

import lombok.Getter;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class EntityManager {

    private EntityManager() {}

    @Getter
    private final static EntityManager instance = new EntityManager();

    //  EntityId -> Entity
    private final Map<Short, Entity> entities = new ConcurrentHashMap<>();

    public void addEntity(short entityId, Entity entity) {
        entities.put(entityId, entity);
    }

    public void removeEntity(Short entityId) {
        entities.remove(entityId);
    }

    public Entity getEntity(short entity) {
        return entities.get(entity);
    }

    public int entitiesLoaded() {
        return entities.size();
    }

    public Collection<Entity> getEntities() {
        return entities.values();
    }
}
