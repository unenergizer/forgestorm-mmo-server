package com.valenguard.server.game.entity;

import com.valenguard.server.game.GameConstants;
import com.valenguard.server.game.maps.Location;
import com.valenguard.server.game.maps.MoveDirection;
import com.valenguard.server.game.maps.Warp;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MovingEntity extends Entity {
    private Location futureMapLocation;
    private float moveSpeed;
    private float realX, realY;
    private float walkTime = 0f;
    private MoveDirection facingDirection;

    public void gameMapRegister(Warp warp) {
        setCurrentMapLocation(new Location(warp.getLocation()));
        setFutureMapLocation(new Location(warp.getLocation()));
        setRealX(warp.getLocation().getX() * GameConstants.TILE_SIZE);
        setRealY(warp.getLocation().getY() * GameConstants.TILE_SIZE);
        setFacingDirection(warp.getFacingDirection());
    }

    void gameMapDeregister() {
        setWalkTime(0f);
    }

    public boolean isEntityMoving() {
        return getCurrentMapLocation().getX() != getFutureMapLocation().getX() || getCurrentMapLocation().getY() != getFutureMapLocation().getY();
    }
}
