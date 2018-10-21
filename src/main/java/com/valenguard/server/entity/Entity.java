package com.valenguard.server.entity;

import com.valenguard.server.maps.data.Location;
import com.valenguard.server.maps.data.TmxMap;
import com.valenguard.server.network.shared.ServerConstants;
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

    public boolean isMoving() {
        return moveDirection != Direction.STOP;
    }

    private float realX;
    private float realY;

    /**
     * The current direction the entity is moving in.
     */
    private Direction moveDirection = Direction.STOP;

    /**
     * The direction the entity intends to move in the future.
     */
    private Direction predictedDirection = Direction.STOP;

    /**
     * The direction the entity is facing. Is not always the same direction
     * as they are moving because the move direction can be STOP.
     */
    private Direction facingDirection;

    public TmxMap getMapData() {
        return currentMapLocation.getMapData();
    }
}
