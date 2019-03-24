package com.valenguard.server.game.world.item.inventory;

import com.valenguard.server.game.world.entity.Player;
import com.valenguard.server.game.world.item.ItemStack;
import com.valenguard.server.game.world.item.ItemStackType;
import com.valenguard.server.network.game.packet.out.InventoryPacketOut;
import lombok.Getter;

public class PlayerBag {

    @Getter
    private final InventorySlot[] bagSlots;
    private final Player player;

    public PlayerBag(final Player player) {
        this.player = player;

        // Setup bag slots
        bagSlots = new InventorySlot[InventoryConstants.BAG_SIZE];
        for (byte slotIndex = 0; slotIndex < bagSlots.length; slotIndex++) {
            bagSlots[slotIndex] = new InventorySlot(slotIndex);
        }
    }

    public boolean isBagFull() {
        for (InventorySlot inventorySlot : bagSlots) {
            if (inventorySlot.getItemStack() == null) return false;
        }
        return true;
    }

    public void giveItemStack(ItemStack itemStack, boolean sendPacket) {
        if (isBagFull()) return;
        for (InventorySlot inventorySlot : bagSlots) {
            if (inventorySlot.getItemStack() == null) {
                inventorySlot.setItemStack(itemStack);
                break;
            }
        }
        if (sendPacket) {
            new InventoryPacketOut(player, new InventoryActions(itemStack)).sendPacket();
        }
    }

    public void setItemStack(int slotIndex, ItemStack itemStack, boolean sendPacket) {
        bagSlots[slotIndex].setItemStack(itemStack);
        if (sendPacket) {
            new InventoryPacketOut(player, new InventoryActions(InventoryActions.SET_BAG, (byte) slotIndex, itemStack)).sendPacket();
        }
    }

    public void removeItemStack(byte slotIndex, boolean sendPacket) {
        bagSlots[slotIndex].setItemStack(null);
        if (sendPacket) {
            new InventoryPacketOut(player, new InventoryActions(slotIndex)).sendPacket();
        }
    }

    void moveItemStack(byte fromPosition, byte toPosition) {
        ItemStack fromItemStack = bagSlots[fromPosition].getItemStack();
        ItemStack toItemStack = bagSlots[toPosition].getItemStack();
        bagSlots[toPosition].setItemStack(fromItemStack);
        bagSlots[fromPosition].setItemStack(toItemStack);

        new InventoryPacketOut(player, new InventoryActions(
                InventoryType.BAG_1,
                InventoryType.BAG_1,
                fromPosition,
                toPosition)).sendPacket();

    }

    public int takenSlots() {
        int takenSlots = 0;
        for (InventorySlot inventorySlot : bagSlots) {
            if (inventorySlot.getItemStack() != null) takenSlots++;
        }
        return takenSlots;
    }

    public InventorySlot getGoldInventorySlot() {
        for (InventorySlot inventorySlot : bagSlots) {
            ItemStack itemStack = inventorySlot.getItemStack();
            if (itemStack == null) continue;
            if (itemStack.getItemStackType() == ItemStackType.GOLD) {
                return inventorySlot;
            }
        }
        return null;
    }

    ItemStack getItemStack(byte slotIndex) {
        return bagSlots[slotIndex].getItemStack();
    }
}
