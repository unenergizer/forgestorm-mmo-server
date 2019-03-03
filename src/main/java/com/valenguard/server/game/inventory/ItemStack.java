package com.valenguard.server.game.inventory;

import com.valenguard.server.game.rpg.Attributes;
import lombok.Data;

@Data
public class ItemStack implements Cloneable {

    protected int itemId;
    private String name;
    private String description;
    private ItemStackType itemStackType;
    private boolean isStackable;
    private int amount;

    private Attributes attributes;

    public ItemStack(int itemId) {
        this.itemId = itemId;
        // TODO get isStackable based on the item type / inventory type such as bank/player inventory ect..
    }

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    @Override
    public Object clone() {
        ItemStack itemStack = generateCloneableInstance();
        itemStack.setName(name);
        itemStack.setDescription(description);
        itemStack.setItemStackType(itemStackType);
        itemStack.setStackable(isStackable);
        itemStack.setAttributes(attributes);
        return itemStack;
    }

    ItemStack generateCloneableInstance() {
        return new ItemStack(itemId);
    }

    @Override
    public String toString() {
        return "{ ID = " + itemId + ", Amount = " + amount + " }";
    }
}