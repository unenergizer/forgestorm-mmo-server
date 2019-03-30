package com.valenguard.server.game.world.item.inventory;

import com.valenguard.server.game.world.item.ItemStack;
import lombok.Getter;

public class InventoryActions {

    /**
     *   SHARED
     */
    public static final byte MOVE = 0x00;

    /**
     *   CLIENT -> SERVER
     */
    public static final byte DROP = 0x01;
    public static final byte USE = 0x02;

    /**
     *   SERVER -> CLIENT
     */
    public static final byte GIVE = 0x03;
    public static final byte REMOVE = 0x04;
    public static final byte SET_BAG = 0x05;
    public static final byte SET_BANK = 0x06;
    public static final byte SET_EQUIPMENT = 0x07;

    @Getter
    private final byte inventoryActionType;

    @Getter
    private ItemStack itemStack;

    @Getter
    private byte slotIndex;

    /**
     *   MOVING ITEMS DATA
     */

    @Getter
    private byte fromPosition;

    @Getter
    private byte toPosition;

    @Getter
    private byte fromWindow;

    @Getter
    private byte toWindow;

    InventoryActions(ItemStack itemStack) {
        this.inventoryActionType = InventoryActions.GIVE;
        this.itemStack = new ItemStack(itemStack);
    }

    public InventoryActions(final byte inventoryActionType, byte slotIndex, ItemStack itemStack) {
        this.inventoryActionType = inventoryActionType;
        this.itemStack = new ItemStack(itemStack);
        this.slotIndex = slotIndex;
    }

    InventoryActions(byte slotIndex) {
        this.inventoryActionType = InventoryActions.REMOVE;
        this.slotIndex = slotIndex;
    }

    InventoryActions(InventoryType fromWindow, InventoryType toWindow, byte fromPosition, byte toPosition) {
        this.inventoryActionType = InventoryActions.MOVE;
        this.fromWindow = fromWindow.getInventoryTypeIndex();
        this.toWindow = toWindow.getInventoryTypeIndex();
        this.fromPosition = fromPosition;
        this.toPosition = toPosition;
    }
}
