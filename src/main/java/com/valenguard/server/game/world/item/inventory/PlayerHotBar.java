package com.valenguard.server.game.world.item.inventory;

import com.valenguard.server.game.world.entity.Player;
import com.valenguard.server.network.game.packet.out.InventoryPacketOut;

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
