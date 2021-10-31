package com.forgestorm.server.game.world.item.inventory;

import com.forgestorm.shared.game.world.item.ItemStack;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class InventorySlot implements Serializable {

    private final transient byte slotIndex;
    private final transient AbstractInventory inventory;
    private ItemStack itemStack = null;

    InventorySlot(AbstractInventory inventory, final byte slotIndex) {
        this.inventory = inventory;
        this.slotIndex = slotIndex;
    }
}
