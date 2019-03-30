package com.valenguard.server.game.world.item.inventory;

import com.valenguard.server.game.world.entity.Player;
import com.valenguard.server.game.world.item.ItemStack;
import com.valenguard.server.game.world.item.ItemStackType;
import com.valenguard.server.network.game.packet.out.InventoryPacketOut;
import lombok.Getter;

public class PlayerBank {

    @Getter
    private final InventorySlot[] bankSlots;
    private final Player player;

    public PlayerBank(final Player player) {
        this.player = player;

        // Setup bag slots
        bankSlots = new InventorySlot[InventoryConstants.BANK_SIZE];
        for (byte slotIndex = 0; slotIndex < bankSlots.length; slotIndex++) {
            bankSlots[slotIndex] = new InventorySlot(slotIndex);
        }
    }

    public boolean isBankFull() {
        for (InventorySlot inventorySlot : bankSlots) {
            if (inventorySlot.getItemStack() == null) return false;
        }
        return true;
    }

    public void giveItemStack(ItemStack itemStack, boolean sendPacket) {
        if (isBankFull()) return;
        for (InventorySlot inventorySlot : bankSlots) {
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
        bankSlots[slotIndex].setItemStack(itemStack);
        if (sendPacket) {
            new InventoryPacketOut(player, new InventoryActions(InventoryActions.SET_BANK, (byte) slotIndex, itemStack)).sendPacket();
        }
    }

    public void removeItemStack(byte slotIndex, boolean sendPacket) {
        bankSlots[slotIndex].setItemStack(null);
        if (sendPacket) {
            new InventoryPacketOut(player, new InventoryActions(slotIndex)).sendPacket();
        }
    }

    void moveItemStack(byte fromPosition, byte toPosition) {
        ItemStack fromItemStack = bankSlots[fromPosition].getItemStack();
        ItemStack toItemStack = bankSlots[toPosition].getItemStack();
        bankSlots[toPosition].setItemStack(fromItemStack);
        bankSlots[fromPosition].setItemStack(toItemStack);

        new InventoryPacketOut(player, new InventoryActions(
                InventoryType.BANK,
                InventoryType.BANK,
                fromPosition,
                toPosition)).sendPacket();

    }

    public int takenSlots() {
        int takenSlots = 0;
        for (InventorySlot inventorySlot : bankSlots) {
            if (inventorySlot.getItemStack() != null) takenSlots++;
        }
        return takenSlots;
    }

    public InventorySlot getGoldInventorySlot() {
        for (InventorySlot inventorySlot : bankSlots) {
            ItemStack itemStack = inventorySlot.getItemStack();
            if (itemStack == null) continue;
            if (itemStack.getItemStackType() == ItemStackType.GOLD) {
                return inventorySlot;
            }
        }
        return null;
    }

    ItemStack getItemStack(byte slotIndex) {
        return bankSlots[slotIndex].getItemStack();
    }
}
