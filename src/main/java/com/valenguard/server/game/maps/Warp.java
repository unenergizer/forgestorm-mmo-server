package com.valenguard.server.game.maps;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Warp {
    private Location location;
    private MoveDirection facingDirection;
}
