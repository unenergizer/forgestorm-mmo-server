package com.valenguard.server.network.packet.in;

import com.valenguard.server.ValenguardMain;
import com.valenguard.server.game.maps.MoveDirection;
import com.valenguard.server.network.shared.*;
import lombok.AllArgsConstructor;

@Opcode(getOpcode = Opcodes.MOVE_REQUEST)
public class PlayerMove implements PacketListener<PlayerMove.MovePacket> {

    @Override
    public PacketData decodePacket(ClientHandler clientHandler) {
        return new MovePacket(clientHandler.readByte());
    }

    @Override
    public boolean sanitizePacket(MovePacket packetData) {
        MoveDirection direction = MoveDirection.getDirection(packetData.directionalByte);
        return !(direction == null || direction == MoveDirection.NONE);
    }

    @Override
    public void onEvent(MovePacket packetData) {
        ValenguardMain.getInstance().getGameLoop().getUpdateMovements().performMove(packetData.getPlayer(), MoveDirection.getDirection(packetData.directionalByte));
    }

    @AllArgsConstructor
    class MovePacket extends PacketData {
        byte directionalByte;
    }
}
