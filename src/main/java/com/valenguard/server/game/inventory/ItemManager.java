package com.valenguard.server.game.inventory;

import java.util.List;

public class ItemManager {

    private ItemStack[] itemStacks;

    public ItemManager() {
        init();
    }

    private void init() {
        ItemLoader itemLoader = new ItemLoader();
        List<ItemStack> loadedItemStacks = itemLoader.loadItems();
        itemStacks = new ItemStack[loadedItemStacks.size()];
        loadedItemStacks.toArray(itemStacks);
    }

    public ItemStack makeItemStack(int id, int amount) {
        ItemStack itemStack = (ItemStack) itemStacks[id].clone();
        itemStack.setAmount(amount);
        return itemStack;
    }
}
