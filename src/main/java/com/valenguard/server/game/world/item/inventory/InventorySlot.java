package com.valenguard.server.game.world.item.inventory;

import com.valenguard.server.game.world.item.ItemStack;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class InventorySlot implements Serializable {

    private final transient byte slotIndex;
    private ItemStack itemStack = null;

    InventorySlot(final byte slotIndex) {
        this.slotIndex = slotIndex;
    }
}
