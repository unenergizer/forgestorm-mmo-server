package com.forgestorm.server.game.world.item.inventory;

public enum BankActions {
    PLAYER_REQUEST_OPEN,
    PLAYER_REQUEST_CLOSE,
    SERVER_OPEN,
    SERVER_CLOSE;

    public static BankActions getType(byte typeByte) {
        for (BankActions value : BankActions.values()) {
            if ((byte) value.ordinal() == typeByte) {
                return value;
            }
        }
        return null;
    }

    public byte getTypeByte() {
        return (byte) this.ordinal();
    }
}
