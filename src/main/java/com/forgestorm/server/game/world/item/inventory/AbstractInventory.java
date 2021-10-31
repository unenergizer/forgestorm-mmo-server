package com.forgestorm.server.game.world.item.inventory;

import com.forgestorm.server.ServerMain;
import com.forgestorm.server.game.world.entity.Player;
import com.forgestorm.shared.game.world.item.ItemStack;
import com.forgestorm.server.game.world.item.ItemStackManager;
import com.forgestorm.shared.game.world.item.ItemStackType;
import com.forgestorm.server.network.game.packet.out.InventoryPacketOutOut;
import com.forgestorm.shared.game.world.item.inventory.InventoryType;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public abstract class AbstractInventory {

    private final ItemStackManager itemStackManager = ServerMain.getInstance().getItemStackManager();

    protected final Player inventoryOwner;
    protected final InventoryType inventoryType;
    protected final InventorySlot[] inventorySlotArray;

    AbstractInventory(final Player inventoryOwner, final InventoryType inventoryType, final int inventorySize) {
        this.inventoryOwner = inventoryOwner;
        this.inventoryType = inventoryType;

        // Setup inventory slots
        inventorySlotArray = new InventorySlot[inventorySize];
        for (byte slotIndex = 0; slotIndex < inventorySlotArray.length; slotIndex++) {
            inventorySlotArray[slotIndex] = new InventorySlot(this, slotIndex);
        }
    }

    public void removeItemStack(final byte slotIndex, boolean sendPacket) {
        inventorySlotArray[slotIndex].setItemStack(null);
        if (sendPacket)
            new InventoryPacketOutOut(inventoryOwner, new InventoryActions().remove(inventoryType, slotIndex)).sendPacket();
    }

    public void setItemStack(final byte slotIndex, final ItemStack itemStack, boolean sendPacket) {
        inventorySlotArray[slotIndex].setItemStack(itemStack);
        if (sendPacket) {
            new InventoryPacketOutOut(inventoryOwner, new InventoryActions().set(inventoryType, slotIndex, itemStack)).sendPacket();
        }
    }

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

        if (toItemStack != null && fromItemStack.getStackable() > 1 && toItemStack.getStackable() > 1
                && fromItemStack.getItemStackType() == toItemStack.getItemStackType()) {

            // TODO: Check max size

            ItemStack itemStack = new ItemStack(fromItemStack);
            itemStack.setAmount(fromItemStack.getAmount() + toItemStack.getAmount());

            inventorySlotArray[fromPositionIndex].setItemStack(null);
            toInventory[toPositionIndex].setItemStack(itemStack);
        } else {
            // Do swap
            inventorySlotArray[fromPositionIndex].setItemStack(toItemStack);
            toInventory[toPositionIndex].setItemStack(fromItemStack);
        }

        return true;
    }

    public InventorySlot findEmptySlot() {
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

    public List<InventorySlot> getGoldSlots() {
        List<InventorySlot> slots = new ArrayList<>();
        for (InventorySlot inventorySlot : inventorySlotArray) {
            ItemStack itemStack = inventorySlot.getItemStack();
            if (itemStack == null) continue;
            if (itemStack.getItemStackType() == ItemStackType.GOLD) {
                slots.add(inventorySlot);
            }
        }
        return slots;
    }
}
