package com.valenguard.server.game.world.item.inventory;

import com.valenguard.server.game.rpg.Attributes;
import com.valenguard.server.game.world.entity.Player;
import com.valenguard.server.game.world.item.ItemStack;
import com.valenguard.server.game.world.item.ItemStackType;
import com.valenguard.server.network.game.packet.out.EntityAttributesUpdatePacketOut;

import static com.valenguard.server.util.Log.println;
import static java.util.Objects.requireNonNull;

public class PlayerEquipment extends AbstractInventory {

    private static final boolean PRINT_DEBUG = false;

    public PlayerEquipment(Player inventoryOwner) {
        super(inventoryOwner, InventoryType.EQUIPMENT, InventoryConstants.EQUIPMENT_SIZE);
    }

    boolean performItemStackMoveSpecial(AbstractInventory fromInventory, byte fromPositionIndex, byte toPositionIndex) {
        InventorySlot[] fromInventorySlotArray = fromInventory.getInventorySlotArray();

        // Grab Items
        ItemStack toItemStack = inventorySlotArray[toPositionIndex].getItemStack();
        ItemStack fromItemStack = fromInventorySlotArray[fromPositionIndex].getItemStack();

        if (!isEquippable(fromItemStack, toPositionIndex)) return false;

        // Do swap
        inventorySlotArray[toPositionIndex].setItemStack(fromItemStack);
        fromInventorySlotArray[fromPositionIndex].setItemStack(toItemStack);

        // TODO: Update attributes
//        if (!insideEquipmentSwap) updatePlayerAttributes(bagItemStack, equipmentItemStack, equipItem, true);

        println(getClass(), "Special check...", false, PRINT_DEBUG);
        updateAppearance(toPositionIndex, true);

        return true;
    }

    @Override
    boolean performItemStackMove(AbstractInventory toInventory, byte fromPositionIndex, byte toPositionIndex) {
        InventorySlot[] toInventorySlotArray = toInventory.getInventorySlotArray();

        // Grab Items
        ItemStack fromItemStack = inventorySlotArray[fromPositionIndex].getItemStack();
        ItemStack toItemStack = toInventorySlotArray[toPositionIndex].getItemStack();

        boolean insideEquipmentSwap = toInventory instanceof PlayerEquipment;
        boolean outsideEquipmentSwap = toItemStack != null;

        // Check if ItemStack type can be equipped.
        println(getClass(), "Attempting swap...", false, PRINT_DEBUG);

        if (insideEquipmentSwap) {
            // Inside equipment swap, lets make sure both equipment slots can perform swap.
            println(getClass(), "Swap Type: Inside Equipment Swap", false, PRINT_DEBUG);
            if (!isEquippable(fromItemStack, toPositionIndex)) return false;
            if (!isEquippable(toItemStack, fromPositionIndex)) return false;

        } else if (outsideEquipmentSwap) {
            // Outside equipment swap (eg from equipment to bag, or bank to equipment).
            // Make sure the incoming item is supported.
            println(getClass(), "Swap Type: Outside Equipment Swap", false, PRINT_DEBUG);
            if (!isEquippable(toItemStack, fromPositionIndex)) return false;
        }

        println(getClass(), "Swap passed checks...", false, PRINT_DEBUG);

        // Do swap
        inventorySlotArray[fromPositionIndex].setItemStack(toItemStack);
        toInventorySlotArray[toPositionIndex].setItemStack(fromItemStack);

        // TODO: Update attributes
//        if (!insideEquipmentSwap) updatePlayerAttributes(bagItemStack, equipmentItemStack, equipItem, true);

        println(getClass(), "Overridden check...", false, PRINT_DEBUG);
        updateAppearance(fromPositionIndex, true);
        return true;
    }

    private boolean isEquippable(ItemStack itemStackToCheck, byte destinationSlotIndex) {
        for (ItemStackType itemStackType : requireNonNull(getAcceptedItemStackTypesArray(destinationSlotIndex))) {
            if (itemStackType == itemStackToCheck.getItemStackType()) return true;
        }
        return false;
    }

