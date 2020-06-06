package com.valenguard.server.game.world.item.inventory;

import com.valenguard.server.game.world.item.ItemStack;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
public class InventoryActions {

    private ActionType inventoryActionType;
    private ItemStack itemStack;
    private byte slotIndex;
    private byte fromPosition;
    private byte toPosition;
    private byte fromWindow;
    private byte toWindow;
    private byte interactInventory;

    public InventoryActions remove(InventoryType interactInventory, byte slotIndex) {
        this.inventoryActionType = InventoryActions.ActionType.REMOVE;
        this.slotIndex = slotIndex;
        this.interactInventory = interactInventory.getInventoryTypeIndex();
        return this;
    }

    public InventoryActions move(InventoryType fromWindow, InventoryType toWindow, byte fromPosition, byte toPosition) {
        this.inventoryActionType = InventoryActions.ActionType.MOVE;
        this.fromWindow = fromWindow.getInventoryTypeIndex();
        this.toWindow = toWindow.getInventoryTypeIndex();
        this.fromPosition = fromPosition;
        this.toPosition = toPosition;
        return this;
    }

    public InventoryActions set(InventoryType interactInventory, byte slotIndex, ItemStack itemStack) {
        this.inventoryActionType = InventoryActions.ActionType.SET;
        this.interactInventory = interactInventory.getInventoryTypeIndex();
        this.slotIndex = slotIndex;
        this.itemStack = itemStack;
        return this;
    }

    public InventoryActions consume(InventoryType interactInventory, byte slotIndex) {
        this.inventoryActionType = InventoryActions.ActionType.CONSUME;
        this.interactInventory = interactInventory.getInventoryTypeIndex();
        this.slotIndex = slotIndex;
        return this;
    }

    @Getter
    @AllArgsConstructor
    public enum ActionType {
        /**
         * SHARED
         */
        MOVE((byte) 0x00),

        /**
         * CLIENT -> SERVER
         */
        DROP((byte) 0x01),
        USE((byte) 0x02),
        CONSUME((byte) 0x03),

        /**
         * SERVER -> CLIENT
         */
        REMOVE((byte) 0x04),
        SET((byte) 0x05);

        private byte getActionType;

        public static ActionType getActionType(byte inventoryActionType) {
            for (ActionType entityType : ActionType.values()) {
                if ((byte) entityType.ordinal() == inventoryActionType) return entityType;
            }
            throw new RuntimeException("ActionType miss match! Byte Received: " + inventoryActionType);
        }
    }
}
