package com.forgestorm.server.network.game.packet.in;

import com.forgestorm.server.ServerMain;
import com.forgestorm.server.game.world.entity.Player;
import com.forgestorm.server.game.world.maps.Location;
import com.forgestorm.server.game.world.task.MovementUpdateTask;
import com.forgestorm.server.network.game.shared.*;
import lombok.AllArgsConstructor;

@Opcode(getOpcode = Opcodes.MOVE_REQUEST)
public class PlayerMovePacketIn implements PacketListener<PlayerMovePacketIn.MovePacket> {

    @Override
    public PacketData decodePacket(ClientHandler clientHandler) {
        final int x = clientHandler.readInt();
        final int y = clientHandler.readInt();
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
        // TODO: REMOVE SHORT CAST!!
        Location location = new Location(player.getMapName(), (short) packetData.x, (short) packetData.y);

        if (!movementUpdateTask.preMovementChecks(player, location)) return;

        movementUpdateTask.performPlayerMove(player, location);
    }

    @AllArgsConstructor
    class MovePacket extends PacketData {
        int x;
        int y;
    }
}
