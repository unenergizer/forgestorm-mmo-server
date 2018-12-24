package com.valenguard.server.game.inventory;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
class EquipmentSlot {

    private ItemStack itemStack;
    private ItemStackType itemStackType;

    EquipmentSlot(ItemStackType itemStackType) {
        this.itemStackType = itemStackType;
    }
}
