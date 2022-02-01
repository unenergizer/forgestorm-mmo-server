package com.forgestorm.server.game.world.item.inventory;

import com.forgestorm.server.game.world.entity.Player;
import com.forgestorm.server.network.game.packet.out.InventoryPacketOut;
import com.forgestorm.shared.game.world.item.inventory.InventoryConstants;
import com.forgestorm.shared.game.world.item.inventory.InventoryType;

public class PlayerHotBar extends AbstractInventory {

    public PlayerHotBar(Player inventoryOwner) {
        super(inventoryOwner, InventoryType.HOT_BAR, InventoryConstants.HOT_BAR_SIZE);
    }

    @Override
    public void removeItemStack(byte slotIndex, boolean sendPacket) {
        inventorySlotArray[slotIndex].setItemStack(null);
        if (sendPacket)
            new InventoryPacketOut(inventoryOwner, new InventoryActions().remove(InventoryType.HOT_BAR, slotIndex)).sendPacket();
    }
}
