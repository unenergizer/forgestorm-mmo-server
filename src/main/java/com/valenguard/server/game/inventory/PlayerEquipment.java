package com.valenguard.server.game.inventory;

import com.valenguard.server.game.entity.Player;
import com.valenguard.server.game.rpg.Attributes;
import com.valenguard.server.network.packet.out.EntityAttributesUpdatePacketOut;
import lombok.Getter;

import static com.valenguard.server.util.Log.println;

public class PlayerEquipment {

    private static final boolean PRINT_DEBUG = false;

    public static final int CAPACITY = 12;

    @Getter
    private final EquipmentSlot[] equipmentSlots = new EquipmentSlot[CAPACITY];

    private Player player;

    public void init(Player player) {
        this.player = player;
        // Main Body
        equipmentSlots[0] = new EquipmentSlot(EquipmentSlotTypes.HELM);
        equipmentSlots[1] = new EquipmentSlot(EquipmentSlotTypes.CHEST);
        equipmentSlots[2] = new EquipmentSlot(EquipmentSlotTypes.BOOTS);
        equipmentSlots[3] = new EquipmentSlot(EquipmentSlotTypes.CAPE);
        equipmentSlots[4] = new EquipmentSlot(EquipmentSlotTypes.GLOVES);
        equipmentSlots[5] = new EquipmentSlot(EquipmentSlotTypes.BELT);

        // Trinket
        equipmentSlots[6] = new EquipmentSlot(EquipmentSlotTypes.RING_0);
        equipmentSlots[7] = new EquipmentSlot(EquipmentSlotTypes.RING_1);
        equipmentSlots[8] = new EquipmentSlot(EquipmentSlotTypes.NECKLACE);

        // Weapon
        equipmentSlots[9] = new EquipmentSlot(EquipmentSlotTypes.WEAPON);
        equipmentSlots[10] = new EquipmentSlot(EquipmentSlotTypes.SHIELD);
        equipmentSlots[11] = new EquipmentSlot(EquipmentSlotTypes.AMMO);
    }

    void swapBagAndEquipmentWindow(PlayerBag playerBag, byte bagIndex, byte equipmentIndex, boolean equipItem) {
        ItemStack bagItemStack = playerBag.getItemStack(bagIndex);
        ItemStack equipmentItemStack = getItemStack(equipmentIndex);

        // Confirming that the equipment is allowed to be switched.
        // If bagItemStack == null then the equipment is being removed.
        if (bagItemStack != null) {
            boolean foundType = false;
            for (ItemStackType itemStackType : equipmentSlots[equipmentIndex].getEquipmentSlot().getAcceptedItemStackTypes()) {
                if (itemStackType == bagItemStack.getItemStackType()) foundType = true;
            }
            if (!foundType) return;
        }

        playerBag.setItemStack(bagIndex, equipmentItemStack);
        equipmentSlots[equipmentIndex].setItemStack(bagItemStack);

        updatePlayerAttributes(bagItemStack, equipmentItemStack, equipItem);
        updateAppearance(equipmentIndex);
    }

    /**
     * Update the {@link Player} with the {@link Attributes} found on equipped item.
     */
    private void updatePlayerAttributes(ItemStack bagItemStack, ItemStack equipItemStack, boolean equipItem) {

        Attributes playerClientAttributes = player.getAttributes();
        Attributes itemStackAttributes = equipItem ? bagItemStack.getAttributes() : equipItemStack.getAttributes();

        println(PRINT_DEBUG);
        println(getClass(), "PC Armor: " + playerClientAttributes.getArmor(),  false,PRINT_DEBUG);
        println(getClass(), "PC Damage: " + playerClientAttributes.getDamage(), false, PRINT_DEBUG);
        println(getClass(), "IS Armor: " + itemStackAttributes.getArmor(),  false,PRINT_DEBUG);
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

        println(getClass(), "PC Armor: " + playerClientAttributes.getArmor(),  false,PRINT_DEBUG);
        println(getClass(), "PC Final Damage: " + playerClientAttributes.getDamage(), false, PRINT_DEBUG);
        println(PRINT_DEBUG);

        // Send attributes packet
        new EntityAttributesUpdatePacketOut(player, player).sendPacket();
    }

    private void updateAppearance(byte equipmentIndex) {
        if (equipmentSlots[equipmentIndex].getItemStack() != null) {
            if (equipmentSlots[equipmentIndex].getItemStack() instanceof WearableItemStack) {
                WearableItemStack newAppearanceStack = (WearableItemStack) equipmentSlots[equipmentIndex].getItemStack();
                if (newAppearanceStack.getItemStackType() == ItemStackType.CHEST) {
                    player.setArmorAppearance(newAppearanceStack.getTextureId());
                } else if (newAppearanceStack.getItemStackType() == ItemStackType.HELM) {
                    player.setHelmAppearance(newAppearanceStack.getTextureId());
                }
            }
        } else {
            if (equipmentSlots[equipmentIndex].getEquipmentSlot().getAcceptedItemStackTypes()[0] == ItemStackType.CHEST) {
                player.setArmorAppearance((short) -1);
            } else if (equipmentSlots[equipmentIndex].getEquipmentSlot().getAcceptedItemStackTypes()[0] == ItemStackType.HELM) {
                player.setHelmAppearance((short) -1);
            }
        }
    }

    ItemStack getItemStack(byte index) {
        return equipmentSlots[index].getItemStack();
    }
}
