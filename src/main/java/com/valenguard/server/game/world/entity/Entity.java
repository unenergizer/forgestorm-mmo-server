package com.valenguard.server.game.world.entity;

import com.valenguard.server.game.world.maps.GameMap;
import com.valenguard.server.game.world.maps.Location;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Entity {

    private Integer databaseId;
    private short serverEntityId;
    private EntityType entityType;
    private String name = "";
    private Location currentMapLocation;
    private Appearance appearance;

    public String getMapName() {
        return currentMapLocation.getMapName();
    }

    public GameMap getGameMap() {
        return currentMapLocation.getGameMap();
    }

    @Override
    public String toString() {
        return "[" + name + "] -> { DatabaseID=" + databaseId + ", ServerEntityID=" + serverEntityId + ", EntityType=" + entityType.toString() + ", Location=" + currentMapLocation.toString() + " }";
    }
}
