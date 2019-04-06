package com.valenguard.server.game.world.item.inventory;

import com.valenguard.server.game.world.entity.Player;
import com.valenguard.server.game.world.item.ItemStack;
import com.valenguard.server.game.world.item.ItemStackType;
import com.valenguard.server.network.game.packet.out.InventoryPacketOut;

public class PlayerBank extends AbstractInventory {

    public PlayerBank(Player inventoryOwner) {
        super(inventoryOwner, InventoryType.BANK, InventoryConstants.BANK_SIZE);
    }

    @Override
    public void giveItemStack(ItemStack itemStack, boolean sendPacket) {
        if (isInventoryFull()) return;
        findEmptySlot().setItemStack(itemStack);
        if (sendPacket) new InventoryPacketOut(inventoryOwner, new InventoryActions(itemStack)).sendPacket();
    }

    @Override
    public void removeItemStack(byte slotIndex, boolean sendPacket) {
        inventorySlotArray[slotIndex].setItemStack(null);
        if (sendPacket) new InventoryPacketOut(inventoryOwner, new InventoryActions(slotIndex)).sendPacket();
    }

    @Override
    public void setItemStack(byte slotIndex, ItemStack itemStack, boolean sendPacket) {
        inventorySlotArray[slotIndex].setItemStack(itemStack);
        if (sendPacket) {
            new InventoryPacketOut(inventoryOwner, new InventoryActions(InventoryActions.ActionType.SET_BANK, slotIndex, itemStack)).sendPacket();
        }
    }

    public InventorySlot getGoldInventorySlot() {
        for (InventorySlot inventorySlot : inventorySlotArray) {
            ItemStack itemStack = inventorySlot.getItemStack();
            if (itemStack == null) continue;
            if (itemStack.getItemStackType() == ItemStackType.GOLD) {
                return inventorySlot;
            }
        }
        return null;
    }
}
