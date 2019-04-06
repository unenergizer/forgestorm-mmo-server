package com.valenguard.server.game.world.item.inventory;

import com.valenguard.server.game.world.entity.Player;
import com.valenguard.server.game.world.item.ItemStack;
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

            if (!doesItemStackExist(inventoryEvent.getInventoryMoveType().getFromWindow(), inventoryEvent)) continue;

            switch (inventoryEvent.getInventoryMoveType()) {
                case FROM_BAG_TO_BAG:
                    fromBagToBag(inventoryEvent);
                    break;
                case FROM_BAG_TO_BANK:
                    break;
                case FROM_BAG_TO_EQUIPMENT:
                    fromBagToEquipment(inventoryEvent);
                    break;
                case FROM_BANK_TO_BAG:
                    break;
                case FROM_BANK_TO_BANK:
                    break;
                case FROM_BANK_TO_EQUIPMENT:
                    break;
                case FROM_EQUIPMENT_TO_BAG:
                    fromEquipmentToBag(inventoryEvent);
                    break;
                case FROM_EQUIPMENT_TO_BANK:
                    break;
                case FROM_EQUIPMENT_TO_EQUIPMENT:
                    fromEquipmentToEquipment();
                    break;
            }
        }
    }

    private void perfromWindowTransfer(InventoryEvent inventoryEvent) {
        if (!checkItemStackExist(inventoryEvent)) return;
        Player player = inventoryEvent.getPlayer();
        InventorySlot[] playerBag = player.getPlayerBag().getInventorySlotArray();
        InventorySlot[] playerBank = player.getPlayerBank().getInventorySlotArray();
        InventorySlot[] playerEquipment = player.getPlayerEquipment().getInventorySlotArray();


        // Perform server move
        InventorySlot[] fromSlot;
        InventorySlot[] toSlot;
        boolean windowTransfer = false;
        boolean equipmentCheck = false;

        switch (inventoryEvent.getInventoryMoveType()) {
            case FROM_BAG_TO_BAG:
                player.getPlayerBag().performInnerWindowMove(inventoryEvent.getFromPositionIndex(), inventoryEvent.getToPositionIndex());
                break;
            case FROM_BAG_TO_BANK:
                fromSlot = playerBag;
                toSlot = playerBank;
                windowTransfer = true;
                break;
            case FROM_BAG_TO_EQUIPMENT:
                fromSlot = playerBag;
                toSlot = playerEquipment;
                windowTransfer = true;
                equipmentCheck = true;
                break;
            case FROM_BANK_TO_BAG:
                fromSlot = playerBank;
                toSlot = playerBag;
                windowTransfer = true;
                break;
            case FROM_BANK_TO_BANK:
                player.getPlayerBank().performInnerWindowMove(inventoryEvent.getFromPositionIndex(), inventoryEvent.getToPositionIndex());
                break;
            case FROM_BANK_TO_EQUIPMENT:
                fromSlot = playerBank;
                toSlot = playerEquipment;
                windowTransfer = true;
                equipmentCheck = true;
                break;
            case FROM_EQUIPMENT_TO_BAG:
                fromSlot = playerEquipment;
                toSlot = playerBag;
                windowTransfer = true;
                equipmentCheck = true;
                break;
            case FROM_EQUIPMENT_TO_BANK:
                fromSlot = playerEquipment;
                toSlot = playerBank;
                windowTransfer = true;
                equipmentCheck = true;
                break;
            case FROM_EQUIPMENT_TO_EQUIPMENT:
                player.getPlayerEquipment().performInnerWindowMove(inventoryEvent.getFromPositionIndex(), inventoryEvent.getToPositionIndex());
                break;
        }

        // Perform client move
        new InventoryPacketOut(player, new InventoryActions(
                inventoryEvent.getInventoryMoveType().getFromWindow(),
                inventoryEvent.getInventoryMoveType().getToWindow(),
                inventoryEvent.getFromPositionIndex(),
                inventoryEvent.getToPositionIndex()
        )).sendPacket();
    }

    private void performWindowTransfer(InventoryEvent inventoryEvent, InventorySlot[] fromSlot, byte fromPositionIndex, InventorySlot[] toSlot,  byte toPositionIndex) {
        ItemStack fromItemStack = fromSlot[fromPositionIndex].getItemStack();
        ItemStack toItemStack = toSlot[toPositionIndex].getItemStack();
//        inventorySlotArray[toPositionIndex].setItemStack(fromItemStack);
//        inventorySlotArray[fromPositionIndex].setItemStack(toItemStack);

        new InventoryPacketOut(inventoryEvent.getPlayer(), new InventoryActions(
                inventoryEvent.getInventoryMoveType().getFromWindow(),
                inventoryEvent.getInventoryMoveType().getToWindow(),
                fromPositionIndex,
                toPositionIndex)).sendPacket();
    }

    private boolean checkItemStackExist(InventoryEvent inventoryEvent) {
        Player player = inventoryEvent.getPlayer();
        byte fromPosition = inventoryEvent.getFromPositionIndex();

        switch (inventoryEvent.getInventoryMoveType().getFromWindow()) {
            case EQUIPMENT:
                return player.getPlayerEquipment().getItemStack(fromPosition) != null;
            case BAG_1:
            case BAG_2:
            case BAG_3:
            case BAG_4:
                return player.getPlayerBag().getItemStack(fromPosition) != null;
            case BANK:
                return player.getPlayerBank().getItemStack(fromPosition) != null;
            default:
                return false;
        }
    }

    private boolean doesItemStackExist(InventoryType fromWindowType, InventoryEvent inventoryEvent) {

        Player player = inventoryEvent.getPlayer();
        byte fromPosition = inventoryEvent.getFromPositionIndex();

        if (fromWindowType == InventoryType.BAG_1 && player.getPlayerBag().getItemStack(fromPosition) == null) {
            return false;
        } else
            return fromWindowType != InventoryType.EQUIPMENT || player.getPlayerEquipment().getItemStack(fromPosition) != null;

    }

    private void fromBagToBag(InventoryEvent inventoryEvent) {
        inventoryEvent.getPlayer().getPlayerBag().performInnerWindowMove(inventoryEvent.getFromPositionIndex(), inventoryEvent.getToPositionIndex());
    }

    private void fromBagToEquipment(InventoryEvent inventoryEvent) {
        Player player = inventoryEvent.getPlayer();

        if (!player.getPlayerEquipment().swapBagAndEquipmentWindow(player.getPlayerBag(), inventoryEvent.getFromPositionIndex(), inventoryEvent.getToPositionIndex(), true)) {
            return;
        }

        new InventoryPacketOut(player, new InventoryActions(
                InventoryType.BAG_1,
                InventoryType.EQUIPMENT,
                inventoryEvent.getFromPositionIndex(),
                inventoryEvent.getToPositionIndex()
        )).sendPacket();
    }

    private void fromEquipmentToBag(InventoryEvent inventoryEvent) {
        Player player = inventoryEvent.getPlayer();

        if (!player.getPlayerEquipment().swapBagAndEquipmentWindow(player.getPlayerBag(), inventoryEvent.getToPositionIndex(), inventoryEvent.getFromPositionIndex(), false)) {
            return;
        }

        new InventoryPacketOut(player, new InventoryActions(
                InventoryType.EQUIPMENT,
                InventoryType.BAG_1,
                inventoryEvent.getFromPositionIndex(),
                inventoryEvent.getToPositionIndex()
        )).sendPacket();
    }

    private void fromEquipmentToEquipment() {
        // TODO: Define functionality here
        System.out.println("FROM EQUIPMENT TO EQUIPMENT");
    }
}
