package com.valenguard.server.game.inventory;

import com.valenguard.server.game.entity.Player;
import com.valenguard.server.game.rpg.Attributes;
import com.valenguard.server.network.packet.out.EntityAttributesUpdatePacketOut;

import static com.valenguard.server.util.Log.println;

public class PlayerEquipment {

    private static final boolean PRINT_DEBUG = true;

    public static final int CAPACITY = 12;

    private final EquipmentSlot[] equipmentSlots = new EquipmentSlot[CAPACITY];

    private Player player;

    public void init(Player player) {
        this.player = player;
        // Main Body
        equipmentSlots[0] = new EquipmentSlot(ItemStackType.HELM);
        equipmentSlots[1] = new EquipmentSlot(ItemStackType.CHEST);
        equipmentSlots[2] = new EquipmentSlot(ItemStackType.BOOTS);
        equipmentSlots[3] = new EquipmentSlot(ItemStackType.CAPE);
        equipmentSlots[4] = new EquipmentSlot(ItemStackType.GLOVES);
        equipmentSlots[5] = new EquipmentSlot(ItemStackType.BELT);

        // Trinket
        equipmentSlots[6] = new EquipmentSlot(ItemStackType.RING);
        equipmentSlots[7] = new EquipmentSlot(ItemStackType.RING);
        equipmentSlots[8] = new EquipmentSlot(ItemStackType.NECKLACE);

        // Weapon
        equipmentSlots[9] = new EquipmentSlot(ItemStackType.SWORD);
        equipmentSlots[10] = new EquipmentSlot(ItemStackType.SHIELD);
        equipmentSlots[11] = new EquipmentSlot(ItemStackType.ARROW);
    }

    public void swapBagAndEquipmentWindow(PlayerBag playerBag, byte bagIndex, byte equipmentIndex, boolean equipItem) {
        ItemStack bagItemStack = playerBag.getItemStack(bagIndex);
        ItemStack equipmentItemStack = getItemStack(equipmentIndex);

        if (equipItem) {
            updatePlayerEquipment(bagItemStack, true);
        } else {
            updatePlayerEquipment(equipmentItemStack, false);
        }

        // Confirming that the equipment is allowed to be switched.
        // If bagItemStack == null then the equipment is being removed.
        if (bagItemStack != null) {
            if (equipmentSlots[equipmentIndex].getItemStackType() != bagItemStack.getItemStackType()) return;
        }

        playerBag.setItemStack(bagIndex, equipmentItemStack);
        equipmentSlots[equipmentIndex].setItemStack(bagItemStack);

        updateAppearance(equipmentIndex);
    }

    /**
     * Update the {@link Player} with the {@link Attributes} found on equipped item.
     *
     * @param itemStack The {@link ItemStack} the player is equipping or removing.
     * @param equipItem True if the player is equipping the {@link ItemStack}, false otherwise.
     */
    private void updatePlayerEquipment(ItemStack itemStack, boolean equipItem) {

        Attributes playerClientAttributes = player.getAttributes();
        Attributes itemStackAttributes = itemStack.getAttributes();

        println(PRINT_DEBUG);
        println(getClass(), "PC Health: " + playerClientAttributes.getHealth(), PRINT_DEBUG);
        println(getClass(), "PC Armor: " + playerClientAttributes.getArmor(), PRINT_DEBUG);
        println(getClass(), "PC Damage: " + playerClientAttributes.getDamage(), PRINT_DEBUG);
        println(getClass(), "IS Health: " + itemStackAttributes.getHealth(), PRINT_DEBUG);
        println(getClass(), "IS Armor: " + itemStackAttributes.getArmor(), PRINT_DEBUG);
        println(getClass(), "IS Damage: " + itemStackAttributes.getDamage(), PRINT_DEBUG);

        // TODO: Instead of manually adding the new values, we should possible loop through all equipped items and get values this way.
        if (equipItem) {
            // Player Equipped an Item. Update attributes!
            playerClientAttributes.setHealth(playerClientAttributes.getHealth() + itemStackAttributes.getHealth());
            playerClientAttributes.setArmor(playerClientAttributes.getArmor() + itemStackAttributes.getArmor());
            playerClientAttributes.setDamage(playerClientAttributes.getDamage() + itemStackAttributes.getDamage());
        } else {
            // Player Unequipped an Item. Update attributes!
            playerClientAttributes.setHealth(playerClientAttributes.getHealth() - itemStackAttributes.getHealth());
            playerClientAttributes.setArmor(playerClientAttributes.getArmor() - itemStackAttributes.getArmor());
            playerClientAttributes.setDamage(playerClientAttributes.getDamage() - itemStackAttributes.getDamage());
        }

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
            if (equipmentSlots[equipmentIndex].getItemStackType() == ItemStackType.CHEST) {
                player.setArmorAppearance((short) -1);
            } else if (equipmentSlots[equipmentIndex].getItemStackType() == ItemStackType.HELM) {
                player.setHelmAppearance((short) -1);
            }
        }
    }

    public ItemStack getItemStack(byte index) {
        return equipmentSlots[index].getItemStack();
    }
}
