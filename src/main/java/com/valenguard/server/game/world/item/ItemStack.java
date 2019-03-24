package com.valenguard.server.game.world.item;

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
        // TODO get isStackable based on the item type / item type such as bank/packetReceiver item ect..
    }

    public ItemStack(ItemStack itemStack) {
        this.itemId = itemStack.getItemId();
        this.name = itemStack.getName();
        this.description = itemStack.getDescription();
        this.itemStackType = itemStack.getItemStackType();
        this.isStackable = itemStack.isStackable();
        this.amount = itemStack.getAmount();
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
