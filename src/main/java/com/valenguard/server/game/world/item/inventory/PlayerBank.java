package com.valenguard.server.game.world.item.inventory;

import com.valenguard.server.game.world.entity.Player;

public class PlayerBank extends AbstractInventory {

    public PlayerBank(Player inventoryOwner) {
        super(inventoryOwner, InventoryType.BANK, InventoryConstants.BANK_SIZE);
    }
}
