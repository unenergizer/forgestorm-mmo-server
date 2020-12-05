package com.forgestorm.server.game.world.entity;

import com.forgestorm.server.game.world.maps.GameWorld;
import com.forgestorm.server.game.world.maps.Location;
import com.forgestorm.server.game.world.maps.WorldChunk;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Entity {

    private Integer databaseId;
    private Short serverEntityId;
    private EntityType entityType;
    private String name = "";
    private Location currentWorldLocation;
    private Appearance appearance;

    public String getWorldName() {
        return currentWorldLocation.getWorldName();
    }

    public GameWorld getGameWorld() {
        return currentWorldLocation.getGameWorld();
    }

    public WorldChunk getWorldChunk() {
        return getGameWorld().findChunk(currentWorldLocation.getX(), currentWorldLocation.getY());
    }

    @Override
    public String toString() {
        return "[" + name + "] -> { DatabaseID=" + databaseId + ", ServerEntityID=" + serverEntityId + ", EntityType=" + entityType.toString() + ", Location=" + currentWorldLocation.toString() + " }";
    }
}
