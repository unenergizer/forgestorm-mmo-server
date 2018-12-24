package com.valenguard.server.network.packet.in;

import com.valenguard.server.game.inventory.InventoryActions;
import com.valenguard.server.game.inventory.InventoryType;
import com.valenguard.server.game.inventory.PlayerInventory;
import com.valenguard.server.network.shared.*;
import lombok.AllArgsConstructor;

@Opcode(getOpcode = Opcodes.INVENTORY_UPDATE)
public class IncomingInventoryUpdate implements PacketListener<IncomingInventoryUpdate.InventoryActionsPacket> {

    private enum WindowMovementInfo {
        FROM_BAG_TO_BAG,
        FROM_BAG_TO_CHARACTER,
        FROM_CHARACTER_TO_BAG;

        private InventoryType getFromWindow() {
            switch (this) {
                case FROM_BAG_TO_BAG:
                    return InventoryType.BAG;
                case FROM_BAG_TO_CHARACTER:
                    return InventoryType.BAG;
                case FROM_CHARACTER_TO_BAG:
                    return InventoryType.CHARACTER;
            }
            throw new RuntimeException("Must implement all cases.");
        }

        private InventoryType getToWindow() {
            switch (this) {
                case FROM_BAG_TO_BAG:
                    return InventoryType.BAG;
                case FROM_BAG_TO_CHARACTER:
                    return InventoryType.CHARACTER;
                case FROM_CHARACTER_TO_BAG:
                    return InventoryType.BAG;
            }
            throw new RuntimeException("Must implement all cases.");
        }
    }

    @Override
    public PacketData decodePacket(ClientHandler clientHandler) {
        byte inventoryAction = clientHandler.readByte();
        byte fromPosition = -1;
        byte toPosition = -1;
        byte fromWindow = -1;
        byte toWindow = -1;

        System.out.println("Decoding inventory packet");

        if (inventoryAction == InventoryActions.MOVE) {
            fromPosition = clientHandler.readByte();
            toPosition = clientHandler.readByte();
            fromWindow = clientHandler.readByte();
            toWindow = clientHandler.readByte();
        }

        return new InventoryActionsPacket(inventoryAction, fromPosition, toPosition, fromWindow, toWindow);
    }

    @Override
    public boolean sanitizePacket(InventoryActionsPacket packetData) {
        // Making sure they are sending correct window types.
        if (packetData.toWindow >= InventoryType.values().length || packetData.fromWindow >= InventoryType.values().length) {
            return false;
        }

        // TODO this should be cleaner
        return packetData.inventoryAction <= 2;
    }

    @Override
    public void onEvent(InventoryActionsPacket packetData) {

        if (packetData.inventoryAction == InventoryActions.MOVE) {
            moveItemStack(packetData);
        }

    }

    private void moveItemStack(InventoryActionsPacket packetData) {
        InventoryType fromWindow = InventoryType.values()[packetData.fromWindow];
        InventoryType toWindow = InventoryType.values()[packetData.toWindow];

        if (!doesNotExceedInventoryLimit(fromWindow, toWindow, packetData)) return;

        WindowMovementInfo windowMovementInfo = determineWindowMovementInfo(fromWindow, toWindow);

        if (windowMovementInfo == WindowMovementInfo.FROM_BAG_TO_BAG) {
            packetData.getPlayer().getPlayerInventory().moveItem(packetData.fromPosition, packetData.toPosition);
        }

    }

    private boolean doesNotExceedInventoryLimit(InventoryType fromWindow, InventoryType toWindow, InventoryActionsPacket packetData) {

        if (fromWindow == InventoryType.BAG) {

            if (packetData.fromPosition >= PlayerInventory.CAPACITY || packetData.fromPosition < 0) return false;

        } else if (fromWindow == InventoryType.CHARACTER) {

            // TODO need to create a character window and determine capacity limits

        }

        if (toWindow == InventoryType.BAG) {

            return packetData.toPosition < PlayerInventory.CAPACITY && packetData.toPosition >= 0;

        } else if (fromWindow == InventoryType.CHARACTER) {

            // TODO need to create a character window and determine capacity limits

        }

        return true;
    }

    private WindowMovementInfo determineWindowMovementInfo(InventoryType fromWindow, InventoryType toWindow) {
        if (fromWindow == InventoryType.CHARACTER && toWindow == InventoryType.BAG) {
            return WindowMovementInfo.FROM_CHARACTER_TO_BAG;
        } else if (fromWindow == InventoryType.BAG && toWindow == InventoryType.CHARACTER) {
            return WindowMovementInfo.FROM_BAG_TO_CHARACTER;
        } else if (fromWindow == InventoryType.BAG && toWindow == InventoryType.BAG) {
            return WindowMovementInfo.FROM_BAG_TO_BAG;
        }
        throw new RuntimeException("The sanitization should have already checked for this.");
    }

    @AllArgsConstructor
    class InventoryActionsPacket extends PacketData {
        private byte inventoryAction;
        private byte fromPosition;
        private byte toPosition;
        private byte fromWindow;
        private byte toWindow;
    }
}
