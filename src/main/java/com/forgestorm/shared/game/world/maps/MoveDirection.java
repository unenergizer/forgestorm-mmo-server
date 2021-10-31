package com.forgestorm.shared.game.world.maps;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MoveDirection {
    SOUTH("down"),
    EAST("right"),
    NORTH("up"),
    WEST("left"),
    NONE("down"); // Just defualt to down...

    private String directionName;

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
