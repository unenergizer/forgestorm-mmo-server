package com.valenguard.server.game.inventory;

public class PlayerInventory {

    private static final int CAPACITY = 5*6;

    private ItemStack[] items = new ItemStack[CAPACITY];

    public void addItemStack(ItemStack itemStack) {
        for (int i = 0; i < CAPACITY; i++) {
            if (items[i] == null) {
                items[i] = itemStack;
                return;
            }
        }
        throw new RuntimeException("Tried to add an item to a full inventory.");
    }

    public void moveItem(byte fromPosition, byte toPosition) {
        ItemStack fromItemStack = items[fromPosition];
        ItemStack toItemStack = items[toPosition];
        items[toPosition] = fromItemStack;
        items[fromPosition] = toItemStack;
    }

}
