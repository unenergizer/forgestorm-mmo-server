package com.valenguard.server.network.game.packet.in;

import com.valenguard.server.game.world.item.inventory.BankActions;
import com.valenguard.server.network.game.shared.*;
import lombok.AllArgsConstructor;

@Opcode(getOpcode = Opcodes.BANK_MANAGEMENT)
public class BankManagePacketIn implements PacketListener<BankManagePacketIn.BankManagePacket> {

    @Override
    public PacketData decodePacket(ClientHandler clientHandler) {
        return new BankManagePacket(BankActions.getType(clientHandler.readByte()));
    }

    @Override
    public boolean sanitizePacket(BankManagePacket packetData) {
        return true;
    }

    @Override
    public void onEvent(BankManagePacket packetData) {
        switch (packetData.bankAction) {
            case PLAYER_REQUEST_OPEN:
                break;
            case PLAYER_REQUEST_CLOSE:
                break;
        }
    }

    @AllArgsConstructor
    class BankManagePacket extends PacketData {
        private BankActions bankAction;
    }
}