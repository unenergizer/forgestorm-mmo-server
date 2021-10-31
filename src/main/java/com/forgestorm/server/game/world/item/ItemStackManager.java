package com.forgestorm.server.game.world.item;

import com.forgestorm.server.ServerMain;
import com.forgestorm.server.game.ManagerStart;
import com.forgestorm.shared.game.world.item.ItemStack;

import java.util.List;

public class ItemStackManager implements ManagerStart {

    private ItemStack[] itemStacks;

    @Override
    public void start() {
        ServerMain.getInstance().getFileManager().loadItemStackData();
        List<ItemStack> loadedItemStacks = ServerMain.getInstance().getFileManager().getItemStackData().getItemStackList();
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
        return makeItemStack(itemStack.getItemId(), itemStack.getAmount());
    }

    public int getNumberOfItems() {
        return itemStacks.length;
    }
}
