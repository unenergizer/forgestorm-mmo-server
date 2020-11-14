package com.forgestorm.server.game.world.entity;

import com.forgestorm.server.game.world.maps.GameWorld;
import com.forgestorm.server.game.world.maps.Location;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Entity {

    private Integer databaseId;
    private Short serverEntityId;
    private EntityType entityType;
    private String name = "";
    private Location currentMapLocation;
    private Appearance appearance;

    public String getMapName() {
        return currentMapLocation.getWorldName();
    }

    public GameWorld getGameMap() {
        return currentMapLocation.getGameMap();
    }

    @Override
    public String toString() {
        return "[" + name + "] -> { DatabaseID=" + databaseId + ", ServerEntityID=" + serverEntityId + ", EntityType=" + entityType.toString() + ", Location=" + currentMapLocation.toString() + " }";
    }
}
