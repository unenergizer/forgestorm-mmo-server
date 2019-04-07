package com.valenguard.server.game.world.item.inventory;

import com.valenguard.server.Server;
import com.valenguard.server.game.world.entity.Player;
import com.valenguard.server.game.world.item.ItemStack;
import com.valenguard.server.game.world.item.ItemStackManager;
import lombok.Getter;

@Getter
public abstract class AbstractInventory {

    private final ItemStackManager itemStackManager = Server.getInstance().getItemStackManager();

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

    public void setInventory(InventorySlot[] inventoryStacks) {
        if (inventoryStacks == null) return;
        for (byte slotIndex = 0; slotIndex < inventoryStacks.length; slotIndex++) {
            if (inventoryStacks[slotIndex].getItemStack() != null) {
                setItemStack(slotIndex, itemStackManager.makeItemStack(inventoryStacks[slotIndex].getItemStack()), false);
            }
        }
    }

    public ItemStack getItemStack(byte slotIndex) {
        return inventorySlotArray[slotIndex].getItemStack();
    }

    boolean performItemStackMove(AbstractInventory abstractInventory, byte fromPositionIndex, byte toPositionIndex) {
        InventorySlot[] toInventory = abstractInventory.getInventorySlotArray();

        // Grab Items
        ItemStack fromItemStack = inventorySlotArray[fromPositionIndex].getItemStack();
        ItemStack toItemStack = toInventory[toPositionIndex].getItemStack();

        // Do swap
        inventorySlotArray[fromPositionIndex].setItemStack(toItemStack);
        toInventory[toPositionIndex].setItemStack(fromItemStack);

        return true;
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
}
