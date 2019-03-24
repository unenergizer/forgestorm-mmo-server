package com.valenguard.server.game.rpg;

public enum ShopOpcodes {

    START_SHOPPING,
    BUY,
    SELL,
    STOP_SHOPPING;

    public static ShopOpcodes getShopOpcode(byte entityTypeByte) {
        for (ShopOpcodes npcShopOpcode : ShopOpcodes.values()) {
            if ((byte) npcShopOpcode.ordinal() == entityTypeByte) {
                return npcShopOpcode;
            }
        }
        return null;
    }
}