    /**
     * Update the {@link Player} with the {@link Attributes} found on equipped item.
     */
    private void updatePlayerAttributes(ItemStack fromItemStack, ItemStack toItemStack, boolean equipItem, boolean sendAttributePacket) {

        Attributes playerClientAttributes = inventoryOwner.getAttributes();
        Attributes itemStackAttributes = equipItem ? fromItemStack.getAttributes() : toItemStack.getAttributes();

        println(PRINT_DEBUG);
        println(getClass(), "PC Armor: " + playerClientAttributes.getArmor(), false, PRINT_DEBUG);
        println(getClass(), "PC Damage: " + playerClientAttributes.getDamage(), false, PRINT_DEBUG);
        println(getClass(), "IS Armor: " + itemStackAttributes.getArmor(), false, PRINT_DEBUG);
        println(getClass(), "IS Damage: " + itemStackAttributes.getDamage(), false, PRINT_DEBUG);

        // TODO: Instead of manually adding the new values, we should possible loop through all equipped items and get values this way.
        if (equipItem) {
            // Player Equipped an Item. Update attributes!
            playerClientAttributes.setArmor(playerClientAttributes.getArmor() + itemStackAttributes.getArmor());
            playerClientAttributes.setDamage(playerClientAttributes.getDamage() + itemStackAttributes.getDamage());

            if (toItemStack != null) {
                println(getClass(), "SWAPPING ITEM STATES", false, PRINT_DEBUG);

                Attributes removedAttributes = toItemStack.getAttributes();
                playerClientAttributes.setArmor(playerClientAttributes.getArmor() - removedAttributes.getArmor());
                playerClientAttributes.setDamage(playerClientAttributes.getDamage() - removedAttributes.getDamage());
            } else {
                println(getClass(), "ADDING ITEM STATES", false, PRINT_DEBUG);
            }

        } else {

            println(getClass(), "REMOVING ITEM STATES", false, PRINT_DEBUG);

            // Player Unequipped an Item. Update attributes!
            playerClientAttributes.setArmor(playerClientAttributes.getArmor() - itemStackAttributes.getArmor());
            playerClientAttributes.setDamage(playerClientAttributes.getDamage() - itemStackAttributes.getDamage());

            if (fromItemStack != null) {
                Attributes addAttributes = fromItemStack.getAttributes();
                playerClientAttributes.setArmor(playerClientAttributes.getArmor() + addAttributes.getArmor());
                playerClientAttributes.setDamage(playerClientAttributes.getDamage() + addAttributes.getDamage());
            }
        }

        println(getClass(), "PC Armor: " + playerClientAttributes.getArmor(), false, PRINT_DEBUG);
        println(getClass(), "PC Final Damage: " + playerClientAttributes.getDamage(), false, PRINT_DEBUG);
        println(PRINT_DEBUG);

        // Send attributes packet
        if (sendAttributePacket) new EntityAttributesUpdatePacketOut(inventoryOwner, inventoryOwner).sendPacket();
    }

    private void updateAppearance(byte slotIndex, boolean sendPacket) {
        println(getClass(), "Appearance update for slot index: " + slotIndex, false, PRINT_DEBUG);
        InventorySlot inventorySlot = inventorySlotArray[slotIndex];
        ItemStackType acceptedItemStackType = requireNonNull(getAcceptedItemStackTypesArray(slotIndex))[0];
        inventoryOwner.getAppearance().updatePlayerAppearance(inventorySlot.getItemStack(), acceptedItemStackType, sendPacket);
    }

    private ItemStackType[] getAcceptedItemStackTypesArray(byte slotIndex) {
        for (EquipmentSlotTypes equipmentSlotTypes : EquipmentSlotTypes.values()) {
            if (equipmentSlotTypes.getSlotIndex() == slotIndex) return equipmentSlotTypes.getAcceptedItemStackTypes();
        }
        return null;
    }

    @Override
    public void giveItemStack(ItemStack itemStack, boolean sendPacket) {
        // TODO
    }

    @Override
    public void removeItemStack(byte positionIndex, boolean sendPacket) {
        // TODO
    }

    @Override
    public void setItemStack(byte slotIndex, ItemStack itemStack, boolean sendPacket) {
        if (sendPacket) println(getClass(), "Primarily used by SQL. Do not send packets now!", true);
        inventorySlotArray[slotIndex].setItemStack(itemStack);

        updatePlayerAttributes(itemStack, null, true, sendPacket);
        updateAppearance(slotIndex, sendPacket);
    }
}
