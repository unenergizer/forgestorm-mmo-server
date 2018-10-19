package com.valenguard.server.entity;

import lombok.Getter;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class EntityManager {

    private EntityManager() {}

    @Getter
    private final static EntityManager instance = new EntityManager();

    // EntityType -> EntityId -> Entity
    private Map<Class<? extends Entity>, Map<Short, Entity>> entities = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public <T extends Entity> Map<Short, T> getEntitiesMap(Class<? extends Entity> entityType) {
        Map<Short, T> entityOfType = (Map<Short, T>) entities.get(entityType);
        if (entityOfType == null) entities.put(entityType, new ConcurrentHashMap<>());
        return (Map<Short, T>) entities.get(entityType);
    }

    @SuppressWarnings("unchecked")
    public <T extends Entity> List<T> getEntities(Class<? extends Entity> entityType) {
        return new ArrayList(getEntitiesMap(entityType).values());
    }

    public <T extends Entity> void addEntity(Class<? extends Entity> entityType, short entityId, T entity) {
        getEntitiesMap(entityType).put(entityId, entity);
    }

    public void removeEntity(Class<? extends Entity> entityType, Integer entityId) {
        getEntitiesMap(entityType).remove(entityId);
    }

    @SuppressWarnings("unchecked")
    public <T extends Entity> T getEntity(Class<? extends Entity> entityType, Integer entityId) {
        return (T) getEntitiesMap(entityType).get(entityId);
    }
}
