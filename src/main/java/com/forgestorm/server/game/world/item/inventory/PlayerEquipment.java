package com.forgestorm.server.game.world.item.inventory;

import com.forgestorm.server.game.PlayerConstants;
import com.forgestorm.shared.game.rpg.Attributes;
import com.forgestorm.server.game.world.entity.Player;
import com.forgestorm.shared.game.world.item.ItemStack;
import com.forgestorm.shared.game.world.item.ItemStackType;
import com.forgestorm.server.network.game.packet.out.EntityAttributesUpdatePacketOut;
import com.forgestorm.shared.game.world.item.inventory.EquipmentSlotTypes;
import com.forgestorm.shared.game.world.item.inventory.InventoryConstants;
import com.forgestorm.shared.game.world.item.inventory.InventoryType;

import static com.forgestorm.server.util.Log.println;
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

        // Update attributes
        updatePlayerAttributes(true);

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

        // Update attributes
        updatePlayerAttributes(true);

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
    private void updatePlayerAttributes(boolean sendAttributePacket) {
        Attributes playerClientAttributes = inventoryOwner.getAttributes();

        int armorUpdated = PlayerConstants.BASE_ARMOR;
        int damageUpdated = PlayerConstants.BASE_DAMAGE;

        for (InventorySlot inventorySlot : inventorySlotArray) {
            if (inventorySlot == null) continue;

            ItemStack itemStack = inventorySlot.getItemStack();
            if (itemStack == null) continue;

            Attributes attributes = itemStack.getAttributes();
            if (attributes == null) continue;

            armorUpdated = armorUpdated + attributes.getArmor();
            damageUpdated = damageUpdated + attributes.getDamage();
        }

        playerClientAttributes.setArmor(armorUpdated);
        playerClientAttributes.setDamage(damageUpdated);

        println(PRINT_DEBUG);
        println(getClass(), "ItemStack Armor: " + armorUpdated, false, PRINT_DEBUG);
        println(getClass(), "ItemStack Damage: " + damageUpdated, false, PRINT_DEBUG);
        println(getClass(), "Player Armor: " + playerClientAttributes.getArmor(), false, PRINT_DEBUG);
        println(getClass(), "Player Damage: " + playerClientAttributes.getDamage(), false, PRINT_DEBUG);

        // Send attributes packet
        if (sendAttributePacket) {
            println(getClass(), "Sending EntityAttributesUpdatePacketOut...", false, PRINT_DEBUG);
            new EntityAttributesUpdatePacketOut(inventoryOwner, inventoryOwner).sendPacket();
        }
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
    public void setItemStack(byte slotIndex, ItemStack itemStack, boolean sendPacket) {
        if (sendPacket) println(getClass(), "Primarily used by SQL. Do not send packets now!", true);
        inventorySlotArray[slotIndex].setItemStack(itemStack);

        updatePlayerAttributes(sendPacket);
        updateAppearance(slotIndex, sendPacket);
    }
}
