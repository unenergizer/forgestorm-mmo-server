package com.forgestorm.server.game.world.item.inventory;

import com.forgestorm.server.game.world.entity.Player;
import com.forgestorm.shared.game.world.item.inventory.InventoryConstants;
import com.forgestorm.shared.game.world.item.inventory.InventoryType;

public class PlayerBank extends AbstractInventory {

    public PlayerBank(Player inventoryOwner) {
        super(inventoryOwner, InventoryType.BANK, InventoryConstants.BANK_SIZE);
    }
}
