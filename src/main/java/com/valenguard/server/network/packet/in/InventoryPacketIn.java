package com.valenguard.server.network.packet.in;

import com.valenguard.server.ValenguardMain;
import com.valenguard.server.game.inventory.*;
import com.valenguard.server.network.shared.*;
import lombok.AllArgsConstructor;

@Opcode(getOpcode = Opcodes.INVENTORY_UPDATE)
public class InventoryPacketIn implements PacketListener<InventoryPacketIn.InventoryActionsPacket> {

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
            byte windowsByte = clientHandler.readByte();
            fromWindow = (byte) (windowsByte >> 4);
            toWindow = (byte) (windowsByte & 0x0F);
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

        PlayerInventoryEvents playerInventoryEvents = ValenguardMain.getInstance().getGameLoop().getPlayerInventoryEvents();
        playerInventoryEvents.addInventoryEvent(new InventoryEvent(packetData.getPlayer(), packetData.fromPosition, packetData.toPosition, windowMovementInfo));

    }

    private boolean doesNotExceedInventoryLimit(InventoryType fromWindow, InventoryType toWindow, InventoryActionsPacket packetData) {

        if (fromWindow == InventoryType.BAG) {
            if (packetData.fromPosition >= PlayerBag.CAPACITY || packetData.fromPosition < 0) return false;
        } else if (fromWindow == InventoryType.EQUIPMENT) {
            if (packetData.fromPosition >= PlayerEquipment.CAPACITY || packetData.fromPosition < 0) return false;
        }

        if (toWindow == InventoryType.BAG) {
            return packetData.toPosition < PlayerBag.CAPACITY && packetData.toPosition >= 0;
        } else if (fromWindow == InventoryType.EQUIPMENT) {
            return packetData.toPosition < PlayerEquipment.CAPACITY && packetData.toPosition >= 0;
        }

        return true;
    }

    private WindowMovementInfo determineWindowMovementInfo(InventoryType fromWindow, InventoryType toWindow) {
        if (fromWindow == InventoryType.EQUIPMENT && toWindow == InventoryType.BAG) {
            return WindowMovementInfo.FROM_EQUIPMENT_TO_BAG;
        } else if (fromWindow == InventoryType.BAG && toWindow == InventoryType.EQUIPMENT) {
            return WindowMovementInfo.FROM_BAG_TO_EQUIPMENT;
        } else if (fromWindow == InventoryType.BAG && toWindow == InventoryType.BAG) {
            return WindowMovementInfo.FROM_BAG_TO_BAG;
        } else if (fromWindow == InventoryType.EQUIPMENT && toWindow == InventoryType.EQUIPMENT) {
            return WindowMovementInfo.FROM_EQUIPMENT_TO_EQUIPMENT;
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
