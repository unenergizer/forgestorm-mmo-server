package com.valenguard.server.game.world.item.inventory;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EquipmentSlot {

    private final EquipmentSlotTypes equipmentSlotTypes;
    private final InventorySlot inventorySlot;

    EquipmentSlot(final byte slotIndex, final EquipmentSlotTypes equipmentSlotTypes) {
        this.inventorySlot = new InventorySlot(slotIndex);
        this.equipmentSlotTypes = equipmentSlotTypes;
    }
}
