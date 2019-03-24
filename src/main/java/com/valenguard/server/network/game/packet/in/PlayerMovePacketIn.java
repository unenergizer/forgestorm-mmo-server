package com.valenguard.server.network.game.packet.in;

import com.valenguard.server.ValenguardMain;
import com.valenguard.server.game.maps.Location;
import com.valenguard.server.game.task.UpdateMovements;
import com.valenguard.server.network.game.shared.*;
import lombok.AllArgsConstructor;

@Opcode(getOpcode = Opcodes.MOVE_REQUEST)
public class PlayerMovePacketIn implements PacketListener<PlayerMovePacketIn.MovePacket> {

    @Override
    public PacketData decodePacket(ClientHandler clientHandler) {
        final short x = clientHandler.readShort();
        final short y = clientHandler.readShort();
        return new MovePacket(x, y);
    }

    @Override
    public boolean sanitizePacket(MovePacket packetData) {
        return true;
    }

    @Override
    public void onEvent(MovePacket packetData) {
        UpdateMovements updateMovements = ValenguardMain.getInstance().getGameLoop().getUpdateMovements();
        Location location = new Location(packetData.getPlayer().getMapName(), packetData.x, packetData.y);

        if (!updateMovements.preMovementChecks(packetData.getPlayer(), location)) return;

        updateMovements.performPlayerMove(packetData.getPlayer(), location);
    }

    @AllArgsConstructor
    class MovePacket extends PacketData {
        short x;
        short y;
    }
}
