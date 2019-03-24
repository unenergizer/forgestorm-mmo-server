package com.valenguard.server.game.world.maps;

public enum MoveDirection {
    NORTH,
    SOUTH,
    WEST,
    EAST,
    NONE;

    public static MoveDirection getDirection(byte directionByte) {
        for (MoveDirection direction : MoveDirection.values()) {
            if ((byte) direction.ordinal() == directionByte) {
                return direction;
            }
        }
        return null;
    }

    public byte getDirectionByte() {
        return (byte) this.ordinal();
    }
}
