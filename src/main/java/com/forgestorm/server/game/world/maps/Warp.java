package com.forgestorm.server.game.world.maps;

import lombok.Getter;

@Getter
public class Warp {
    private final Location warpDestination;
    private final MoveDirection directionToFace;

    private short fromX, fromY;

    public Warp(Location warpDestination, MoveDirection directionToFace) {
        this.warpDestination = warpDestination;
        this.directionToFace = directionToFace;
    }

    public Warp(Location warpDestination, MoveDirection directionToFace, short fromX, short fromY) {
        this.warpDestination = warpDestination;
        this.directionToFace = directionToFace;
        this.fromX = fromX;
        this.fromY = fromY;
    }
}
