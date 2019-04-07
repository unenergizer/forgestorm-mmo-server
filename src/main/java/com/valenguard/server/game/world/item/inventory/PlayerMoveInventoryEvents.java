package com.valenguard.server.game.world.item.inventory;

import com.valenguard.server.game.world.entity.Player;
import com.valenguard.server.network.game.packet.out.InventoryPacketOut;

import java.util.LinkedList;
import java.util.Queue;

import static com.valenguard.server.util.Log.println;

public class PlayerMoveInventoryEvents {

    private static final boolean PRINT_DEBUG = true;

    private final Queue<InventoryEvent> inventoryEvents = new LinkedList<>();

    public void addInventoryEvent(InventoryEvent inventoryEvent) {
        inventoryEvents.add(inventoryEvent);
    }

    public void processInventoryEvents() {
        InventoryEvent inventoryEvent;
        while ((inventoryEvent = inventoryEvents.poll()) != null) {

            if (!doesItemStackExist(inventoryEvent.getInventoryMoveType().getFromWindow(), inventoryEvent)) continue;

            println(PRINT_DEBUG);
            println(getClass(), "MOVE INFO:", false, PRINT_DEBUG);
            println(getClass(), "Type: " + inventoryEvent.getInventoryMoveType().toString(), false, PRINT_DEBUG);
            println(getClass(), "FromIndex: " + inventoryEvent.getFromPositionIndex(), false, PRINT_DEBUG);
            println(getClass(), "ToIndex: " + inventoryEvent.getToPositionIndex(), false, PRINT_DEBUG);

            performWindowTransfer(inventoryEvent);
        }
    }

    private void performWindowTransfer(InventoryEvent inventoryEvent) {
        if (!checkItemStackExist(inventoryEvent)) return;
        Player player = inventoryEvent.getPlayer();
        PlayerBag playerBag = player.getPlayerBag();
        PlayerBank playerBank = player.getPlayerBank();
        PlayerEquipment playerEquipment = player.getPlayerEquipment();

        boolean sendPacket = false;

        switch (inventoryEvent.getInventoryMoveType()) {
            case FROM_BAG_TO_BAG:
                sendPacket = playerBag.performItemStackMove(playerBag, inventoryEvent.getFromPositionIndex(), inventoryEvent.getToPositionIndex());
                break;
            case FROM_BAG_TO_BANK:
                sendPacket = playerBag.performItemStackMove(playerBank, inventoryEvent.getFromPositionIndex(), inventoryEvent.getToPositionIndex());
                break;
            case FROM_BAG_TO_EQUIPMENT:
                sendPacket = playerEquipment.performEquipmentMove(playerBag, inventoryEvent.getFromPositionIndex(), inventoryEvent.getToPositionIndex(), true);
                break;
            case FROM_BANK_TO_BAG:
                sendPacket = playerBank.performItemStackMove(playerBag, inventoryEvent.getFromPositionIndex(), inventoryEvent.getToPositionIndex());
                break;
            case FROM_BANK_TO_BANK:
                sendPacket = playerBank.performItemStackMove(playerBank, inventoryEvent.getFromPositionIndex(), inventoryEvent.getToPositionIndex());
                break;
            case FROM_BANK_TO_EQUIPMENT:
                sendPacket = playerEquipment.performEquipmentMove(playerBank, inventoryEvent.getFromPositionIndex(), inventoryEvent.getToPositionIndex(), true);
                break;
            case FROM_EQUIPMENT_TO_BAG:
                sendPacket = playerEquipment.performEquipmentMove(playerBag, inventoryEvent.getFromPositionIndex(), inventoryEvent.getToPositionIndex(), false);
                break;
            case FROM_EQUIPMENT_TO_BANK:
                sendPacket = playerEquipment.performEquipmentMove(playerBank, inventoryEvent.getFromPositionIndex(), inventoryEvent.getToPositionIndex(), false);
                break;
            case FROM_EQUIPMENT_TO_EQUIPMENT:
                sendPacket = playerEquipment.performEquipmentMove(playerEquipment, inventoryEvent.getFromPositionIndex(), inventoryEvent.getToPositionIndex(), false);
                break;
        }

        if (!sendPacket) {
            println(getClass(), "MOVING ITEMS: false", false, PRINT_DEBUG);
            return;
        }

        println(getClass(), "MOVING ITEMS: true", false, PRINT_DEBUG);

        // Perform client move
        new InventoryPacketOut(player, new InventoryActions(
                inventoryEvent.getInventoryMoveType().getFromWindow(),
                inventoryEvent.getInventoryMoveType().getToWindow(),
                inventoryEvent.getFromPositionIndex(),
                inventoryEvent.getToPositionIndex()
        )).sendPacket();
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
}
