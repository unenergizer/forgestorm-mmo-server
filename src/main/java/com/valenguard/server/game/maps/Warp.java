package com.valenguard.server.game.maps;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Warp {
    private final Location location;
    private final MoveDirection facingDirection;
}
