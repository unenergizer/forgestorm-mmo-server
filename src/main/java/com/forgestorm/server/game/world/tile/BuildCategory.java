package com.forgestorm.server.game.world.tile;

public enum BuildCategory {
    DECORATION,
    WALKABLE,
    WALL,
    ROOF,
    WANG,
    UNDEFINED;

    @Override
    public String toString() {
        String name = name().toLowerCase();
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }
}
