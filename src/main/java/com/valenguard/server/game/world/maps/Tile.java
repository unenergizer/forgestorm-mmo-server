package com.valenguard.server.game.world.maps;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Tile {
    private boolean isTraversable;
    private Warp warp;
}
