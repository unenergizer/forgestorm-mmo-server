package com.valenguard.server.game.inventory;

import com.valenguard.server.game.entity.Player;
import com.valenguard.server.network.game.packet.out.InventoryPacketOut;

import java.util.LinkedList;
import java.util.Queue;

public class PlayerMoveInventoryEvents {

    private final Queue<InventoryEvent> inventoryEvents = new LinkedList<>();

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
        inventoryEvent.getPlayer().moveItemStackInBag(inventoryEvent.getFromPosition(), inventoryEvent.getToPosition());
    }

    private void fromBagToEquipment(InventoryEvent inventoryEvent) {
        Player player = inventoryEvent.getPlayer();

        if (!player.getPlayerEquipment().swapBagAndEquipmentWindow(player.getPlayerBag(), inventoryEvent.getFromPosition(), inventoryEvent.getToPosition(), true)) {
            return;
        }

        new InventoryPacketOut(player, new InventoryActions(
                InventoryActions.MOVE,
                InventoryType.BAG_1,
                InventoryType.EQUIPMENT,
                inventoryEvent.getFromPosition(),
                inventoryEvent.getToPosition()
        )).sendPacket();
    }

    private void fromEquipmentToBag(InventoryEvent inventoryEvent) {
        Player player = inventoryEvent.getPlayer();

        if (!player.getPlayerEquipment().swapBagAndEquipmentWindow(player.getPlayerBag(), inventoryEvent.getToPosition(), inventoryEvent.getFromPosition(), false)) {
            return;
        }

        // 1, 2, 3, 4
        // ^...

        // Problems
        // 1. At the moment we don't respond to failed movements
        // 2. Player moves an item to a new inventory spot then from that new inventory
        // spot they move the item again before receiving a response <- How do we know the state of the inventory to resolve this?


        new InventoryPacketOut(player, new InventoryActions(
                InventoryActions.MOVE,
                InventoryType.EQUIPMENT,
                InventoryType.BAG_1,
                inventoryEvent.getFromPosition(),
                inventoryEvent.getToPosition()
        )).sendPacket();
    }

    private void fromEquipmentToEquipment(InventoryEvent inventoryEvent) {
        System.out.println("FROM EQUIPMENT TO EQUIPMENT");
    }
}
