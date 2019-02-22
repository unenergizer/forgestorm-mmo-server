package com.valenguard.server.game.inventory;

import lombok.Getter;

public class InventoryActions {

    /**
    *   CLIENT -> SERVER
    */
    public static final byte MOVE = 0x00;
    public static final byte DROP = 0x01;
    public static final byte USE = 0x02;

    /**
     *   SERVER -> CLIENT
     */
    public static final byte GIVE = 0x03;
    public static final byte REMOVE = 0x04;

    @Getter
    private byte inventoryActionType;

    @Getter
    private ItemStack itemStack;

    public InventoryActions(byte inventoryActionType, ItemStack itemStack) {
        this.inventoryActionType = inventoryActionType;
        this.itemStack = itemStack;
    }
}
