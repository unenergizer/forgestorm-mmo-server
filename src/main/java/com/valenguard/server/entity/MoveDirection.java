package com.valenguard.server.entity;

public enum MoveDirection {
    UP,
    DOWN,
    LEFT,
    RIGHT,
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
