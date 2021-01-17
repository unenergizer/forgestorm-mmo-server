package com.forgestorm.server.game.world.maps;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Warp {
    private final Location warpDestination;
    private final MoveDirection directionToFace;
}
