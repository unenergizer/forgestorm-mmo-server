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

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    boolean swapBagAndEquipmentWindow(PlayerBag playerBag, byte bagIndex, byte equipmentIndex, boolean equipItem) {
        ItemStack bagItemStack = playerBag.getItemStack(bagIndex);
        ItemStack equipmentItemStack = getItemStack(equipmentIndex);

        // Confirming that the equipment is allowed to be switched.
        // If bagItemStack == null then the equipment is being removed.
        if (bagItemStack != null) {
            boolean foundType = false;
            for (ItemStackType itemStackType : requireNonNull(getAcceptedItemStackTypes(equipmentIndex))) {
                if (itemStackType == bagItemStack.getItemStackType()) foundType = true;
            }
            if (!foundType) return false;
        }

        playerBag.setItemStack(bagIndex, equipmentItemStack, false);
        inventorySlotArray[equipmentIndex].setItemStack(bagItemStack);

        updatePlayerAttributes(bagItemStack, equipmentItemStack, equipItem, true);

        InventorySlot inventorySlot = inventorySlotArray[equipmentIndex];
        ItemStackType acceptedItemStackType = requireNonNull(getAcceptedItemStackTypes(equipmentIndex))[0];
        inventoryOwner.getAppearance().updatePlayerAppearance(inventorySlot.getItemStack(), acceptedItemStackType, true);

        return true;
    }

    /**
     * Update the {@link Player} with the {@link Attributes} found on equipped item.
     */
    private void updatePlayerAttributes(ItemStack bagItemStack, ItemStack equipItemStack, boolean equipItem, boolean sendAttributePacket) {

        Attributes playerClientAttributes = inventoryOwner.getAttributes();
        Attributes itemStackAttributes = equipItem ? bagItemStack.getAttributes() : equipItemStack.getAttributes();

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

            if (equipItemStack != null) {
                println(getClass(), "SWAPPING ITEM STATES", false, PRINT_DEBUG);

                Attributes removedAttributes = equipItemStack.getAttributes();
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

            if (bagItemStack != null) {
                Attributes addAttributes = bagItemStack.getAttributes();
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

    private ItemStackType[] getAcceptedItemStackTypes(byte slotIndex) {
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
        
        InventorySlot inventorySlot = inventorySlotArray[slotIndex];
        ItemStackType acceptedItemStackType = requireNonNull(getAcceptedItemStackTypes(slotIndex))[0];
        inventoryOwner.getAppearance().updatePlayerAppearance(inventorySlot.getItemStack(), acceptedItemStackType, sendPacket);
    }
}
