package com.valenguard.server.network.game.packet.in;

import com.valenguard.server.ServerMain;
import com.valenguard.server.game.world.entity.Player;
import com.valenguard.server.game.world.maps.Location;
import com.valenguard.server.game.world.task.MovementUpdateTask;
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
        Player player = packetData.getClientHandler().getPlayer();

        MovementUpdateTask movementUpdateTask = ServerMain.getInstance().getGameLoop().getMovementUpdateTask();
        Location location = new Location(player.getMapName(), packetData.x, packetData.y);

        if (!movementUpdateTask.preMovementChecks(player, location)) return;

        movementUpdateTask.performPlayerMove(player, location);
    }

    @AllArgsConstructor
    class MovePacket extends PacketData {
        short x;
        short y;
    }
}
