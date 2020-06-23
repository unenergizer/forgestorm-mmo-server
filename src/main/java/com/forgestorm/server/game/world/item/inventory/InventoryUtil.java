package com.forgestorm.server.game.world.item.inventory;

import com.forgestorm.server.game.world.entity.Player;

public class InventoryUtil {

    private InventoryUtil() {
    }

    public static AbstractInventory getItemSlotContainer(byte inventoryByte, Player player) {
        InventoryType inventoryType = InventoryType.values()[inventoryByte];
        if (inventoryType == InventoryType.BAG_1) {
            return player.getPlayerBag();
        } else if (inventoryType == InventoryType.BANK) {
            return player.getPlayerBank();
        } else if (inventoryType == InventoryType.EQUIPMENT) {
            return player.getPlayerEquipment();
        } else if (inventoryType == InventoryType.HOT_BAR) {
            return player.getPlayerHotBar();
        }
        throw new RuntimeException("Impossible Case!");
    }
}
