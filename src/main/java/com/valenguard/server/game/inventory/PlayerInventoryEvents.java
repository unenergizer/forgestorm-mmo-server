package com.valenguard.server.game.inventory;

import com.valenguard.server.game.entity.Player;

import java.util.LinkedList;
import java.util.Queue;

public class PlayerInventoryEvents {

    private Queue<InventoryEvent> inventoryEvents = new LinkedList<>();

    public void addInventoryEvent(InventoryEvent inventoryEvent) {
        inventoryEvents.add(inventoryEvent);
    }

    public void processInventoryEvents() {
        InventoryEvent inventoryEvent;
        while ((inventoryEvent = inventoryEvents.poll()) != null) {

            if (!fromItemStackExist(inventoryEvent.getWindowMovementInfo().getFromWindow(), inventoryEvent)) continue;

            if (inventoryEvent.getWindowMovementInfo() == WindowMovementInfo.FROM_BAG_TO_BAG) {
                bagMove(inventoryEvent);
            } else if (inventoryEvent.getWindowMovementInfo() == WindowMovementInfo.FROM_BAG_TO_EQUIPMENT) {
                fromBagToEquipment(inventoryEvent);
            } else if (inventoryEvent.getWindowMovementInfo() == WindowMovementInfo.FROM_EQUIPMENT_TO_BAG) {
                fromEquipmentToBag(inventoryEvent);
            } else if (inventoryEvent.getWindowMovementInfo() == WindowMovementInfo.FROM_EQUIPMENT_TO_EQUIPMENT) {
                fromEquipmentToEquipment(inventoryEvent);
            }
        }
    }

    private boolean fromItemStackExist(InventoryType fromWindowType, InventoryEvent inventoryEvent) {

        Player player = inventoryEvent.getPlayer();
        byte fromPosition = inventoryEvent.getFromPosition();

        if (fromWindowType == InventoryType.BAG_1 && player.getPlayerBag().getItemStack(fromPosition) == null) {
            return false;
        } else
            return fromWindowType != InventoryType.EQUIPMENT || player.getPlayerEquipment().getItemStack(fromPosition) != null;

    }

    private void bagMove(InventoryEvent inventoryEvent) {
        inventoryEvent.getPlayer().getPlayerBag().moveItemStacks(inventoryEvent.getFromPosition(), inventoryEvent.getToPosition());
    }

    private void fromBagToEquipment(InventoryEvent inventoryEvent) {
        Player player = inventoryEvent.getPlayer();
        player.getPlayerEquipment().swapBagAndEquipmentWindow(player.getPlayerBag(), inventoryEvent.getFromPosition(), inventoryEvent.getToPosition(), true);
    }

    private void fromEquipmentToBag(InventoryEvent inventoryEvent) {
        Player player = inventoryEvent.getPlayer();
        player.getPlayerEquipment().swapBagAndEquipmentWindow(player.getPlayerBag(), inventoryEvent.getToPosition(), inventoryEvent.getFromPosition(), false);
    }

    private void fromEquipmentToEquipment(InventoryEvent inventoryEvent) {
        System.out.println("FROM EQUIPMENT TO EQUIPMENT");
    }
}
