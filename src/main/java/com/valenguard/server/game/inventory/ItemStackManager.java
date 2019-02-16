package com.valenguard.server.game.inventory;

import com.valenguard.server.game.data.ItemStackLoader;

import java.util.List;

public class ItemStackManager {

    private ItemStack[] itemStacks;

    public ItemStackManager() {
        init();
    }

    private void init() {
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
