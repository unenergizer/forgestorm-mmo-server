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
    public void onEvent(MovePacket packetData) {

        MoveDirection direction = MoveDirection.getDirection(packetData.directionalByte);

        if (direction == null || direction == MoveDirection.NONE) return;

        ValenguardMain.getInstance().getGameLoop().getUpdateMovements().performMove(packetData.getPlayer(), direction);
    }

    @AllArgsConstructor
    class MovePacket extends PacketData {
        byte directionalByte;
    }
}
