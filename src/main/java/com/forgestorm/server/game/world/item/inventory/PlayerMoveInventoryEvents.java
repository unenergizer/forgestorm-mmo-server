package com.forgestorm.server.game.world.item.inventory;

import com.forgestorm.server.game.world.entity.Player;
import com.forgestorm.server.network.game.packet.out.InventoryPacketOutOut;
import com.forgestorm.shared.game.world.item.inventory.InventoryMoveType;
import com.forgestorm.shared.game.world.item.inventory.InventoryType;

import java.util.LinkedList;
import java.util.Queue;

import static com.forgestorm.server.util.Log.println;

public class PlayerMoveInventoryEvents {

    private static final boolean PRINT_DEBUG = false;

    private final Queue<InventoryEvent> inventoryEvents = new LinkedList<>();

    public void addInventoryEvent(InventoryEvent inventoryEvent) {
        inventoryEvents.add(inventoryEvent);
    }

    public void processInventoryEvents() {
        InventoryEvent inventoryEvent;
        while ((inventoryEvent = inventoryEvents.poll()) != null) {
            InventoryMoveType inventoryMoveType = inventoryEvent.getInventoryMoveType();
            boolean isBankMove = inventoryMoveType.getToWindow() == InventoryType.BANK
                    || inventoryMoveType.getFromWindow() == InventoryType.BANK;

            if (!doesItemStackExist(inventoryEvent.getInventoryMoveType().getFromWindow(), inventoryEvent) ||
                    /* The player is trying to move item in their bank but their bank is not open */
                    (isBankMove && !inventoryEvent.getPlayer().isBankOpen())) {

                // Sending back a response to the client telling them not to move anything.
                new InventoryPacketOutOut(inventoryEvent.getPlayer(), new InventoryActions()
                        .move(
                                inventoryEvent.getInventoryMoveType().getFromWindow(),
                                inventoryEvent.getInventoryMoveType().getToWindow(),
                                inventoryEvent.getFromPositionIndex(),
                                inventoryEvent.getFromPositionIndex()
                        )).sendPacket();

                continue;
            }

            println(PRINT_DEBUG);
            println(getClass(), "MOVE INFO IN:", false, PRINT_DEBUG);
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
        PlayerHotBar playerHotBar = player.getPlayerHotBar();

        boolean sendPacket = false;

        switch (inventoryEvent.getInventoryMoveType()) {

            // Bag
            case FROM_BAG_TO_BAG:
                sendPacket = playerBag.performItemStackMove(playerBag, inventoryEvent.getFromPositionIndex(), inventoryEvent.getToPositionIndex());
                break;
            case FROM_BAG_TO_BANK:
                sendPacket = playerBag.performItemStackMove(playerBank, inventoryEvent.getFromPositionIndex(), inventoryEvent.getToPositionIndex());
                break;
            case FROM_BAG_TO_EQUIPMENT:
                sendPacket = playerEquipment.performItemStackMoveSpecial(playerBag, inventoryEvent.getFromPositionIndex(), inventoryEvent.getToPositionIndex());
                break;
            case FROM_BAG_TO_HOT_BAR:
                sendPacket = playerBag.performItemStackMove(playerHotBar, inventoryEvent.getFromPositionIndex(), inventoryEvent.getToPositionIndex());
                break;

            // Bank
            case FROM_BANK_TO_BAG:
                sendPacket = playerBank.performItemStackMove(playerBag, inventoryEvent.getFromPositionIndex(), inventoryEvent.getToPositionIndex());
                break;
            case FROM_BANK_TO_BANK:
                sendPacket = playerBank.performItemStackMove(playerBank, inventoryEvent.getFromPositionIndex(), inventoryEvent.getToPositionIndex());
                break;
            case FROM_BANK_TO_EQUIPMENT:
                sendPacket = playerEquipment.performItemStackMoveSpecial(playerBank, inventoryEvent.getFromPositionIndex(), inventoryEvent.getToPositionIndex());
                break;
            case FROM_BANK_TO_HOT_BAR:
                sendPacket = playerBank.performItemStackMove(playerHotBar, inventoryEvent.getFromPositionIndex(), inventoryEvent.getToPositionIndex());
                break;

            // Equipment
            case FROM_EQUIPMENT_TO_BAG:
                sendPacket = playerEquipment.performItemStackMove(playerBag, inventoryEvent.getFromPositionIndex(), inventoryEvent.getToPositionIndex());
                break;
            case FROM_EQUIPMENT_TO_BANK:
                sendPacket = playerEquipment.performItemStackMove(playerBank, inventoryEvent.getFromPositionIndex(), inventoryEvent.getToPositionIndex());
                break;
            case FROM_EQUIPMENT_TO_EQUIPMENT:
                sendPacket = playerEquipment.performItemStackMove(playerEquipment, inventoryEvent.getFromPositionIndex(), inventoryEvent.getToPositionIndex());
                break;
            case FROM_EQUIPMENT_TO_HOT_BAR:
                sendPacket = playerEquipment.performItemStackMove(playerHotBar, inventoryEvent.getFromPositionIndex(), inventoryEvent.getToPositionIndex());
                break;

            // HotBar
            case FROM_HOT_BAR_TO_BAG:
                sendPacket = playerHotBar.performItemStackMove(playerBag, inventoryEvent.getFromPositionIndex(), inventoryEvent.getToPositionIndex());
                break;
            case FROM_HOT_BAR_TO_BANK:
                sendPacket = playerHotBar.performItemStackMove(playerBank, inventoryEvent.getFromPositionIndex(), inventoryEvent.getToPositionIndex());
                break;
            case FROM_HOT_BAR_TO_EQUIPMENT:
                sendPacket = playerEquipment.performItemStackMoveSpecial(playerHotBar, inventoryEvent.getFromPositionIndex(), inventoryEvent.getToPositionIndex());
                break;
            case FROM_HOT_BAR_TO_HOT_BAR:
                sendPacket = playerHotBar.performItemStackMove(playerHotBar, inventoryEvent.getFromPositionIndex(), inventoryEvent.getToPositionIndex());
                break;
        }

        // TODO: Shouldn't we be sending back a fail response?
        if (!sendPacket) {
            println(getClass(), "MOVING ITEMS: false", false, PRINT_DEBUG);
            return;
        }

        println(getClass(), "MOVING ITEMS: true", false, PRINT_DEBUG);

        println(getClass(), "MOVE INFO OUT:", false, PRINT_DEBUG);
        println(getClass(), "Type: " + inventoryEvent.getInventoryMoveType().toString(), false, PRINT_DEBUG);
        println(getClass(), "FromIndex: " + inventoryEvent.getFromPositionIndex(), false, PRINT_DEBUG);
        println(getClass(), "ToIndex: " + inventoryEvent.getToPositionIndex(), false, PRINT_DEBUG);

        // Perform client move
        new InventoryPacketOutOut(player, new InventoryActions().move(
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
            case HOT_BAR:
                return player.getPlayerHotBar().getItemStack(fromPosition) != null;
            default:
                return false;
        }
    }

    private boolean doesItemStackExist(InventoryType fromWindowType, InventoryEvent inventoryEvent) {

        Player player = inventoryEvent.getPlayer();
        byte fromPosition = inventoryEvent.getFromPositionIndex();

        AbstractInventory abstractInventory = null;
        if (fromWindowType == InventoryType.BAG_1) {
            abstractInventory = player.getPlayerBag();
        } else if (fromWindowType == InventoryType.EQUIPMENT) {
            abstractInventory = player.getPlayerEquipment();
        } else if (fromWindowType == InventoryType.BANK) {
            abstractInventory = player.getPlayerBank();
        } else if (fromWindowType == InventoryType.HOT_BAR) {
            abstractInventory = player.getPlayerHotBar();
        }

        if (abstractInventory == null) {
            throw new RuntimeException("Did not implement stack existing checking for inventory.");
        }

        return abstractInventory.getItemStack(fromPosition) != null;
    }
}
