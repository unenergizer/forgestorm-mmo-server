package com.valenguard.server.game.inventory;

public class PlayerBag {

    public static final int CAPACITY = 5 * 6;

    private final ItemStack[] items = new ItemStack[CAPACITY];

    public boolean isBagFull() {
        for (int i = 0; i < CAPACITY; i++) if (items[i] == null) return false;
        return true;
    }

    public void addItemStack(ItemStack itemStack) {
        if (isBagFull()) return;
        for (int i = 0; i < CAPACITY; i++) {
            if (items[i] == null) {
                items[i] = itemStack;
                return;
            }
        }
        throw new RuntimeException("Tried to add an item to a full inventory.");
    }

    public void moveItemStacks(byte fromPosition, byte toPosition) {
        ItemStack fromItemStack = items[fromPosition];
        ItemStack toItemStack = items[toPosition];
        items[toPosition] = fromItemStack;
        items[fromPosition] = toItemStack;
    }

    public void setItemStack(byte index, ItemStack itemStack) {
        items[index] = itemStack;
    }

    public ItemStack getItemStack(byte index) {
        return items[index];
    }
}
