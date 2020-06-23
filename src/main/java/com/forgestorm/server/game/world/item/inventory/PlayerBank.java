package com.forgestorm.server.game.world.item.inventory;

import com.forgestorm.server.game.world.entity.Player;

public class PlayerBank extends AbstractInventory {

    public PlayerBank(Player inventoryOwner) {
        super(inventoryOwner, InventoryType.BANK, InventoryConstants.BANK_SIZE);
    }
}
