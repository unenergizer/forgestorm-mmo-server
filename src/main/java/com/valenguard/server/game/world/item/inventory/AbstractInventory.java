package com.valenguard.server.game.world.item.inventory;

import com.valenguard.server.game.world.entity.Player;
import com.valenguard.server.game.world.item.ItemStack;
import com.valenguard.server.network.game.packet.out.InventoryPacketOut;
import lombok.Getter;

@Getter
public abstract class AbstractInventory {

    protected final Player inventoryOwner;
    protected final InventoryType inventoryType;
    protected final InventorySlot[] inventorySlotArray;


    AbstractInventory(final Player inventoryOwner, final InventoryType inventoryType, final int inventorySize) {
        this.inventoryOwner = inventoryOwner;
        this.inventoryType = inventoryType;

        // Setup inventory slots
        inventorySlotArray = new InventorySlot[inventorySize];
        for (byte slotIndex = 0; slotIndex < inventorySlotArray.length; slotIndex++) {
            inventorySlotArray[slotIndex] = new InventorySlot(slotIndex);
        }
    }

    public abstract void giveItemStack(final ItemStack itemStack, boolean sendPacket);

    public abstract void removeItemStack(final byte slotIndex, boolean sendPacket);

    public abstract void setItemStack(final byte slotIndex, final ItemStack itemStack, boolean sendPacket);

    public ItemStack getItemStack(byte slotIndex) {
        return inventorySlotArray[slotIndex].getItemStack();
    }

    void performInnerWindowMove(byte fromPositionIndex, byte toPositionIndex) {
        ItemStack fromItemStack = inventorySlotArray[fromPositionIndex].getItemStack();
        ItemStack toItemStack = inventorySlotArray[toPositionIndex].getItemStack();
        inventorySlotArray[toPositionIndex].setItemStack(fromItemStack);
        inventorySlotArray[fromPositionIndex].setItemStack(toItemStack);

        new InventoryPacketOut(inventoryOwner, new InventoryActions(
                inventoryType,
                inventoryType,
                fromPositionIndex,
                toPositionIndex)).sendPacket();
    }

    InventorySlot findEmptySlot() {
        for (InventorySlot inventorySlot : inventorySlotArray) {
            if (inventorySlot.getItemStack() == null) return inventorySlot;
        }
        return null;
    }

    public boolean isInventoryFull() {
        return findEmptySlot() == null;
    }

    public int takenSlots() {
        int takenSlots = 0;
        for (InventorySlot inventorySlot : inventorySlotArray) {
            if (inventorySlot.getItemStack() != null) takenSlots++;
        }
        return takenSlots;
    }

    public int freeSlots() {
        return inventorySlotArray.length - takenSlots();
    }
}
