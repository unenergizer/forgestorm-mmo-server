package com.valenguard.server.game.entity;

import com.valenguard.server.game.GameConstants;
import com.valenguard.server.game.maps.Location;
import com.valenguard.server.game.maps.MoveDirection;
import com.valenguard.server.game.maps.Warp;
import com.valenguard.server.game.rpg.Attributes;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MovingEntity extends Entity {

    /**
     * The exact tile location of the entity on the tile grid.
     */
    private Location futureMapLocation;

    /**
     * The direction the entity is facing. Is not always the same direction
     * as they are moving because the move direction can be NONE.
     */
    private MoveDirection facingDirection;

    /**
     * The rate of speed the entity moves across tiles.
     * The smaller the number, the faster the entity moves.
     */
    private float moveSpeed;

    /**
     * Current X and Y of the packetReceiver (this is the interpolated values)
     */
    private float realX, realY;

    /**
     * Used by entity manager to measure the walk time between tiles/locations.
     */
    private float walkTime = 0f;

    /**
     * Entity attributes
     */
    private Attributes attributes = new Attributes();

    private int maxHealth;
    private int currentHealth;

    /**
     * This is the entity that we are interested in
     */
    private MovingEntity targetEntity;

    private Warp spawnWarp;

    private MoveDirection previousDirection = MoveDirection.NONE;

    public void gameMapRegister(Warp warp) {
        setCurrentMapLocation(new Location(warp.getLocation()));
        setFutureMapLocation(new Location(warp.getLocation()));
        setRealX(warp.getLocation().getX() * GameConstants.TILE_SIZE);
        setRealY(warp.getLocation().getY() * GameConstants.TILE_SIZE);
        walkTime = 0f;
        setFacingDirection(warp.getFacingDirection());
    }

    void gameMapDeregister() {
        setWalkTime(0f);
    }

    public boolean isEntityMoving() {
        return getCurrentMapLocation().getX() != getFutureMapLocation().getX() || getCurrentMapLocation().getY() != getFutureMapLocation().getY();
    }
}
