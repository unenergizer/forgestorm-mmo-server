package com.valenguard.server.game.world.item.inventory;

import com.valenguard.server.game.world.item.ItemStack;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
class EquipmentSlot {

    private EquipmentSlotTypes equipmentSlot;
    private ItemStack itemStack;

    EquipmentSlot(EquipmentSlotTypes equipmentSlot) {
        this.equipmentSlot = equipmentSlot;
    }
}
