package com.valenguard.server.game.world.item.inventory;

import com.valenguard.server.game.world.entity.Player;
import com.valenguard.server.game.world.item.ItemStack;
import com.valenguard.server.game.world.item.ItemStackType;

public class PlayerBag extends AbstractInventory {

    public PlayerBag(Player inventoryOwner) {
        super(inventoryOwner, InventoryType.BAG_1, InventoryConstants.BAG_SIZE);
    }

    public InventorySlot getGoldInventorySlot() {
        for (InventorySlot inventorySlot : inventorySlotArray) {
            ItemStack itemStack = inventorySlot.getItemStack();
            if (itemStack == null) continue;
            if (itemStack.getItemStackType() == ItemStackType.GOLD) {
                return inventorySlot;
            }
        }
        return null;
    }
}
