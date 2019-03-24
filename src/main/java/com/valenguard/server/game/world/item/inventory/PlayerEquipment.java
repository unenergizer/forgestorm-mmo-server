package com.valenguard.server.game.world.item.inventory;

import com.valenguard.server.Server;
import com.valenguard.server.game.rpg.Attributes;
import com.valenguard.server.game.world.entity.Appearance;
import com.valenguard.server.game.world.entity.Player;
import com.valenguard.server.game.world.item.ItemStack;
import com.valenguard.server.game.world.item.ItemStackType;
import com.valenguard.server.game.world.item.WearableItemStack;
import com.valenguard.server.network.game.packet.out.EntityAppearancePacketOut;
import com.valenguard.server.network.game.packet.out.EntityAttributesUpdatePacketOut;
import lombok.Getter;

import static com.valenguard.server.util.Log.println;
import static java.util.Objects.requireNonNull;

public class PlayerEquipment {

    private static final boolean PRINT_DEBUG = false;

    @Getter
    private final InventorySlot[] equipmentSlots;
    private final Player player;

    public PlayerEquipment(final Player player) {
        this.player = player;

        // Setup equipment slots
        equipmentSlots = new InventorySlot[InventoryConstants.EQUIPMENT_SIZE];
        for (byte slotIndex = 0; slotIndex < equipmentSlots.length; slotIndex++) {
            equipmentSlots[slotIndex] = new InventorySlot(slotIndex);
        }
    }

    public void setEquipmentSlot(byte equipmentIndex, ItemStack itemStack) {
        equipmentSlots[equipmentIndex].setItemStack(itemStack);
        updatePlayerAttributes(itemStack, null, true, false);
        updateAppearance(equipmentIndex, false);
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

        playerBag.setItemStack(bagIndex, equipmentItemStack, false); // TODO: false or true?
        equipmentSlots[equipmentIndex].setItemStack(bagItemStack);

        updatePlayerAttributes(bagItemStack, equipmentItemStack, equipItem, true);
        updateAppearance(equipmentIndex, true);

        return true;
    }

    /**
     * Update the {@link Player} with the {@link Attributes} found on equipped item.
     */
    private void updatePlayerAttributes(ItemStack bagItemStack, ItemStack equipItemStack, boolean equipItem, boolean sendAttributePacket) {

        Attributes playerClientAttributes = player.getAttributes();
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
        if (sendAttributePacket) new EntityAttributesUpdatePacketOut(player, player).sendPacket();
    }

    private void updateAppearance(byte slotIndex, boolean sendPacket) {
        InventorySlot inventorySlot = equipmentSlots[slotIndex];
        ItemStackType acceptedItemStackType = requireNonNull(getAcceptedItemStackTypes(slotIndex))[0];

        if (inventorySlot.getItemStack() != null) {
            if (inventorySlot.getItemStack() instanceof WearableItemStack) {
                WearableItemStack newAppearanceStack = (WearableItemStack) inventorySlot.getItemStack();
                if (newAppearanceStack.getItemStackType() == ItemStackType.CHEST) {
                    setArmorAppearance(newAppearanceStack.getTextureId(), sendPacket);
                } else if (newAppearanceStack.getItemStackType() == ItemStackType.HELM) {
                    setHelmAppearance(newAppearanceStack.getTextureId(), sendPacket);
                }
            }
        } else {
            if (acceptedItemStackType == ItemStackType.CHEST) {
                setArmorAppearance((short) -1, sendPacket);
            } else if (acceptedItemStackType == ItemStackType.HELM) {
                setHelmAppearance((short) -1, sendPacket);
            }
        }
    }

    private ItemStackType[] getAcceptedItemStackTypes(byte slotIndex) {
        for (EquipmentSlotTypes equipmentSlotTypes : EquipmentSlotTypes.values()) {
            if (equipmentSlotTypes.getSlotIndex() == slotIndex) return equipmentSlotTypes.getAcceptedItemStackTypes();
        }
        return null;
    }


    private void setHelmAppearance(short helmTextureId, boolean sendPacket) {
        player.getAppearance().getTextureIds()[Appearance.HELM] = helmTextureId;
        if (sendPacket) {
            Server.getInstance().getGameManager().sendToAllButPlayer(player, clientHandler ->
                    new EntityAppearancePacketOut(clientHandler.getPlayer(), player, EntityAppearancePacketOut.HELM_INDEX).sendPacket());
        }
    }

    private void setArmorAppearance(short armorTextureId, boolean sendPacket) {
        player.getAppearance().getTextureIds()[Appearance.ARMOR] = armorTextureId;
        if (sendPacket) {
            Server.getInstance().getGameManager().sendToAllButPlayer(player, clientHandler ->
                    new EntityAppearancePacketOut(clientHandler.getPlayer(), player, EntityAppearancePacketOut.ARMOR_INDEX).sendPacket());
        }
    }

    ItemStack getItemStack(byte index) {
        return equipmentSlots[index].getItemStack();
    }
}
