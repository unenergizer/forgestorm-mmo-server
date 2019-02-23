package com.valenguard.server.game.inventory;

import com.valenguard.server.game.GameConstants;
import lombok.Getter;

public class PlayerBag {

    @Getter
    private final ItemStack[] items = new ItemStack[GameConstants.BAG_SIZE];

    public boolean isBagFull() {
        for (int i = 0; i < GameConstants.BAG_SIZE; i++) if (items[i] == null) return false;
        return true;
    }

    public void addItemStack(ItemStack itemStack) {
        if (isBagFull()) return;
        for (int i = 0; i < GameConstants.BAG_SIZE; i++) {
            if (items[i] == null) {
                items[i] = itemStack;
                return;
            }
        }
        throw new RuntimeException("Tried to add an item to a full inventory.");
    }

    public void removeItemStack(byte slotIndex) {
        items[slotIndex] = null;
    }

    public void moveItemStacks(byte fromPosition, byte toPosition) {
        ItemStack fromItemStack = items[fromPosition];
        ItemStack toItemStack = items[toPosition];
        items[toPosition] = fromItemStack;
        items[fromPosition] = toItemStack;
    }

    public int takenSlots() {
        int takenSlots = 0;
        for (int i = 0; i < GameConstants.BAG_SIZE; i++) {
            if (items[i] != null) takenSlots++;
        }
        return takenSlots;
    }

    public void setItemStack(byte index, ItemStack itemStack) {
        items[index] = itemStack;
    }

    public ItemStack getItemStack(byte index) {
        return items[index];
    }
}
