package com.valenguard.server.game.inventory;

public class PlayerEquipment {

    public static final int CAPACITY = 12;

    private EquipmentSlot[] equipmentSlots = new EquipmentSlot[CAPACITY];

    public void init() {
        equipmentSlots[0] = new EquipmentSlot(ItemStackType.HELM);
        equipmentSlots[1] = new EquipmentSlot(ItemStackType.ARROW);
        equipmentSlots[2] = new EquipmentSlot(ItemStackType.NECKLACE);
        equipmentSlots[3] = new EquipmentSlot(ItemStackType.CAPE);
        equipmentSlots[4] = new EquipmentSlot(ItemStackType.RINGS);
        equipmentSlots[5] = new EquipmentSlot(ItemStackType.CHEST);
        equipmentSlots[6] = new EquipmentSlot(ItemStackType.GLOVES);
        equipmentSlots[7] = new EquipmentSlot(ItemStackType.RINGS);
        equipmentSlots[8] = new EquipmentSlot(ItemStackType.BELT);
        equipmentSlots[9] = new EquipmentSlot(ItemStackType.BOOTS);
        equipmentSlots[10] = new EquipmentSlot(ItemStackType.SWORD);
        equipmentSlots[11] = new EquipmentSlot(ItemStackType.SHIELD);
    }

    public void swapBagAndEquipmentWindow(PlayerBag playerBag, byte bagIndex, byte equipmentIndex) {
        ItemStack bagItemStack = playerBag.getItemStack(bagIndex);
        ItemStack equipmentItemStack = getItemStack(equipmentIndex);

        // Confirming that the equipment is allowed to be switched.
        // If bagItemStack == null then the equipment is being removed.
        if (bagItemStack != null) {
            if (equipmentSlots[equipmentIndex].getItemStackType() != bagItemStack.getItemStackType()) return;
        }

        playerBag.setItemStack(bagIndex, equipmentItemStack);
        equipmentSlots[equipmentIndex].setItemStack(bagItemStack);

        System.out.println("PLAYER_BAG[" + bagIndex + "] = " + playerBag.getItemStack(bagIndex));
        System.out.println("EQUIPMENT[" + equipmentIndex + "] = " + equipmentSlots[equipmentIndex].getItemStack());
    }

    public ItemStack getItemStack(byte index) {
        return equipmentSlots[index].getItemStack();
    }
}
