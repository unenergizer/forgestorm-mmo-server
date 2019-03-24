package com.valenguard.server.game.world.item.inventory;

import com.valenguard.server.game.world.item.ItemStack;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class EquipmentSlot implements Serializable {

    private transient EquipmentSlotTypes equipmentSlot;
    private ItemStack itemStack;

    EquipmentSlot(EquipmentSlotTypes equipmentSlot) {
        this.equipmentSlot = equipmentSlot;
    }
}
