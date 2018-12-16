package com.valenguard.server.network.packet.in;

import com.valenguard.server.game.inventory.InventoryActions;
import com.valenguard.server.network.shared.*;
import lombok.AllArgsConstructor;

@Opcode(getOpcode = Opcodes.INVENTORY_UPDATE)
public class InventoryUpdate implements PacketListener<InventoryUpdate.InventoryActionsPacket> {

    @Override
    public PacketData decodePacket(ClientHandler clientHandler) {
        byte inventoryAction = clientHandler.readByte();
        byte clickedPosition = clientHandler.readByte();
        byte toPosition = 0x00;
        // todo: add enum or something
        if (inventoryAction == InventoryActions.MOVE) toPosition = clientHandler.readByte();
        return new InventoryActionsPacket(inventoryAction, clickedPosition, toPosition);
    }

    @Override
    public boolean sanitizePacket(InventoryActionsPacket packetData) {

        return packetData.inventoryAction <= 2;
    }

    @Override
    public void onEvent(InventoryActionsPacket packetData) {

        if (packetData.inventoryAction == InventoryActions.MOVE) {
            packetData.getPlayer().getPlayerInventory().moveItem(packetData.clickedPosition, packetData.toPosition);
        }

    }

    @AllArgsConstructor
    class InventoryActionsPacket extends PacketData {
        private byte inventoryAction;
        private byte clickedPosition;
        private byte toPosition;
    }
}
