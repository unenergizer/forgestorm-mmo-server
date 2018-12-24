package com.valenguard.server.game.inventory;

import lombok.Data;

@Data
public class ItemStack implements Cloneable {

    protected int itemId;
    private String name;
    private String description;
    private ItemStackType itemStackType;
    private boolean isStackable;
    private int amount;

    public ItemStack(int itemId) {
        this.itemId = itemId;
        // TODO get isStackable based on the item type / inventory type such as bank/player inventory ect..
    }

    @Override
    public Object clone() {
        ItemStack itemStack = generateCloneableInstance();
        itemStack.setName(name);
        itemStack.setDescription(description);
        itemStack.setItemStackType(itemStackType);
        itemStack.setStackable(isStackable);
        return itemStack;
    }

    protected ItemStack generateCloneableInstance() {
        return new ItemStack(itemId);
    }

    @Override
    public String toString() {
        return "{ ID = " + itemId + ", Amount = " + amount + " }";
    }
}
