package com.forgestorm.server.game.world.item;

import com.forgestorm.server.io.ItemStackLoader;

import java.util.List;

public class ItemStackManager {

    private ItemStack[] itemStacks;

    public void start() {
        ItemStackLoader itemStackLoader = new ItemStackLoader();
        List<ItemStack> loadedItemStacks = itemStackLoader.loadItems();
        itemStacks = new ItemStack[loadedItemStacks.size()];
        loadedItemStacks.toArray(itemStacks);
    }

    public ItemStack makeItemStack(String name, int amount) {
        for (ItemStack itemStack : itemStacks) {
            if (itemStack.getName().toLowerCase().equals(name.toLowerCase())) {
                return (ItemStack) itemStack.clone();
            }
        }
        return null;
    }

    public ItemStack makeItemStack(int id, int amount) {
        ItemStack itemStack = (ItemStack) itemStacks[id].clone();
        itemStack.setAmount(amount);
        return itemStack;
    }

    public ItemStack makeItemStack(ItemStack itemStack) {
        return makeItemStack(itemStack.itemId, itemStack.getAmount());
    }

    public int getNumberOfItems() {
        return itemStacks.length;
    }
}
