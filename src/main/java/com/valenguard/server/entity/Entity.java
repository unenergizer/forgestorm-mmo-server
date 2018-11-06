package com.valenguard.server.entity;

import com.valenguard.server.maps.data.Location;
import com.valenguard.server.maps.data.TmxMap;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Entity {

    /**
     * Represents a unique server ID for a given
     * entity.
     */
    private short serverEntityId;

    private short entityType;

    private int health;
    private String name = "";
    private int level;

    private Location currentMapLocation;
    private Location futureMapLocation;
    private float moveSpeed;

    private float realX, realY;

    private float walkTime = 0f;

    /**
     * The direction the entity is facing. Is not always the same direction
     * as they are moving because the move direction can be STOP.
     */
    private MoveDirection facingDirection;

    public TmxMap getTmxMap() {
        return currentMapLocation.getTmxMap();
    }
}
