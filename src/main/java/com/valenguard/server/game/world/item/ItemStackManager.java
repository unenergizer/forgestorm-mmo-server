package com.valenguard.server.game.world.item;

import com.valenguard.server.io.ItemStackLoader;

import java.util.List;

public class ItemStackManager {

    private ItemStack[] itemStacks;

    public void start() {
        ItemStackLoader itemStackLoader = new ItemStackLoader();
        List<ItemStack> loadedItemStacks = itemStackLoader.loadItems();
        itemStacks = new ItemStack[loadedItemStacks.size()];
        loadedItemStacks.toArray(itemStacks);
    }

    public ItemStack makeItemStack(int id, int amount) {
        ItemStack itemStack = (ItemStack) itemStacks[id].clone();
        itemStack.setAmount(amount);
        return itemStack;
    }

    public int numberOfItems() {
        return itemStacks.length;
    }
}
