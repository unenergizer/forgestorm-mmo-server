package com.valenguard.server.game.world.item;

import com.valenguard.server.game.rpg.Attributes;
import lombok.Data;

import java.io.Serializable;

@Data
public class ItemStack implements Cloneable, Serializable {

    protected int itemId;
    private transient String name;
    private transient String description;
    private transient ItemStackType itemStackType;
    private transient boolean isStackable;
    private int amount;
    private transient boolean isConsumable;

    private transient Attributes attributes;

    public ItemStack(int itemId) {
        this.itemId = itemId;
    }

    public ItemStack(ItemStack itemStack) {
        this.itemId = itemStack.getItemId();
        this.name = itemStack.getName();
        this.description = itemStack.getDescription();
        this.itemStackType = itemStack.getItemStackType();
        this.isStackable = itemStack.isStackable();
        this.attributes = itemStack.getAttributes();
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
        itemStack.setConsumable(isConsumable);
        return itemStack;
    }

    ItemStack generateCloneableInstance() {
        return new ItemStack(itemId);
    }

    @Override
    public String toString() {
        return "{ ID = " + itemId + ", Name = " + name + ", Amount = " + amount + " }";
    }
}
