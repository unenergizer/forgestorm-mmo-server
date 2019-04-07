package com.valenguard.server.game.world.item.inventory;

import com.valenguard.server.game.world.item.ItemStack;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
public class InventoryActions {

    private final ActionType inventoryActionType;
    private ItemStack itemStack;
    private byte slotIndex;
    private byte fromPosition;
    private byte toPosition;
    private byte fromWindow;
    private byte toWindow;

    InventoryActions(ItemStack itemStack) {
        this.inventoryActionType = InventoryActions.ActionType.GIVE;
        this.itemStack = new ItemStack(itemStack);
    }

    public InventoryActions(ActionType inventoryActionType, byte slotIndex, ItemStack itemStack) {
        this.inventoryActionType = inventoryActionType;
        this.itemStack = new ItemStack(itemStack);
        this.slotIndex = slotIndex;
    }

    InventoryActions(byte slotIndex) {
        this.inventoryActionType = InventoryActions.ActionType.REMOVE;
        this.slotIndex = slotIndex;
    }

    InventoryActions(InventoryType fromWindow, InventoryType toWindow, byte fromPosition, byte toPosition) {
        this.inventoryActionType = InventoryActions.ActionType.MOVE;
        this.fromWindow = fromWindow.getInventoryTypeIndex();
        this.toWindow = toWindow.getInventoryTypeIndex();
        this.fromPosition = fromPosition;
        this.toPosition = toPosition;
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

        /**
         * SERVER -> CLIENT
         */
        GIVE((byte) 0x03),
        REMOVE((byte) 0x04),
        SET_BAG((byte) 0x05),
        SET_BANK((byte) 0x06),
        SET_EQUIPMENT((byte) 0x07);

        private byte getActionType;

        public static ActionType getActionType(byte inventoryActionType) {
            for (ActionType entityType : ActionType.values()) {
                if ((byte) entityType.ordinal() == inventoryActionType) return entityType;
            }
            throw new RuntimeException("ActionType miss match! Byte Received: " + inventoryActionType);
        }
    }
}
